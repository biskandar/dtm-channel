package com.beepcast.channel;

import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelDBReader {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelDBReader" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;

  private ChannelApp channelApp;
  private ChannelConf channelConf;

  private ChannelSessionService csService;
  private ChannelSessionStatusProcessor cssProcessor;
  private ChannelSessionSimulationStatusProcessor csssProcessor;

  private Thread threadWorker;
  private boolean threadActive;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelDBReader( ChannelApp channelApp , ChannelConf channelConf ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();

    if ( channelApp == null ) {
      DLog.warning( lctx , "Failed to construct the object "
          + ", found ChannelApp is null" );
      return;
    }
    this.channelApp = channelApp;
    if ( channelConf == null ) {
      DLog.warning( lctx , "Failed to construct the object "
          + ", found ChannelConf is null" );
      return;
    }
    this.channelConf = channelConf;

    csService = new ChannelSessionService();
    DLog.debug( lctx , "Prepared channel session service" );
    cssProcessor = new ChannelSessionStatusProcessor( channelApp , channelConf );
    DLog.debug( lctx , "Prepared channel session status processor" );
    csssProcessor = new ChannelSessionSimulationStatusProcessor( channelApp ,
        channelConf );
    DLog.debug( lctx , "Prepared channel session simulation status processor" );

    threadWorker = new ChannelDBReaderThread();
    threadActive = false;

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
    threadActive = true;
    threadWorker.start();
  }

  public void stop() {
    threadActive = false;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  class ChannelDBReaderThread extends Thread {

    public ChannelDBReaderThread() {
      super( "ChannelDBReaderThread" );
    }

    public void run() {
      DLog.debug( lctx , "Thread Running" );

      if ( opropsApp.getBoolean(
          "Channel.DBReader.RestartRunningBroadcastsAfterBoot" , false ) ) {
        restartChannelSessions();
      }

      int counter = 0 , delay1s = 1000;
      while ( threadActive ) {

        try {
          Thread.sleep( delay1s );
        } catch ( InterruptedException e ) {
        }
        counter = counter + delay1s;
        if ( counter < opropsApp.getLong( "Channel.DBReader.SleepTime" ,
            channelConf.getDbreaderSleepTime() ) ) {
          continue;
        }
        counter = 0;

        // execute channel session status processor
        if ( opropsApp.getBoolean(
            "Channel.DBReader.EnableBroadcastStatusProcessor" , false ) ) {
          cssProcessor.process();
        }

        // execute channel session simulation status processor
        if ( opropsApp
            .getBoolean(
                "Channel.DBReader.EnableBroadcastSimulationStatusProcessor" ,
                false ) ) {
          csssProcessor.process();
        }

        // send warning for idle scheduled channel session
        // that has been reached the time
        AlertForIdleScheduledChannelSession.processAlertMessages();

      }
      DLog.debug( lctx , "Thread stopped" );
    }

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void restartChannelSessions() {
    DLog.debug( lctx , "Trying to restart all pending channel sesssion(s)" );
    int totalRecords = csService.restartChannelSessions();
    if ( totalRecords > 0 ) {
      DLog.debug( lctx , "Updated flag to restart channel sessions "
          + ", total effected = " + totalRecords + " record(s)" );
    }
  }

}
