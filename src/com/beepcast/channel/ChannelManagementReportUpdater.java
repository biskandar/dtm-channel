package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.common.ChannelSessionXipmeSupport;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentLinkedQueue;

public class ChannelManagementReportUpdater implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelManagementReportUpdater" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;
  private ChannelApp app;

  private ChannelManagementReport channelManagementReport;
  private ChannelSessionXipmeSupport channelSessionXipmeSupport;
  private ChannelManagementReportFile channelManagementReportFile;

  private int queueCapacity;
  private ConcurrentLinkedQueue queueDataIn;
  private ConcurrentLinkedQueue queueDataOu;

  private boolean threadActive;
  private Thread[] threadWorkers;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportUpdater( ChannelApp app , int queueSize ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();

    if ( app == null ) {
      DLog.warning( lctx , "Failed to initialize , found null channel app" );
      return;
    }
    this.app = app;

    if ( queueSize < 1 ) {
      DLog.warning( lctx , "Failed to initialize , found zero queue size" );
      return;
    }

    channelManagementReport = new ChannelManagementReport( app );
    channelSessionXipmeSupport = new ChannelSessionXipmeSupport();
    channelManagementReportFile = new ChannelManagementReportFile();

    queueCapacity = queueSize;
    queueDataIn = new ConcurrentLinkedQueue();
    queueDataOu = new ConcurrentLinkedQueue();
    DLog.debug( lctx , "Created concurrentLinkedQueue with capacity : "
        + queueCapacity + " msg(s)" );

    threadActive = false;
    threadWorkers = new Thread[app.getChannelConf()
        .getManagementUpdateChannelSessionReport_workerSize()];
    for ( int idx = 0 ; idx < threadWorkers.length ; idx++ ) {
      threadWorkers[idx] = new ChannelManagementReportUpdaterThread( idx );
    }
    DLog.debug( lctx , "Created threadWorkers with size = "
        + threadWorkers.length );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , found not yet initialized" );
      return;
    }

    threadActive = true;
    for ( int idx = 0 ; idx < threadWorkers.length ; idx++ ) {
      try {
        Thread.sleep( 500 );
      } catch ( InterruptedException e ) {
      }
      threadWorkers[idx].start();
    }

  }

  public void stop() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop , found not yet initialized" );
      return;
    }

    threadActive = false;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportUpdaterRecord createChannelManagementReportUpdaterRecord(
      int channelSessionId , boolean externalRequest ) {
    ChannelManagementReportUpdaterRecord record = new ChannelManagementReportUpdaterRecord();
    record.setChannelSessionId( channelSessionId );
    record.setExternalRequest( externalRequest );
    return record;
  }

  public boolean updateReports() {
    boolean result = false;
    String headerLog = "[UpdateReports-" + System.currentTimeMillis() + "] ";
    List listChannelSessionIds = channelManagementReport
        .listAvailChannelSessionIdsReadyToReport();
    if ( ( listChannelSessionIds == null )
        || ( listChannelSessionIds.size() < 1 ) ) {
      // bypass if found null or empty
      return result;
    }
    DLog.debug( lctx ,
        "Updating internal total " + listChannelSessionIds.size()
            + " broadcast report(s) : " + listChannelSessionIds );
    int queueFreeSize = queueCapacity - queueDataIn.size();
    if ( queueFreeSize < listChannelSessionIds.size() ) {
      DLog.warning( lctx , headerLog + "Failed to updated reports "
          + ", found queueFreeSize = " + queueFreeSize
          + " msg(s) is less than listChannelSessionIds.size "
          + ", queueCapacity = " + queueCapacity + " msg(s)" );
      return result;
    }
    List listCmruRecs = new ArrayList();
    Iterator iterChannelSessionIds = listChannelSessionIds.iterator();
    while ( iterChannelSessionIds.hasNext() ) {
      Integer channelSessionId = (Integer) iterChannelSessionIds.next();
      if ( channelSessionId == null ) {
        continue;
      }
      if ( channelSessionId.intValue() < 1 ) {
        continue;
      }
      listCmruRecs.add( createChannelManagementReportUpdaterRecord(
          channelSessionId.intValue() , false ) );
    }
    result = updateReports( headerLog , listCmruRecs );
    return result;
  }

  public boolean updateReports( String headerLog , List listCmruRecs ) {
    boolean result = false;
    if ( listCmruRecs == null ) {
      return result;
    }
    ChannelManagementReportUpdaterRecord cmruRec = null;
    Iterator iterCmruRecs = listCmruRecs.iterator();
    while ( iterCmruRecs.hasNext() ) {
      try {
        cmruRec = (ChannelManagementReportUpdaterRecord) iterCmruRecs.next();
        if ( cmruRec == null ) {
          continue;
        }
        String headerLog2 = headerLog + "[ChannelSession-"
            + cmruRec.getChannelSessionId() + "] ";
        if ( queueDataIn.contains( cmruRec ) ) {
          DLog.debug( lctx , headerLog2 + "Failed to update report "
              + ", found channelSessionId value is already in the queue" );
          continue;
        }
        if ( !queueDataIn.add( cmruRec ) ) {
          DLog.debug( lctx , headerLog2 + "Failed to update report "
              + ", found failed to add record into the queue" );
          continue;
        }
        DLog.debug(
            lctx ,
            headerLog2 + "Queued record to update "
                + "the summary report : externalRequest = "
                + cmruRec.isExternalRequest() );
      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to update report , " + e );
      }
    } // while ( iterCmruRecs.hasNext() ) {
    result = true;
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class ChannelManagementReportUpdaterRecord {

    private int channelSessionId;
    private boolean externalRequest;

    public ChannelManagementReportUpdaterRecord() {
    }

    public int getChannelSessionId() {
      return channelSessionId;
    }

    public void setChannelSessionId( int channelSessionId ) {
      this.channelSessionId = channelSessionId;
    }

    public boolean isExternalRequest() {
      return externalRequest;
    }

    public void setExternalRequest( boolean externalRequest ) {
      this.externalRequest = externalRequest;
    }

    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + channelSessionId;
      return result;
    }

    public boolean equals( Object obj ) {
      if ( this == obj )
        return true;
      if ( obj == null )
        return false;
      if ( getClass() != obj.getClass() )
        return false;
      final ChannelManagementReportUpdaterRecord other = (ChannelManagementReportUpdaterRecord) obj;
      if ( channelSessionId != other.channelSessionId )
        return false;
      return true;
    }

  }

  class ChannelManagementReportUpdaterThread extends Thread {

    public ChannelManagementReportUpdaterThread( int idx ) {
      super( "ChannelManagementReportUpdaterThread-" + idx );
    }

    public void run() {
      long counter = 0 , delay1s = 1000;
      ChannelManagementReportUpdaterRecord cmruRecNew = null , cmruRecOld = null;
      DLog.debug( lctx , "Thread started" );
      while ( threadActive ) {
        try {

          // set latency to update report between the broadcast id
          Thread.sleep( delay1s );
          counter = counter + delay1s;
          if ( counter < opropsApp.getLong(
              "Channel.UpdateBroadcastReport.IntervalMillisReports" , 5000 ) ) {
            continue;
          }
          counter = 0;

          // read and validate record from the queue
          cmruRecNew = (ChannelManagementReportUpdaterRecord) queueDataIn
              .poll();
          if ( cmruRecNew == null ) {
            cmruRecOld = null;
            continue;
          }

          // is the record is still in process with other workers ?
          if ( queueDataOu.contains( cmruRecNew ) ) {
            continue;
          }

          // header log
          String headerLog = "[ChannelSession-"
              + cmruRecNew.getChannelSessionId() + "] ";

          // bypass if used to be the same channel session id as before
          if ( ( cmruRecOld != null )
              && ( cmruRecOld.getChannelSessionId() == cmruRecNew
                  .getChannelSessionId() ) ) {
            continue;
          }

          // execute to update channel session report & xipme
          {
            queueDataOu.add( cmruRecNew );
            long deltaTime = System.currentTimeMillis();
            DLog.debug( lctx , headerLog + "Persisting channel session "
                + "report" );
            boolean persistedCsReport = channelManagementReport
                .updateChannelSessionReport( cmruRecNew.getChannelSessionId() ,
                    cmruRecNew.isExternalRequest() );
            DLog.debug( lctx , headerLog + "Persisting channel session "
                + "xipme report" );
            boolean persistedCsXipmeReport = channelSessionXipmeSupport
                .persistMapReport( cmruRecNew.getChannelSessionId() );
            boolean persistedCsFileReport = false;
            if ( persistedCsReport || persistedCsXipmeReport ) {
              DLog.debug( lctx , headerLog + "Persisting channel session "
                  + "report file" );
              persistedCsFileReport = channelManagementReportFile
                  .generateFileReport( cmruRecNew.getChannelSessionId() );
            }
            deltaTime = System.currentTimeMillis() - deltaTime;
            DLog.debug( lctx , headerLog + "Persisted report "
                + ": persistedCsReport = " + persistedCsReport
                + " , persistedCsXipmeReport = " + persistedCsXipmeReport
                + " , persistedCsFileReport = " + persistedCsFileReport
                + " , took " + deltaTime + " ms" );
            queueDataOu.remove( cmruRecNew );
          }

          // new became old
          cmruRecOld = cmruRecNew;

        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to update channel management report , "
              + e );
        }
      }
      DLog.debug( lctx , "Thread stopped" );
    }
  }

}
