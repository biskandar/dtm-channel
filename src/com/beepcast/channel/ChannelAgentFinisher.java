package com.beepcast.channel;

import com.beepcast.channel.common.ChannelSessionAlert;
import com.beepcast.channel.common.ChannelSessionReportSupport;
import com.beepcast.channel.common.ClientEventReserveBalance;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.mt.SendBufferService;
import com.beepcast.subscriber.SubscriberGroupBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelAgentFinisher implements Runnable {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelAgentFinisher" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;

  private String headerLog;

  private ChannelApp channelApp;
  private ChannelConf channelConf;
  private ChannelSessionBean csBean;
  private SubscriberGroupBean sgBean;
  private TEvent eventOut;
  private TClient clientOut;
  private TClientLevel clientLevelOut;

  private ChannelMonitorService monitorService;

  private ChannelSessionService csService;

  private SendBufferService sendBufferService;
  private ChannelSessionReportDAO channelSessionReportDAO;
  private ChannelGatewayLogDAO channelGatewayLogDAO;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelAgentFinisher( ChannelApp channelApp , ChannelConf channelConf ,
      ChannelSessionBean csBean , SubscriberGroupBean sgBean , TEvent eventOut ,
      TClient clientOut , TClientLevel clientLevelOut ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();

    headerLog = "";

    if ( channelApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel app" );
      return;
    }
    this.channelApp = channelApp;

    if ( channelConf == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel conf" );
      return;
    }
    this.channelConf = channelConf;

    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel session bean" );
      return;
    }
    this.csBean = csBean;

    if ( sgBean == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null subscriber group bean" );
      return;
    }
    this.sgBean = sgBean;

    if ( eventOut == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null event profile" );
      return;
    }
    this.eventOut = eventOut;

    if ( clientOut == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null client profile" );
      return;
    }
    this.clientOut = clientOut;

    if ( clientLevelOut == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null client level profile" );
      return;
    }
    this.clientLevelOut = clientLevelOut;

    monitorService = new ChannelMonitorService(
        channelApp.getChannelMonitorHolder() );

    csService = new ChannelSessionService();

    sendBufferService = new SendBufferService();
    channelSessionReportDAO = new ChannelSessionReportDAO();
    channelGatewayLogDAO = new ChannelGatewayLogDAO();

    headerLog = "[ChannelSession-" + csBean.getId() + "] ";

    // log some info from channel agent
    DLog.debug( lctx , headerLog + "Initialized properly with : "
        + "csBean.totalNumbers = " + csBean.getTotalNumbers()
        + " , csBean.totalRemains = " + csBean.getTotalRemains() );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {

    if ( !initialized ) {
      DLog.warning( lctx , headerLog + "Failed to start the thread "
          + ", found not yet initialized" );
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      return;
    }

    // trap and log delta time
    long deltaTime = System.currentTimeMillis();
    DLog.debug( lctx , headerLog + "Thread started" );

    // is there any amount for debit per number ?
    double totalDebitAmountPerNumber = ChannelSessionReportSupport
        .calculateTotalDebitAmountForRemainNumbers( csBean.getId() ,
            csBean.isAllowDuplicated() , csBean.isUseDateSend() ,
            csBean.getGroupSubscriberId() , eventOut.getEventId() , 1 );
    if ( !( totalDebitAmountPerNumber > 0 ) ) {
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      // update finished channel session with total drop messages
      updateFinishedChannelSession( 0 );
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Found zero total debit amount per "
          + "number , stop the process here ( takes " + deltaTime + " ms )" );
      return;
    }

    // wait until all the remaining send message empty
    // validate to check any message remains in the send buffer ?
    long totalSendMessages = 0;
    {
      int timeOutMinutes = (int) opropsApp.getLong(
          "Channel.AgentFinisher.ValidateSendMessagesTimeOutMinutes" , 60 * 3 );
      int timeOutTimes = (int) opropsApp.getLong(
          "Channel.AgentFinisher.ValidateSendMessagesTimeOutTimes" , 10 );
      DLog.debug( lctx , headerLog + "Observing send messages with timeout : "
          + timeOutMinutes + " minutes , " + timeOutTimes + " times" );
      totalSendMessages = observeSendMessages( timeOutMinutes , timeOutTimes );
      DLog.debug( lctx , headerLog + "Found total " + totalSendMessages
          + " send message(s) remains in the send buffer table" );
    }

    // observe is there any a drop messages , based on channel log
    // table and gateway log table ?
    long totalDropMessages = 0;
    {
      int timeOutMinutes = (int) opropsApp.getLong(
          "Channel.AgentFinisher.ValidateDropMessagesTimeOutMinutes" , 15 );
      int timeOutTimes = (int) opropsApp.getLong(
          "Channel.AgentFinisher.ValidateDropMessagesTimeOutTimes" , 10 );
      DLog.debug( lctx , headerLog + "Observing drop messages with timeout : "
          + timeOutMinutes + " minutes , " + timeOutTimes + " times" );
      totalDropMessages = observeDropMessages( timeOutMinutes , timeOutTimes );
      DLog.debug( lctx , headerLog + "Found total " + totalDropMessages
          + " drop message(s) for event id = " + eventOut.getEventId() );
    }

    // when found timeout occur stop the process here
    if ( totalSendMessages > 0 ) {
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      // update finished channel session with total drop messages
      updateFinishedChannelSession( totalDropMessages );
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Found send buffer timeout flag is "
          + "occured , stop the process here ( takes " + deltaTime + " ms )" );
      return;
    }

    // when found no drop messages stop the process here
    if ( totalDropMessages < 1 ) {
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      // update finished channel session with total drop messages
      updateFinishedChannelSession( totalDropMessages );
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Found there is no drop messages "
          + ", the total messages in the channel log table is matched "
          + "with the gateway log table, stop the process here ( takes "
          + deltaTime + " ms )" );
      return;
    }

    // alert if any drop numbers found
    {
      ChannelSessionAlert.sendAlertDropMessages( csBean , totalDropMessages ,
          sgBean , clientOut , eventOut );
      DLog.warning( lctx , headerLog + "Found total " + totalDropMessages
          + " drop messages , sent alert email message first" );
    }

    // verify the system property is allow to resent the drop messages
    if ( !opropsApp.getBoolean( "Channel.AgentFinisher.ResentDropMessages" ,
        false ) ) {
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      // update finished channel session with total drop messages
      updateFinishedChannelSession( totalDropMessages );
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.debug( lctx , headerLog + "Found disable property to resent "
          + "all the drop messages , stop the process here ( takes "
          + deltaTime + " ms )" );
      return;
    }

    // perform to resent drop messages
    DLog.debug( lctx , headerLog + "Trying to update to resend total "
        + totalDropMessages + " drop message(s)" );
    if ( !updateToResendDropMessages() ) {
      // unreserve client balance if found any
      ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
      // update finished channel session with total drop messages
      updateFinishedChannelSession( totalDropMessages );
      deltaTime = System.currentTimeMillis() - deltaTime;
      DLog.warning( lctx , headerLog + "Failed to update to resend all "
          + "drop messages , stop the process here ( takes " + deltaTime
          + " ms ) " );
      return;
    }

    // trap and log delta time
    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , headerLog + "Successfully updated to send all "
        + "drop messages ( takes " + deltaTime + " ms )" );
    DLog.debug( lctx , headerLog + "Thread stopped" );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ...

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean updateToResendDropMessages() {
    boolean result = false;
    try {

      // read all channel log ids whose has drop messages
      DLog.debug( lctx , headerLog + "Prepare to marked all drop numbers "
          + "from the channel log table" );
      String strChannelLogIds = channelGatewayLogDAO
          .readStrChannelLogIdsForUnprocessedNumbers( eventOut.getEventId() );
      if ( ( strChannelLogIds == null ) || ( strChannelLogIds.equals( "" ) ) ) {
        DLog.warning( lctx , headerLog + "Failed to resend drop messages "
            + ", found failed to retrieve strChannelLogIds of drop messages" );
        return result;
      }
      DLog.debug( lctx , headerLog + "Read list of dropMessage's "
          + "channelLogId : " + strChannelLogIds );

      // update to marked as resend message from the channel log table
      long totalUpdatedDropMessages = channelGatewayLogDAO
          .updateMarkedChannelLogForUnprocessedNumbers( strChannelLogIds );
      if ( totalUpdatedDropMessages < 1 ) {
        DLog.warning( lctx , headerLog + "Failed to resend drop messages "
            + ", found failed to update marked channel log records" );
        return result;
      }
      DLog.debug( lctx , headerLog + "Updated marked resend from channel log "
          + ", total " + totalUpdatedDropMessages + " record(s)" );

      // re-update channel session : total remains field
      DLog.debug( lctx , headerLog + "Re-updated csBean.totalRemains : "
          + csBean.getTotalRemains() + " -> " + totalUpdatedDropMessages );
      csBean.setTotalRemains( totalUpdatedDropMessages );

      // reserve new event balance for drop messages only
      double totalReserveUnits = ChannelSessionReportSupport
          .calculateTotalDebitAmountForRemainNumbers( csBean.getId() ,
              csBean.isAllowDuplicated() , csBean.isUseDateSend() ,
              csBean.getGroupSubscriberId() , csBean.getEventId() ,
              csBean.getTotalRemains() );
      if ( !ClientEventReserveBalance.reserveBalance( headerLog ,
          clientOut.getClientId() , eventOut.getEventId() , totalReserveUnits ) ) {
        DLog.warning( lctx , headerLog + "Failed to reserve "
            + "client prepaid balance , total " + totalReserveUnits
            + " unit(s)" );
        return result;
      }

      // run the channel session agent
      DLog.debug( lctx , headerLog + "Re-run the channel agent now" );
      result = channelApp.executeChannelAgent( csBean );

    } catch ( Exception e ) {
      DLog.debug( lctx , headerLog + "Failed to mark all drop messages ," + e );
    }
    return result;
  }

  private boolean updateFinishedChannelSession( long totalDropMessages ) {
    boolean result = false;

    // re-update date finished to trigger report processing
    csBean.setDateFinished( null );
    boolean updatedFinished = csService.updateFinishedChannelSession( csBean );
    channelSessionReportDAO.updateDateInserted( csBean.getId() , null );

    // log it
    DLog.debug( lctx , headerLog
        + ( updatedFinished ? "Successfully updated " : "Failed to update " )
        + "flag finished in channel session" );

    // send email notification to support guys attached with
    // the drop message information if any
    ChannelSessionAlert.sendAlertFinishedBroadcast( csBean , totalDropMessages ,
        sgBean , clientOut , eventOut );
    DLog.debug( lctx , headerLog + "Sent alert finished broadcast" );

    // end monitor
    endMonitor();

    result = true;
    return result;
  }

  private long observeDropMessages( long timeOutMinutes , int timeOutTimes ) {
    long totalDropMessagesCur = 0;

    long delay1s = 1000;
    long delay1m = delay1s * 60;

    long counterSleep = 0 , counterTimeOutMinutes = 0 , counterTimeOutTimes = 0;

    while ( true ) {

      // counter for sleep
      try {
        Thread.sleep( delay1s );
      } catch ( InterruptedException e ) {
      }
      counterSleep = counterSleep + delay1s;
      if ( counterSleep < delay1m ) {
        continue;
      }
      counterSleep = 0;

      // counter for timeout minutes
      counterTimeOutMinutes = counterTimeOutMinutes + delay1m;
      if ( counterTimeOutMinutes >= ( timeOutMinutes * 60 * 1000 ) ) {
        DLog.warning( lctx , headerLog + "Failed to process next read "
            + "total drop messages , found reached timeout " + timeOutMinutes
            + " minutes" );
        break;
      }

      // read total drop messages from channel and gateway log table
      long totalDropMessagesNew = channelGatewayLogDAO
          .getTotalUnprocessedNumbers( eventOut.getEventId() );

      // counter for timeout times
      if ( totalDropMessagesNew == totalDropMessagesCur ) {
        counterTimeOutTimes = counterTimeOutTimes + 1;
      } else {
        counterTimeOutTimes = 0;
      }
      if ( counterTimeOutTimes >= timeOutTimes ) {
        DLog.warning( lctx , headerLog + "Failed to process next read "
            + "total drop messages , found reached timeout " + timeOutTimes
            + " times" );
        break;
      }

      // log it
      totalDropMessagesCur = totalDropMessagesNew;
      DLog.debug( lctx , headerLog + "Read " + totalDropMessagesCur
          + " drop messages still remains" );

      // do monitor
      if ( totalDropMessagesCur > 0 ) {
        long totalProcessed = csBean.getTotalNumbers() - totalDropMessagesCur;
        doMonitor( workerMaxLoad() , totalProcessed );
      }

      // if found empty drop messages then quit
      if ( totalDropMessagesCur < 1 ) {
        DLog.debug( lctx , headerLog + "Found empty drop messages "
            + "stopped the waiting process here" );
        break;
      }

    }

    return totalDropMessagesCur;
  }

  private long observeSendMessages( long timeOutMinutes , int timeOutTimes ) {
    long totalSendMessagesCur = 0;

    long delay1s = 1000;
    long delay1m = delay1s * 60;

    long counterSleep = 0 , counterTimeOutMinutes = 0 , counterTimeOutTimes = 0;

    while ( true ) {

      // counter for sleep
      try {
        Thread.sleep( delay1s );
      } catch ( InterruptedException e ) {
      }
      counterSleep = counterSleep + delay1s;
      if ( counterSleep < delay1m ) {
        continue;
      }
      counterSleep = 0;

      // counter for timeout minutes
      counterTimeOutMinutes = counterTimeOutMinutes + delay1m;
      if ( counterTimeOutMinutes >= ( timeOutMinutes * 60 * 1000 ) ) {
        DLog.warning( lctx , headerLog + "Failed to process next read "
            + "total send messages , found reached timeout " + timeOutMinutes
            + " minutes" );
        break;
      }

      // read total current records from send buffer table
      long totalSendMessagesNew = sendBufferService
          .totalRecordsByChannelSessionId( csBean.getId() );

      // counter for timeout times
      if ( totalSendMessagesNew == totalSendMessagesCur ) {
        counterTimeOutTimes = counterTimeOutTimes + 1;
      } else {
        counterTimeOutTimes = 0;
      }
      if ( counterTimeOutTimes >= timeOutTimes ) {
        DLog.warning( lctx , headerLog + "Failed to process next read "
            + "total send messages , found reached timeout " + timeOutTimes
            + " times" );
        break;
      }

      // log it
      totalSendMessagesCur = totalSendMessagesNew;
      DLog.debug( lctx , headerLog + "Read " + totalSendMessagesCur
          + " record(s) still remains in the send buffer table" );

      // do monitor
      if ( totalSendMessagesCur > 0 ) {
        long totalProcessed = csBean.getTotalNumbers() - totalSendMessagesCur;
        doMonitor( workerMaxLoad() , totalProcessed );
      }

      // if found empty send messages then quit
      if ( totalSendMessagesCur < 1 ) {
        DLog.debug( lctx , headerLog + "Found empty send messages "
            + "stopped the waiting process here" );
        break;
      }

    }

    return totalSendMessagesCur;
  }

  private boolean endMonitor() {
    boolean result = false;
    if ( csBean == null ) {
      return result;
    }
    if ( monitorService == null ) {
      return result;
    }
    DLog.debug( lctx , headerLog + "Cleaning monitor" );
    if ( !monitorService.clean( csBean.getId() , channelConf.isDebug() ) ) {
      DLog.warning( lctx , headerLog + "Failed to clean monitor" );
      return result;
    }
    DLog.debug( lctx , headerLog + "Successfully clean monitor" );
    result = true;
    return result;
  }

  private boolean doMonitor( int maxLoad , long totalProcessed ) {
    boolean result = false;
    if ( csBean == null ) {
      return result;
    }
    if ( monitorService == null ) {
      return result;
    }
    if ( maxLoad < 0 ) {
      DLog.warning( lctx , headerLog + "Failed to execute monitor "
          + ", found zero max load" );
      return result;
    }
    if ( totalProcessed < 0 ) {
      DLog.warning( lctx , headerLog + "Failed to execute monitor "
          + ", found zero total processed" );
      return result;
    }
    DLog.debug( lctx , headerLog + "Executing monitor with total number = "
        + csBean.getTotalNumbers() + " record(s) , total processed = "
        + totalProcessed + " record(s)" );
    if ( !monitorService.executeMonitor( csBean.getId() , csBean.getEventId() ,
        "" , csBean.getGroupSubscriberId() , "" , csBean.getTotalNumbers() ,
        maxLoad , totalProcessed , System.currentTimeMillis() ,
        channelConf.isDebug() ) ) {
      DLog.warning( lctx , headerLog + "Failed to execute monitor" );
      return result;
    }
    DLog.debug( lctx , headerLog + "Successfully executed monitor" );
    result = true;
    return result;
  }

  private int workerMaxLoad() {
    int maxLoad = 5;
    if ( channelConf != null ) {
      maxLoad = channelConf.getWorkerMaxLoad();
    }
    if ( opropsApp != null ) {
      maxLoad = (int) opropsApp.getLong( "Channel.Worker.MaxLoad" , maxLoad );
    }
    if ( clientLevelOut != null ) {
      int maxLoadClientLevel = clientLevelOut.getMaxCampaignThroughput();
      if ( maxLoadClientLevel > 0 ) {
        maxLoad = maxLoadClientLevel;
      }
    }
    return maxLoad;
  }

}
