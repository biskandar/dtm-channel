package com.beepcast.channel.xipme;

import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.ChannelLogBean;
import com.beepcast.channel.ChannelLogService;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

class XipmeBufferWriter extends Thread {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "XipmeBufferWriter" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;
  private String headerLog;
  private ChannelLogService clService;
  private BoundedLinkedQueue queueChannelLogBeans;
  private int eventId;
  private boolean useDateSend;

  private long totalAvailableNumbers;
  private long totalMissingNumbers;
  private long recordOffset;
  private Object lockRecordOffset;
  private boolean threadActiveWriter;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeBufferWriter( String headerLog ,
      BoundedLinkedQueue queueChannelLogBeans , int eventId ,
      boolean useDateSend , long totalAvailableNumbers ) {
    super( "XipmeBufferWriter" );

    opropsApp = OnlinePropertiesApp.getInstance();
    this.headerLog = headerLog;
    clService = new ChannelLogService();
    this.queueChannelLogBeans = queueChannelLogBeans;
    this.eventId = eventId;
    this.useDateSend = useDateSend;

    this.totalAvailableNumbers = totalAvailableNumbers;
    this.totalMissingNumbers = 0;
    this.recordOffset = 0;
    lockRecordOffset = new Object();
    threadActiveWriter = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {
    int limitRecords = 0;
    int counter = 0 , delay500ms = 500;
    int queueCapacity = queueChannelLogBeans.capacity() , queueSize = 0;
    DLog.debug( lctx , headerLog + "Thread started." );
    threadActiveWriter = true;
    while ( threadActiveWriter ) {

      // sleep management

      try {
        Thread.sleep( delay500ms );
      } catch ( InterruptedException e ) {
      }
      counter = counter + delay500ms;
      if ( counter < opropsApp.getLong( "Channel.XipmeBuffer.writerLatency" ,
          5000 ) ) {
        continue;
      }
      counter = 0;

      // define queue remains

      queueSize = queueChannelLogBeans.size();

      // loop if found queue full

      if ( queueSize == queueCapacity ) {
        continue;
      }

      // need to slow down the process based on the queue size

      slowDownWriter( queueCapacity , queueSize );

      // define limitRecords

      limitRecords = limitRecords( limitRecords , queueCapacity , queueSize );

      // load channel log beans

      long deltaTime = System.currentTimeMillis();
      List listChannelLogBeans = null;
      synchronized ( lockRecordOffset ) {
        listChannelLogBeans = clService.getChannelLogBeans( eventId ,
            useDateSend , (int) recordOffset , limitRecords );
      }
      int sizeChannelLogBeans = 0;
      if ( listChannelLogBeans != null ) {
        sizeChannelLogBeans = listChannelLogBeans.size();
      }
      deltaTime = System.currentTimeMillis() - deltaTime;

      // reset offset when found empty result

      if ( sizeChannelLogBeans < 1 ) {
        if ( opropsApp.getBoolean( "Channel.XipmeBuffer.writerLoopAfterFinish" ,
            false ) ) {
          synchronized ( lockRecordOffset ) {
            if ( recordOffset > 0 ) {
              DLog.debug( lctx , headerLog + "Found empty records load from "
                  + "marked channel log beans , and restart writer process : "
                  + "recordOffset = 0" );
            }
            recordOffset = 0;
          }
        } else {
          threadActiveWriter = false;
          DLog.debug( lctx , headerLog + "Found empty records load from "
              + "marked channel log beans , stop writer process here" );
        }
        continue;
      }

      synchronized ( lockRecordOffset ) {

        // log load result

        DLog.debug( lctx , headerLog + "Load marked channel log beans with "
            + ": offset = " + recordOffset + " , limit = " + limitRecords
            + " ; result ( takes " + deltaTime + " ms ) : read = "
            + sizeChannelLogBeans + " record(s)" );

        // prepare for the next empty list

        recordOffset = recordOffset + sizeChannelLogBeans;

      }

      // store channel log beans into the queue

      queueListChannelLogBeans( listChannelLogBeans );

    } // while ( threadActive )
    DLog.debug( lctx , headerLog + "Thread stopped." );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void moduleStart( long recordOffset ) {
    synchronized ( lockRecordOffset ) {
      this.recordOffset = recordOffset;
      DLog.debug( lctx , headerLog + "Thread starting with : recordOffset = "
          + this.recordOffset );
    }
    super.start();
  }

  public void moduleStop() {
    DLog.debug( lctx , headerLog + "Thread stopping" );
    threadActiveWriter = false;
  }

  public boolean isThreadActiveWriter() {
    return threadActiveWriter;
  }

  public long updateRecordOffset( long totalRemains ) {
    long delay = 0;

    synchronized ( lockRecordOffset ) {

      int threshold = (int) opropsApp.getLong(
          "Channel.XipmeBuffer.synchRemainsThreshold" , 1000 );

      int limit = (int) opropsApp.getLong(
          "Channel.XipmeBuffer.synchRemainsLimit" , 10000 );

      long recordOffsetOld = recordOffset;
      long channelAgentTotalRemains = totalRemains;
      long xipmeBufferTotalRemains = ( totalAvailableNumbers - totalMissingNumbers )
          - recordOffsetOld;
      int sizeQueueChannelLogBeans = queueChannelLogBeans.size();
      boolean threadActive = threadActiveWriter;

      DLog.debug( lctx , headerLog
          + "Trying to update record offset if possible "
          + ", with : recordOffset = " + recordOffsetOld
          + " , channelAgentTotalRemains = " + channelAgentTotalRemains
          + " , xipmeBufferTotalRemains = " + xipmeBufferTotalRemains
          + " , sizeQueueChannelLogBeans = " + sizeQueueChannelLogBeans
          + " , threadActiveWriter = " + threadActive );

      // ignore process if found worker is stopped
      if ( threadActive == false ) {
        return delay;
      }

      // ignore process if remains almost empty

      if ( channelAgentTotalRemains < limit ) {
        return delay;
      }
      if ( xipmeBufferTotalRemains < limit ) {
        return delay;
      }

      // ignore if the queue is still below threshold

      if ( sizeQueueChannelLogBeans < threshold ) {
        return delay;
      }

      // slow down if the delta reached the threshold value

      int delta = (int) ( channelAgentTotalRemains - xipmeBufferTotalRemains );
      if ( delta > threshold ) {
        return delay;
      }

      delay = (int) opropsApp.getLong( "Channel.XipmeBuffer.startTimeout" ,
          30000 );

      DLog.warning( lctx , headerLog
          + "Found delta numbers is reached threshold ( = " + delta
          + " ) set delay = " + delay + " ms : channelAgentTotalRemains = "
          + channelAgentTotalRemains + " , xipmeBufferTotalRemains = "
          + xipmeBufferTotalRemains + " , totalAvailableNumbers = "
          + totalAvailableNumbers + " , totalMissingNumbers = "
          + totalMissingNumbers );

      if ( delta > ( threshold / 2 ) ) { // is almost half threshold ?
        return delay;
      }

      long recordOffsetNew = recordOffsetOld + threshold;
      long missingNumbers = recordOffsetNew - recordOffsetOld;

      DLog.warning( lctx , headerLog
          + "Found delta numbers is almost zero ( = " + delta
          + " ) set delay = " + delay + " ms : updated new offset = "
          + recordOffsetOld + " -> " + recordOffsetNew + " , missingNumbers = "
          + missingNumbers );

      totalMissingNumbers += missingNumbers;
      recordOffset = recordOffsetNew;

    } // synchronized ( lockRecordOffset )

    return delay;
  }

  public int queueListChannelLogBeans( List listChannelLogBeans ) {
    int totalQueued = 0;

    if ( listChannelLogBeans == null ) {
      return totalQueued;
    }

    StringBuffer sbLog = new StringBuffer();
    long deltaTime = System.currentTimeMillis();

    Iterator iterChannelLogBeans = listChannelLogBeans.iterator();
    while ( iterChannelLogBeans.hasNext() ) {
      try {

        // read channelLogBean
        ChannelLogBean clBean = (ChannelLogBean) iterChannelLogBeans.next();

        // bypass for unmarked record
        if ( clBean.getMarked() < 1 ) {
          continue;
        }

        // read channelLogId
        String clId = Integer.toString( clBean.getId() );

        // store into queue
        queueChannelLogBeans.put( clBean );

        // trap counter
        totalQueued = totalQueued + 1;
        sbLog.append( clId );
        sbLog.append( ", " );

      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to store channel log bean "
            + "into queue , " + e );
      }
    } // while ( iterChannelLogBeans.hasNext() )

    deltaTime = System.currentTimeMillis() - deltaTime;

    if ( totalQueued > 0 ) {
      DLog.debug( lctx , headerLog
          + "Stored listChannelLogBeans into the queue " + totalQueued
          + " msg(s) ( takes " + deltaTime + " ms ) : " + sbLog.toString() );
    }

    return totalQueued;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private int queueEmpty( int queueCapacity , int queueSize ) {
    return queueCapacity - queueSize;
  }

  private int queueEmptyPerc( int queueCapacity , int queueSize ) {
    int queueEmpty = queueEmpty( queueCapacity , queueSize );
    return (int) ( (double) queueEmpty * 100.0 / (double) queueCapacity );
  }

  private void slowDownWriter( int queueCapacity , int queueSize ) {
    long delay = 0;

    int queueEmptyPerc = queueEmptyPerc( queueCapacity , queueSize );
    if ( queueEmptyPerc <= 50 ) {
      delay = 10;
    }
    if ( queueEmptyPerc <= 25 ) {
      delay = 50;
    }
    if ( queueEmptyPerc <= 10 ) {
      delay = 100;
    }
    if ( queueEmptyPerc <= 5 ) {
      delay = 250;
    }

    if ( delay > 0 ) {
      DLog.debug( lctx , headerLog + "Queue empty space reached "
          + queueEmptyPerc + " % , try to slow down process : " + delay + " ms" );
      try {
        Thread.sleep( delay );
      } catch ( InterruptedException e ) {
      }
    }
  }

  private int limitRecords( int limitRecordsOld , int queueCapacity ,
      int queueSize ) {

    int limitRecordsNew = (int) opropsApp.getLong(
        "Channel.XipmeBuffer.limitRecords" , 500 );

    int queueEmpty = queueEmpty( queueCapacity , queueSize );

    if ( queueEmpty < limitRecordsNew ) {
      limitRecordsNew = queueEmpty;
    }

    if ( limitRecordsOld != limitRecordsNew ) {
      DLog.debug( lctx , headerLog + "Updated record limits : "
          + limitRecordsOld + " -> " + limitRecordsNew );
    }

    return limitRecordsNew;
  }

}
