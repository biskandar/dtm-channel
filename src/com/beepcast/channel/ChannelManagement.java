package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagement {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelManagement" );

  static final Object lockObject = new Object();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;
  private ChannelApp app;
  private ChannelConf conf;

  private ChannelMonitorService channelMonitorService;

  private Thread workerThread;
  private boolean activeThread;

  private ChannelManagementTask channelManagementTask;
  private ChannelManagementReportUpdater channelManagementReportUpdater;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagement( ChannelApp app ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();
    if ( app == null ) {
      DLog.warning( lctx , "Failed to construct the object "
          + ", found ChannelApp is null" );
      return;
    }
    this.app = app;
    if ( app.getChannelConf() == null ) {
      DLog.warning( lctx , "Failed to construct the object "
          + ", found ChannelConf is null" );
      return;
    }
    conf = app.getChannelConf();

    channelMonitorService = new ChannelMonitorService(
        app.getChannelMonitorHolder() );

    workerThread = new ChannelManagementThread();
    activeThread = false;

    channelManagementTask = new ChannelManagementTask( app );
    channelManagementReportUpdater = new ChannelManagementReportUpdater( app ,
        100 );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , found not yet initialized" );
      return;
    }
    activeThread = true;
    workerThread.start();
    channelManagementReportUpdater.start();
  }

  public void stop() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to stop , found not yet initialized" );
      return;
    }
    channelManagementReportUpdater.stop();
    activeThread = false;
  }

  public boolean updateChannelSessionReports( List listChannelSessionIds ) {
    boolean result = false;
    String headerLog = "[UpdateReports-" + System.currentTimeMillis() + "] ";
    if ( listChannelSessionIds == null ) {
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
      listCmruRecs.add( channelManagementReportUpdater
          .createChannelManagementReportUpdaterRecord(
              channelSessionId.intValue() , true ) );
    }
    result = channelManagementReportUpdater.updateReports( headerLog ,
        listCmruRecs );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class ChannelManagementThread extends Thread {

    public ChannelManagementThread() {
      super( "ChannelManagementThread" );
    }

    public void run() {
      int cpOldWorkers = -1 , cpOldQueueSize = -1;
      int delay1s = 1000 , counter = 0;
      DLog.debug( lctx , "started" );
      while ( activeThread ) {
        try {

          // sleep
          Thread.sleep( delay1s );

          // counter
          counter = counter + delay1s;
          if ( counter < opropsApp.getLong( "Channel.Management.SleepInMillis" ,
              conf.getManagementSleepTime() ) ) {
            continue;
          }
          counter = 0;

          // prepare string log
          StringBuffer sbLog = null;

          // refresh and read channel processor worker and queue
          int cpNewWorkers = app.refreshChannelProcessorWorkers();
          int cpNewQueueSize = app.getChannelProcessorQueueSize();
          if ( ( cpOldWorkers != cpNewWorkers )
              || ( cpOldQueueSize != cpNewQueueSize ) || conf.isDebug() ) {

            if ( sbLog == null ) {
              sbLog = new StringBuffer();
              sbLog.append( "Broadcast Monitor : " );
            } else {
              sbLog.append( ", " );
            }
            sbLog.append( "( Processor : " + cpOldWorkers + " -> "
                + cpNewWorkers + " worker(s) , queued " + cpOldQueueSize
                + " -> " + cpNewQueueSize + " msg(s) ) " );

            cpOldWorkers = cpNewWorkers;
            cpOldQueueSize = cpNewQueueSize;
          }

          // show broadcast monitoring log if any
          String channelMonitorServiceInfo = channelMonitorService.info();
          if ( !StringUtils.isBlank( channelMonitorServiceInfo ) ) {
            if ( sbLog == null ) {
              sbLog = new StringBuffer();
              sbLog.append( "Broadcast Monitor : " );
            } else {
              sbLog.append( ", " );
            }
            sbLog.append( channelMonitorServiceInfo );
          } else {
            // perform clean channel log when
            // there is no broadcast running
            doCleanChannelLog();
          }

          // log it if any
          if ( sbLog != null ) {
            DLog.debug( lctx , sbLog.toString() );
          }

          // perform update broadcast reports
          doUpdateChannelSessionReports();

        } catch ( Exception e ) {
          DLog.warning( lctx , "Failed to do channel management thread , " + e );
        }
      }
      DLog.debug( lctx , "stopped" );
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void doCleanChannelLog() {
    if ( conf.isManagementCleanChannelLog() ) {
      channelManagementTask.doCleanChannelLog();
    }
  }

  private void doUpdateChannelSessionReports() {
    if ( conf.isManagementUpdateChannelSessionReport() ) {
      channelManagementReportUpdater.updateReports();
    }
  }

}
