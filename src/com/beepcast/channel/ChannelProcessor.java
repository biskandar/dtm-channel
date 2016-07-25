package com.beepcast.channel;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.loadmng.LoadManagementApi;
import com.beepcast.model.transaction.ProcessCode;
import com.beepcast.model.transaction.TransactionInputMessage;
import com.beepcast.model.transaction.TransactionProcess;
import com.beepcast.model.transaction.TransactionProcessFactory;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelProcessor implements Module {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelProcessor" );

  static LoadManagement loadMng = LoadManagement.getInstance();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private final Object lockObject;

  private ChannelApp channelApp;
  private ChannelConf channelConf;

  private OnlinePropertiesApp opropsApp;

  private BoundedLinkedQueue queue;

  private int curActiveWorkers;
  private boolean[] arrActiveWorkers;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelProcessor( ChannelApp channelApp , ChannelConf channelConf ) {

    this.lockObject = new Object();

    if ( channelApp == null ) {
      return;
    }
    this.channelApp = channelApp;

    if ( channelConf == null ) {
      return;
    }
    this.channelConf = channelConf;

    this.opropsApp = OnlinePropertiesApp.getInstance();

    DLog.debug( lctx ,
        "Allow concurrent = " + channelConf.isProcessorConcurrent() );

    queue = new BoundedLinkedQueue( readQueueMaxSize() );
    DLog.debug( lctx , "Created queue with capacity = " + queue.capacity()
        + " msg(s)" );

    curActiveWorkers = 0;
    arrActiveWorkers = new boolean[25];
    for ( int idx = 0 ; idx < arrActiveWorkers.length ; idx++ ) {
      arrActiveWorkers[idx] = false;
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    // start workers
    setupWorkers( readWorkerSize() );
  }

  public void stop() {
    // stop workers
    setupWorkers( 0 );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int refreshWorkers() {
    return setupWorkers( readWorkerSize() );
  }

  public int execute( TransactionInputMessage imsg ) {
    int result = ProcessCode.PROCESS_ERROR;

    if ( imsg == null ) {
      return result;
    }

    loadHit( 1 );

    if ( !allowConcurrent() ) {
      result = executeSynch( imsg );
      return result;
    }

    try {
      queue.put( imsg );
      result = ProcessCode.PROCESS_SUCCEED;
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , "Failed to store input message , " + e );
    }

    return result;
  }

  public int getQueueCurSize() {
    int size = -1;
    try {
      size = queue.size();
    } catch ( Exception e ) {
    }
    return size;
  }

  public static int loadHit() {
    return loadMng.getLoad( LoadManagementApi.HDRPROF_CHANNEL ,
        LoadManagementApi.CONTYPE_SMSMT , "PROCESSOR" , false , false );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void loadHit( int count ) {
    loadMng.hit( LoadManagementApi.HDRPROF_CHANNEL ,
        LoadManagementApi.CONTYPE_SMSMT , "PROCESSOR" , count , true , false );
  }

  private int readQueueMaxSize() {
    int queueSize = 5;
    if ( channelConf != null ) {
      queueSize = channelConf.getProcessorQueueSize();
    }
    if ( opropsApp != null ) {
      queueSize = (int) opropsApp.getLong( "Channel.Processor.QueueSize" ,
          queueSize );
    }
    return queueSize;
  }

  private int readWorkerSize() {
    int workerSize = 1;
    if ( channelConf != null ) {
      workerSize = channelConf.getProcessorWorkerSize();
    }
    if ( opropsApp != null ) {
      workerSize = (int) opropsApp.getLong( "Channel.Processor.WorkerSize" ,
          workerSize );
    }
    return workerSize;
  }

  private int setupWorkers( int workerSize ) {
    int result = -1;
    synchronized ( lockObject ) {
      if ( workerSize < 0 ) {
        workerSize = 0;
      }
      if ( workerSize > arrActiveWorkers.length ) {
        workerSize = arrActiveWorkers.length;
      }
      if ( workerSize == curActiveWorkers ) {
        result = curActiveWorkers;
      }
      if ( workerSize > curActiveWorkers ) {
        for ( int idx = curActiveWorkers ; idx < workerSize ; idx++ ) {
          arrActiveWorkers[idx] = true;
          Thread thread = new ChannelProcessorThread( idx );
          thread.start();
        }
        DLog.debug( lctx , "Updated new workers size : " + curActiveWorkers
            + " -> " + workerSize );
        curActiveWorkers = workerSize;
        result = curActiveWorkers;
      }
      if ( workerSize < curActiveWorkers ) {
        for ( int idx = workerSize ; idx < curActiveWorkers ; idx++ ) {
          arrActiveWorkers[idx] = false;
        }
        DLog.debug( lctx , "Updated new workers size : " + curActiveWorkers
            + " -> " + workerSize );
        curActiveWorkers = workerSize;
        result = curActiveWorkers;
      }
    } // synchronized ( lockObject )
    return result;
  }

  private boolean allowConcurrent() {
    boolean concurrent = false;
    if ( channelConf != null ) {
      concurrent = channelConf.isProcessorConcurrent();
    }
    if ( opropsApp != null ) {
      concurrent = opropsApp.getBoolean( "Channel.Processor.Concurrent" ,
          concurrent );
    }
    return concurrent;
  }

  private int executeSynch( TransactionInputMessage imsg ) {
    int result = ProcessCode.PROCESS_ERROR;
    if ( imsg == null ) {
      DLog.warning( lctx , "Failed to execute , found null input message" );
      return result;
    }
    TransactionProcess transProcess = TransactionProcessFactory
        .generateTransactionProcessStandard();
    if ( transProcess == null ) {
      DLog.warning( lctx , "Failed to execute , found null trans process" );
      return result;
    }
    result = transProcess.main( imsg );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class ChannelProcessorThread extends Thread {

    private int index;

    public ChannelProcessorThread( int index ) {
      super( "ChannelProcessorThread-" + index );
      this.index = index;
    }

    public void run() {
      DLog.debug( lctx , "Worker started" );
      while ( arrActiveWorkers[index] ) {
        try {
          Object object = queue.poll( 5000 );
          if ( object == null ) {
            continue;
          }
          if ( !( object instanceof TransactionInputMessage ) ) {
            continue;
          }
          executeSynch( (TransactionInputMessage) object );
        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to execute transaction input message , "
              + e );
        }
      } // while ( workerActive )
      DLog.debug( lctx , "Worker stopped" );
    }

  }

}
