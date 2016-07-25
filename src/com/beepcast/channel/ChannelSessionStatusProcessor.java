package com.beepcast.channel;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.common.ChannelSessionAlert;
import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.ClientCountriesCommon;
import com.beepcast.dbmanager.common.ClientLevelCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.QClientLevel;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TClientToCountries;
import com.beepcast.dbmanager.table.TClientToCountry;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.subscriber.SubscriberApp;
import com.beepcast.subscriber.SubscriberGroupBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionStatusProcessor {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionStatusProcessor" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private ChannelApp channelApp;
  private ChannelConf channelConf;

  private SubscriberApp subscriberApp;

  private ChannelSessionSubscriberService cssuService;
  private ChannelSessionService csService;
  private ChannelLogService clService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionStatusProcessor( ChannelApp channelApp ,
      ChannelConf channelConf ) {
    initialized = false;

    this.channelApp = channelApp;
    this.channelConf = channelConf;

    subscriberApp = SubscriberApp.getInstance();

    cssuService = new ChannelSessionSubscriberService();
    DLog.debug( lctx , "Prepared channel session subscriber service" );
    csService = new ChannelSessionService();
    DLog.debug( lctx , "Prepared channel session service" );
    clService = new ChannelLogService();
    DLog.debug( lctx , "Prepared channel log service" );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Functions
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean process() {
    boolean result = false;
    try {

      // get and available channel sessions
      List channelSessions = csService.getAvailableChannelSessions( channelConf
          .getWorkerPoolExecutor() );
      if ( ( channelSessions == null ) || ( channelSessions.size() < 1 ) ) {
        return result;
      }
      // process all new status inside channel sessions
      DLog.debug( lctx , "Processing new status channel sessions , total = "
          + channelSessions.size() + " record(s)" );
      ChannelSessionBean csBean = null;
      Iterator iter = channelSessions.iterator();
      while ( iter.hasNext() ) {
        csBean = (ChannelSessionBean) iter.next();
        if ( csBean == null ) {
          continue;
        }
        String headerLog = ChannelSessionLogSupport.headerLog( csBean );
        // process channel session bean
        if ( !processNewStatusChannelSession( csBean ) ) {
          DLog.warning( lctx , headerLog + "Failed to process new status "
              + "channel session" );
          continue;
        }
        // updated to clear the new status back
        if ( !csService.updateStatusEmptyChannelSession( csBean.getId() ) ) {
          DLog.warning( lctx , headerLog + "Failed to update status "
              + "empty channel session" );
          continue;
        }
        DLog.debug( lctx , headerLog
            + "Processed new status channel session : "
            + ChannelSessionNewStatus.toString( csBean.getNewStatus() ) );
      }
      DLog.debug( lctx , "Successfully processed new status channel sessions" );

      result = true;
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to process , " + e );
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Functions
  //
  // ////////////////////////////////////////////////////////////////////////////

  private TClientLevel getTClientLevel( int clientId ) {
    TClientLevel clientLevelOut = null;
    if ( clientId < 1 ) {
      return clientLevelOut;
    }
    QClientLevel qClientLevel = ClientCommon.getClientLevel( clientId );
    if ( qClientLevel == null ) {
      return clientLevelOut;
    }
    clientLevelOut = ClientLevelCommon.getClientLevel( qClientLevel
        .getClientLevelId() );
    return clientLevelOut;
  }

  private boolean processNewStatusChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;
    if ( csBean == null ) {
      return result;
    }

    // set default as true result
    result = true;

    // get channelSessionId
    int channelSessionId = csBean.getId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // get subscriber group bean
    SubscriberGroupBean sgBean = subscriberApp.getSubscriberGroupBean( csBean
        .getGroupSubscriberId() );
    if ( sgBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to process channel session "
          + ", found null subscriber group bean" );
      return result;
    }

    // get event bean
    TEvent eventOut = EventCommon.getEvent( csBean.getEventId() );
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to process channel session "
          + ", found empty event bean" );
      return result;
    }
    csBean.setEventOut( eventOut );
    DLog.debug(
        lctx ,
        headerLog + "Updated channelSessionBean's eventOut " + ": eventId = "
            + eventOut.getEventId() + " , eventName = "
            + eventOut.getEventName() );

    // get client bean
    TClient clientOut = ClientCommon.getClient( eventOut.getClientId() );
    if ( clientOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to process channel session "
          + ", found empty client bean" );
      return result;
    }
    csBean.setClientOut( clientOut );
    DLog.debug( lctx , headerLog + "Updated channelSessionBean's clientOut "
        + ": clientId = " + clientOut.getClientId() + " , companyName = "
        + clientOut.getCompanyName() );

    // get client level bean
    TClientLevel clientLevelOut = getTClientLevel( clientOut.getClientId() );
    if ( clientLevelOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to process channel session "
          + ", found empty client level bean" );
      return result;
    }
    csBean.setClientLevelOut( clientLevelOut );
    DLog.debug(
        lctx ,
        headerLog + "Updated channelSessionBean's " + "clientLevelOut : id = "
            + clientLevelOut.getId() + " , maxCampaignConcurrent = "
            + clientLevelOut.getMaxCampaignConcurrent()
            + " , maxCampaignThroughput = "
            + clientLevelOut.getMaxCampaignThroughput() );

    // log first
    DLog.debug(
        lctx ,
        headerLog
            + "Found a new channel session ready to process , with : newStatus = "
            + ChannelSessionNewStatus.toString( csBean.getNewStatus() )
            + " , suspended = " + csBean.isSuspended() + " , started = "
            + csBean.isStarted() + " , finished = " + csBean.isFinished()
            + " , dateScheduledStart = "
            + DateTimeFormat.convertToString( csBean.getDateSchedulledStart() )
            + " , dateScheduledFinish = "
            + DateTimeFormat.convertToString( csBean.getDateSchedulledFinish() ) );

    // parse and process the new status
    if ( csBean.getNewStatus() == ChannelSessionNewStatus.ST_SCHEDULLED ) {
      DLog.debug( lctx , headerLog + "Perform to start scheduled broadcast" );
      if ( processStartChannelSession( csBean ) ) {
        ChannelSessionAlert.sendAlertStartScheduledBroadcast( csBean , sgBean ,
            clientOut , eventOut );
      } else {
        DLog.warning( lctx , headerLog + "Failed to start "
            + "scheduled broadcast" );
      }
    }
    if ( csBean.getNewStatus() == ChannelSessionNewStatus.ST_STARTED ) {
      DLog.debug( lctx , headerLog + "Perform to start broadcast" );
      if ( processStartChannelSession( csBean ) ) {
        ChannelSessionAlert.sendAlertStartManualBroadcast( csBean , sgBean ,
            clientOut , eventOut );
      } else {
        DLog.warning( lctx , headerLog + "Failed to start broadcast" );
      }
    }
    if ( csBean.getNewStatus() == ChannelSessionNewStatus.ST_SUSPENDED ) {
      DLog.debug( lctx , headerLog + "Perform to suspend broadcast" );
      if ( processSuspendChannelSession( csBean ) ) {
        ChannelSessionAlert.sendAlertSuspendedBroadcast( csBean , sgBean ,
            clientOut , eventOut );
      } else {
        DLog.warning( lctx , headerLog + "Failed to suspend broadcast" );
      }
    }
    if ( csBean.getNewStatus() == ChannelSessionNewStatus.ST_FINISHED ) {
      DLog.debug( lctx , headerLog + "Perform to finish broadcast" );
      if ( processFinishChannelSession( csBean ) ) {
        ChannelSessionAlert.sendAlertFinishedBroadcast( csBean , 0 , sgBean ,
            clientOut , eventOut );
      } else {
        DLog.warning( lctx , headerLog + "Failed to finish broadcast" );
      }
    }

    // final log
    DLog.debug( lctx , headerLog + "Successfully processed "
        + "channel session new status" );
    return result;
  }

  private boolean processStartChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;

    // get channelSessionId
    int channelSessionId = csBean.getId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // verify concurrent broadcast running
    if ( !csService.validChannelSessionConcurrent( csBean ) ) {
      DLog.warning( lctx , headerLog + "Failed to start channel session "
          + ", found concurrent channel session is reached limit" );
      return result;
    }

    // read and verify : client and event profile
    TClient clientOut = csBean.getClientOut();
    TEvent eventOut = csBean.getEventOut();
    if ( ( clientOut == null ) || ( eventOut == null ) ) {
      DLog.warning( lctx , headerLog + "Failed to start channel session "
          + ", found invalid client and/or event profile" );
      return result;
    }

    // if not from suspend state than populate the subscribers
    boolean isFromSuspendState = csBean.isSuspended();
    if ( isFromSuspendState ) {

      DLog.debug( lctx , headerLog + "Bypass to populate subscribers "
          + ", found previous state as suspended" );

      // set update flag start in the table channel session
      boolean updateStarted = csService
          .updateStartedChannelSession( channelSessionId );
      if ( !updateStarted ) {
        DLog.warning( lctx , headerLog
            + "Failed to update flag start in channel session table" );
        return result;
      }

    } else {

      // prepare parameters
      int clientId = clientOut.getClientId();
      int channelId = eventOut.getEventId();
      int subscriberGroupId = csBean.getGroupSubscriberId();
      boolean allowDuplicated = csBean.isAllowDuplicated();
      boolean useDateSend = csBean.isUseDateSend();

      // log first
      DLog.debug( lctx , headerLog + "Storing subscribers into "
          + "channel log table with : clientId = " + clientId
          + " , channelId = " + channelId + " , subscriberGroupId = "
          + subscriberGroupId + " , allowDuplicated = " + allowDuplicated
          + " , useDateSend = " + useDateSend );

      // store and get total record subscribers
      // from client_subscriber to channel_log
      long totalSubscribers = storeSubscribers( channelSessionId , channelId ,
          subscriberGroupId , allowDuplicated , useDateSend , clientId );
      DLog.debug( lctx , headerLog + "Stored subscribers inside "
          + "channel log table , total = " + totalSubscribers + " record(s)" );

      // calculate total debit units
      double totalDebitUnits = clService
          .getTotalCreditCostForRemainsSubscriber( channelSessionId ,
              channelId , clientId , false );
      DLog.debug( lctx , headerLog + "Calculated total debit amount = "
          + totalDebitUnits + " unit(s)" );

      // prepare the update field for start broadcast
      csBean.setDateStarted( new Date() );
      csBean.setTotalNumbers( totalSubscribers );
      csBean.setTotalRemains( totalSubscribers );
      csBean.setDebitUnits( totalDebitUnits );

      // log it
      DLog.debug( lctx , headerLog + "Starting channel session from beginning" );

      // set update flag start in the table channel session
      boolean updateStarted = csService.updateStartedChannelSession(
          channelSessionId , csBean.getTotalNumbers() , csBean.getDebitUnits() ,
          csBean.getDateStarted() );
      if ( !updateStarted ) {
        DLog.warning( lctx , headerLog + "Failed to update flag start "
            + "in channel session table" );
        return result;
      }

    }

    // run the channel session agent
    DLog.debug( lctx , headerLog + "Run the channel agent now" );
    result = channelApp.executeChannelAgent( csBean );

    return result;
  }

  private boolean processSuspendChannelSession( ChannelSessionBean csBean ) {
    boolean result = true;

    // get channelSessionId
    int channelSessionId = csBean.getId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // set update flag suspended in the table channel session
    boolean updateSuspended = csService.updateSuspendedChannelSession( csBean );
    if ( !updateSuspended ) {
      result = false;
      DLog.warning( lctx , headerLog + "Failed to update flag suspend "
          + "in channel session table" );
      return result;
    }

    return result;
  }

  private boolean processFinishChannelSession( ChannelSessionBean csBean ) {
    boolean result = true;

    // get channelSessionId
    int channelSessionId = csBean.getId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // set update flag suspended in the table channel session
    boolean updateFinished = csService.updateFinishedChannelSession( csBean );
    if ( !updateFinished ) {
      result = false;
      DLog.warning( lctx , headerLog + "Failed to update flag finish "
          + "in channel session table" );
      return result;
    }

    // backup all the unprocessed numbers
    int totalUnprocessedNumbers = cssuService.insertUnprocessedNumbers(
        channelSessionId , csBean.getEventId() );
    DLog.debug( lctx , headerLog + "Saved all the unprocessed numbers "
        + ", total = " + totalUnprocessedNumbers );

    return result;
  }

  private long storeSubscribers( int channelSessionId , int channelId ,
      int subscriberGroupId , boolean allowDuplicated , boolean useDateSend ,
      int clientId ) {
    long totalRecords = 0;

    long deltaTime = System.currentTimeMillis();

    // headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // make sure to clean channel log subscriber
    boolean clean = clService.cleanSubscribers( channelId );
    if ( clean ) {
      DLog.debug( lctx , headerLog + "Cleaned previous subscriber(s) "
          + "from the channel log table" );
    }

    // store subscribers into channel log table
    totalRecords = clService.storeSubscribers( channelSessionId , channelId ,
        subscriberGroupId , allowDuplicated , useDateSend );
    if ( totalRecords < 1 ) {
      DLog.warning( lctx , headerLog + "No need to update "
          + "subscribers country profile , found empty subscribers" );
      return totalRecords;
    }
    DLog.debug( lctx , headerLog + "Successfully store subscriber "
        + totalRecords + " record(s) from client_subscriber "
        + "into channel_log table" );

    // iterate countries support and update subscribers country profile
    TClientToCountries clientCountriesBean = ClientCountriesCommon
        .getClientToCountries( clientId );
    List listIds = clientCountriesBean.getIds();
    Iterator iterIds = listIds.iterator();
    while ( iterIds.hasNext() ) {
      TClientToCountry clientCountryBean = clientCountriesBean
          .getClientToCountry( (String) iterIds.next() );
      if ( clientCountryBean == null ) {
        continue;
      }
      int countryId = clientCountryBean.getCountryId();
      if ( countryId < 1 ) {
        continue;
      }

      try {
        Thread.sleep( 500 ); // trying not to abuse the cpu machine
      } catch ( InterruptedException e ) {
      }
      // update and calculate total of subscriber country profile
      long totalUpdated = clService.updateSubscriberCountryProfile( channelId ,
          countryId );
      DLog.debug( lctx , headerLog + "Updated subscriber country profile , "
          + "for country id = " + countryId + " , total effected = "
          + totalUpdated + " record(s)" );
    }

    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , "Successfully store subcribers , it takes = "
        + deltaTime + " ms" );
    return totalRecords;
  }

}
