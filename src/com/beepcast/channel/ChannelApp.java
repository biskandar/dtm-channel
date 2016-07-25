package com.beepcast.channel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.channel.view.ChannelSessionReportInfoBean;
import com.beepcast.channel.view.ChannelSessionReportInfoService;
import com.beepcast.channel.view.ListChannelSessionInfoService;
import com.beepcast.common.util.concurrent.PooledExecutor;
import com.beepcast.model.event.EventBean;
import com.beepcast.model.event.EventService;
import com.beepcast.model.transaction.TransactionInputMessage;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelApp implements Module , ChannelApi {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelApp" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private ChannelConf conf;
  private ChannelSessionService channelSessionService;
  private ChannelSessionSimulationService channelSessionSimulationService;
  private ChannelSessionReportInfoService channelSessionReportInfoService;

  private ChannelMonitorHolder monitorHolder;
  private ChannelMonitorService monitorService;

  private ChannelDBReader dbreader;
  private ChannelProcessor processor;
  private ChannelManagement management;
  private PooledExecutor agentExecutor;

  private ListChannelSessionInfoService listChannelSessionInfoService;
  private ChannelLogService channelLogService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void init( ChannelConf conf ) {
    initialized = false;

    if ( conf == null ) {
      DLog.warning( lctx , "Failed to init , found null conf" );
      return;
    }

    this.conf = conf;
    DLog.debug( lctx , "Debug mode = " + conf.isDebug() );
    DLog.debug( lctx , "Defined message priority = " + conf.getMsgPriority() );
    DLog.debug(
        lctx ,
        "Defined worker params : burstSize = " + conf.getWorkerBurstSize()
            + " , maxLoad = " + conf.getWorkerMaxLoad()
            + " , maxSendBufferRecords = "
            + conf.getWorkerMaxSendBufferRecords() + " , maxBatchRecords = "
            + conf.getWorkerMaxBatchRecords() + " , sleepTime = "
            + conf.getWorkerSleepTime() );

    channelSessionService = new ChannelSessionService();
    DLog.debug( lctx , "Created channel session service module" );
    channelSessionSimulationService = new ChannelSessionSimulationService();
    DLog.debug( lctx , "Created channel session simulation service module" );
    channelSessionReportInfoService = new ChannelSessionReportInfoService();
    DLog.debug( lctx , "Created channel session report info service module" );

    monitorHolder = new ChannelMonitorHolder();
    DLog.debug( lctx , "Created channel monitor holder module" );
    monitorService = new ChannelMonitorService( monitorHolder );
    DLog.debug( lctx , "Created channel monitor service module" );

    dbreader = new ChannelDBReader( this , conf );
    DLog.debug( lctx , "Created channel db reader module" );
    processor = new ChannelProcessor( this , conf );
    DLog.debug( lctx , "Created channel processor module" );
    management = new ChannelManagement( this );
    DLog.debug( lctx , "Created channel management module" );
    agentExecutor = new PooledExecutor( conf.getWorkerPoolExecutor() );
    DLog.debug(
        lctx ,
        "Created agent executor with : poolSize = "
            + conf.getWorkerPoolExecutor() + " worker(s)" );

    listChannelSessionInfoService = new ListChannelSessionInfoService( this ,
        conf );
    DLog.debug( lctx , "Created list channel session info service module" );
    channelLogService = new ChannelLogService();
    DLog.debug( lctx , "Created channel log service" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start() {
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to start , not yet initialized" );
      return;
    }

    dbreader.start();
    processor.start();
    management.start();

  }

  public void stop() {

    management.stop();
    processor.stop();
    dbreader.stop();

    initialized = false;
  }

  public boolean insertChannelSession( int eventId , int groupSubscriberId ,
      boolean allowDuplicated , boolean useDateSend , double expectResponse ,
      List listChannelSessionSchedules ) {
    return channelSessionService.insertChannelSession( eventId ,
        groupSubscriberId , allowDuplicated , useDateSend , expectResponse ,
        listChannelSessionSchedules );
  }

  public boolean updateChannelSession( int channelSessionId ,
      int groupSubscriberId , boolean allowDuplicated , boolean useDateSend ,
      double expectResponse , List listChannelSessionSchedules ) {
    return channelSessionService.updateChannelSession( channelSessionId ,
        groupSubscriberId , allowDuplicated , useDateSend , expectResponse ,
        listChannelSessionSchedules );
  }

  public ChannelSessionBean selectChannelSession( int channelSessionId ) {
    ChannelSessionBean csBean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select channel session "
          + ", found zero channel session id" );
      return csBean;
    }
    csBean = channelSessionService.getChannelSession( channelSessionId );
    return csBean;
  }

  public ChannelSessionSimulationBean selectChannelSessionSimulation(
      int channelSessionId ) {
    ChannelSessionSimulationBean cssBean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to select channel session "
          + ", found zero channel session id" );
      return cssBean;
    }
    cssBean = channelSessionSimulationService
        .selectByChannelSessionId( channelSessionId );
    return cssBean;
  }

  public ChannelSessionBean getLastChannelSession( int eventId ) {
    ChannelSessionBean channelSessionBean = null;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get the last channel session "
          + ", found zero eventId" );
      return channelSessionBean;
    }
    channelSessionBean = channelSessionService.getLastChannelSession( eventId );
    return channelSessionBean;
  }

  public boolean executeChannelAgent( ChannelSessionBean channelSessionBean ) {
    boolean result = false;

    if ( channelSessionBean == null ) {
      DLog.warning( lctx , "Can not execute channel agent "
          + ", found null channel session bean" );
      return result;
    }

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionBean );

    // create channelAgent
    ChannelAgent channelAgent = new ChannelAgent( this , channelSessionBean );

    // run it
    DLog.debug( lctx , headerLog + "Trying to execute channel agent" );
    try {
      agentExecutor.execute( channelAgent );
      result = true;
      DLog.debug( lctx , headerLog + "Successfully executed the channel agent" );
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to execute the channel agent , "
          + e );
    }

    return result;
  }

  public boolean executeChannelAgentSimulation(
      ChannelSessionSimulationBean channelSessionSimulationBean ) {
    boolean result = false;

    if ( channelSessionSimulationBean == null ) {
      DLog.warning( lctx , "Can not execute channel agent simulation "
          + ", found null channel session simulation bean" );
      return result;
    }

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport
        .headerLog( channelSessionSimulationBean.getChannelSessionId() );

    // create channelAgentSimulation
    ChannelAgentSimulation channelAgentSimulation = new ChannelAgentSimulation(
        this , channelSessionSimulationBean );

    // run it
    DLog.debug( lctx , headerLog + "Trying to execute channel agent "
        + "simulation" );
    try {
      agentExecutor.execute( channelAgentSimulation );
      result = true;
      DLog.debug( lctx , headerLog + "Successfully executed the channel agent "
          + "simulation" );
    } catch ( InterruptedException e ) {
      DLog.warning( lctx , headerLog + "Failed to execute the channel agent "
          + "simulation , " + e );
    }

    return result;
  }

  public int getChannelProcessorQueueSize() {
    return processor.getQueueCurSize();
  }

  public int refreshChannelProcessorWorkers() {
    return processor.refreshWorkers();
  }

  public int executeChannelProcessor( TransactionInputMessage imsg ) {
    return processor.execute( imsg );
  }

  public String progressToString( int channelSessionId ) {
    return monitorService.progressToString( channelSessionId );
  }

  public List listHistoryBroadcasts( int clientId ) {
    return listChannelSessionInfoService.getListHistoryBroadcast( clientId );
  }

  public List listScheduledBroadcasts( int clientId ) {
    return listChannelSessionInfoService.getListSchedulledBroadcast( clientId );
  }

  public List listRunningBroadcasts( int clientId ) {
    return listChannelSessionInfoService.getListRunningBroadcast( clientId );
  }

  public List listHistoryBroadcastsFromMaster( int clientType ,
      int masterClientId ) {
    return listChannelSessionInfoService.getListHistoryBroadcastFromMaster(
        clientType , masterClientId );
  }

  public List listScheduledBroadcastsFromMaster( int clientType ,
      int masterClientId ) {
    return listChannelSessionInfoService.getListSchedulledBroadcastFromMaster(
        clientType , masterClientId );
  }

  public List listRunningBroadcastsFromMaster( int clientType ,
      int masterClientId ) {
    return listChannelSessionInfoService.getListRunningBroadcastFromMaster(
        clientType , masterClientId );
  }

  public ChannelLogBean getChannelLogBean( int eventId , String phoneNumber ) {
    return channelLogService.getChannelLogBean( eventId , phoneNumber );
  }

  public String createBroadcastFileName( int channelSessionId , boolean fullPath ) {
    String fileName = null;
    try {

      if ( channelSessionId < 1 ) {
        DLog.warning( lctx , "Failed to create broadcast file name "
            + ", found zero channel session id" );
        return fileName;
      }

      ChannelSessionBean csBean = channelSessionService
          .getChannelSession( channelSessionId );
      if ( csBean == null ) {
        DLog.warning( lctx , "Failed to create broadcast file name "
            + ", found invalid channel session id = " + channelSessionId );
        return fileName;
      }

      EventBean evBean = null;
      EventService evService = new EventService();
      evBean = evService.select( csBean.getEventId() );
      if ( evBean == null ) {
        DLog.warning( lctx , "Failed to create broadcast file name "
            + ", found invalid event id = " + csBean.getEventId() );
        return fileName;
      }

      String evName = evBean.getEventName();
      if ( StringUtils.isBlank( evName ) ) {
        DLog.warning( lctx , "Failed to create broadcast file name "
            + ", found blank event name from id = " + csBean.getEventId() );
        return fileName;
      }

      ChannelManagementReportFile cmrFile = new ChannelManagementReportFile();
      fileName = cmrFile.filePathWithoutExtension( evName );

      if ( fullPath ) {
        String basePath = conf
            .getManagementUpdateChannelSessionReport_downloadPath();
        if ( ( basePath != null ) && ( !basePath.endsWith( "/" ) ) ) {
          basePath = basePath.concat( "/" );
        }
        if ( !StringUtils.isBlank( basePath ) ) {
          fileName = basePath.concat( fileName );
        }
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to create broadcast file name , " + e );
    }
    return fileName;
  }

  public ChannelSessionReportInfoBean getReportInfo( int channelSessionId ) {
    ChannelSessionReportInfoBean channelSessionReportInfoBean = null;
    if ( channelSessionId < 1 ) {
      return channelSessionReportInfoBean;
    }

    // select channel session report info based on channel session id
    channelSessionReportInfoBean = channelSessionReportInfoService
        .getChannelSessionReportInfo( channelSessionId );
    if ( channelSessionReportInfoBean == null ) {
      return channelSessionReportInfoBean;
    }

    // is file report created ?
    String downloadBasePath = conf
        .getManagementUpdateChannelSessionReport_downloadPath();
    String channelSessionKey = channelSessionReportInfoBean.getKey();
    if ( ( ( downloadBasePath != null ) && ( !downloadBasePath.equals( "" ) ) )
        && ( ( channelSessionKey != null ) && ( !channelSessionKey.equals( "" ) ) ) ) {

      // clean download base path
      if ( !downloadBasePath.endsWith( "/" ) ) {
        downloadBasePath = downloadBasePath.concat( "/" );
      }

      // resolved download path file
      File downloadPathFile = new File( downloadBasePath.concat(
          channelSessionKey ).concat( ".zip" ) );

      // is file report exists ? ( update back into the bean )
      boolean fileReportCreated = downloadPathFile.isFile()
          && downloadPathFile.exists();
      channelSessionReportInfoBean.setFileReportCreated( fileReportCreated );

      // log it
      DLog.debug( lctx , "Resolved downloadPathFile = " + downloadPathFile
          + " , fileReportCreated = " + fileReportCreated );

    }

    return channelSessionReportInfoBean;
  }

  public boolean requestToUpdateReport( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      return result;
    }
    List listChannelSessionIds = new ArrayList();
    listChannelSessionIds.add( new Integer( channelSessionId ) );
    result = management.updateChannelSessionReports( listChannelSessionIds );
    return result;
  }

  public boolean requestToUpdateReports( List listChannelSessionIds ) {
    boolean result = false;
    if ( listChannelSessionIds == null ) {
      return result;
    }
    result = management.updateChannelSessionReports( listChannelSessionIds );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isDebug() {
    boolean result = false;
    if ( conf != null ) {
      result = conf.isDebug();
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelConf getChannelConf() {
    return conf;
  }

  public ChannelMonitorHolder getChannelMonitorHolder() {
    return monitorHolder;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Singleton Pattern
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static final ChannelApp INSTANCE = new ChannelApp();

  private ChannelApp() {
  }

  public static final ChannelApp getInstance() {
    return INSTANCE;
  }

}
