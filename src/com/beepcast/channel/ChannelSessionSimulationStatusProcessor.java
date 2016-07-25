package com.beepcast.channel;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import com.beepcast.subscriber.SubscriberApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSimulationStatusProcessor {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSimulationStatusProcessor" );

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
  private ChannelSessionSimulationService cssiService;
  private ChannelLogSimulationService clsiService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionSimulationStatusProcessor( ChannelApp channelApp ,
      ChannelConf channelConf ) {
    initialized = false;

    this.channelApp = channelApp;
    this.channelConf = channelConf;

    subscriberApp = SubscriberApp.getInstance();

    cssuService = new ChannelSessionSubscriberService();
    DLog.debug( lctx , "Prepared channel session subscriber service" );
    csService = new ChannelSessionService();
    DLog.debug( lctx , "Prepared channel session service" );
    cssiService = new ChannelSessionSimulationService();
    DLog.debug( lctx , "Prepared channel session simulation service" );
    clsiService = new ChannelLogSimulationService();
    DLog.debug( lctx , "Prepared channel log simulation service" );

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

      // get and available channel session simulations
      List channelSessionSimulations = cssiService
          .getAvailableChannelSessionSimulations( channelConf
              .getWorkerPoolExecutor() );
      if ( ( channelSessionSimulations == null )
          || ( channelSessionSimulations.size() < 1 ) ) {
        return result;
      }
      // process all new status inside channel session simulations
      DLog.debug( lctx ,
          "Processing new status channel session simulations , total = "
              + channelSessionSimulations.size() + " record(s)" );
      ChannelSessionSimulationBean cssBean = null;
      Iterator iter = channelSessionSimulations.iterator();
      while ( iter.hasNext() ) {
        cssBean = (ChannelSessionSimulationBean) iter.next();
        if ( cssBean == null ) {
          continue;
        }
        String headerLog = ChannelSessionLogSupport.headerLog( cssBean
            .getChannelSessionId() );
        // process channel session simulation bean
        if ( !processNewStatusChannelSessionSimulation( cssBean ) ) {
          DLog.warning( lctx , headerLog + "Failed to process new status "
              + "channel session simulation" );
          continue;
        }
        // updated to clear the new status back
        if ( !cssiService.updateStatusEmptyChannelSessionSimulation( cssBean
            .getId() ) ) {
          DLog.warning( lctx , headerLog + "Failed to update status "
              + "empty channel session simulation" );
          continue;
        }
        DLog.debug( lctx , headerLog
            + "Processed new status channel session simulation : "
            + ChannelSessionNewStatus.toString( cssBean.getNewStatus() ) );
      }
      DLog.debug( lctx ,
          "Successfully processed new status channel session simulations" );

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

  private boolean processNewStatusChannelSessionSimulation(
      ChannelSessionSimulationBean cssBean ) {
    boolean result = true;

    // get channelSessionId
    int channelSessionId = cssBean.getChannelSessionId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // compose channel session bean inside
    {
      ChannelSessionBean csBean = csService
          .getChannelSession( channelSessionId );
      if ( ( csBean == null ) || ( !csBean.isActive() ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel session simulation "
            + ", found inactive channel session bean" );
        return result;
      }
      cssBean.setChannelSessionBean( csBean );
      DLog.debug( lctx , headerLog
          + "Updated channelSessionSimulation's channelSessionBean : csId = "
          + csBean.getId() + " , eventId = " + csBean.getEventId()
          + " , groupSubscriberId = " + csBean.getGroupSubscriberId() );

      // get event bean
      TEvent eventOut = EventCommon.getEvent( csBean.getEventId() );
      if ( eventOut == null ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel session simulation "
            + ", found empty event bean" );
        return result;
      }
      csBean.setEventOut( eventOut );
      DLog.debug( lctx , headerLog + "Updated channelSessionBean's eventOut "
          + ": eventId = " + eventOut.getEventId() + " , eventName = "
          + eventOut.getEventName() );

      // get client bean
      TClient clientOut = ClientCommon.getClient( eventOut.getClientId() );
      if ( clientOut == null ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel session simulation "
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
        DLog.warning( lctx , headerLog
            + "Failed to process channel session simulation "
            + ", found empty client level bean" );
        return result;
      }
      csBean.setClientLevelOut( clientLevelOut );
      DLog.debug(
          lctx ,
          headerLog + "Updated channelSessionBean's "
              + "clientLevelOut : id = " + clientLevelOut.getId()
              + " , maxCampaignConcurrent = "
              + clientLevelOut.getMaxCampaignConcurrent()
              + " , maxCampaignThroughput = "
              + clientLevelOut.getMaxCampaignThroughput() );
    }

    // log first
    DLog.debug(
        lctx ,
        headerLog + "Found a new channel session simulation ready to process "
            + ", with : newStatus = "
            + ChannelSessionNewStatus.toString( cssBean.getNewStatus() )
            + " , suspended = " + cssBean.isSuspended() + " , started = "
            + cssBean.isStarted() + " , finished = " + cssBean.isFinished() );

    // parse and process the new status
    if ( cssBean.getNewStatus() == ChannelSessionNewStatus.ST_STARTED ) {
      DLog.debug( lctx , headerLog + "Perform to start broadcast simulation" );
      if ( !processStartChannelSessionSimulation( cssBean ) ) {
        DLog.warning( lctx , headerLog + "Failed to start broadcast simulation" );
      }
    }
    if ( cssBean.getNewStatus() == ChannelSessionNewStatus.ST_FINISHED ) {
      DLog.debug( lctx , headerLog + "Perform to finish broadcast simulation" );
      if ( !processFinishChannelSessionSimulation( cssBean ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to finish broadcast simulation" );
      }
    }

    // final log
    DLog.debug( lctx , headerLog + "Successfully processed "
        + "channel session simulation new status" );
    return result;
  }

  private boolean processStartChannelSessionSimulation(
      ChannelSessionSimulationBean cssBean ) {
    boolean result = false;

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( cssBean
        .getChannelSessionId() );

    // read channel session bean
    ChannelSessionBean csBean = cssBean.getChannelSessionBean();
    if ( csBean == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to start channel session simulation "
          + ", found null channel session bean" );
      return result;
    }

    // read and verify : client and event profile
    TClient clientOut = csBean.getClientOut();
    TEvent eventOut = csBean.getEventOut();
    if ( ( clientOut == null ) || ( eventOut == null ) ) {
      DLog.warning( lctx , headerLog
          + "Failed to start channel session simulation "
          + ", found invalid client and/or event profile" );
      return result;
    }

    // if not from suspend state than populate the subscribers
    boolean isFromSuspendState = cssBean.isSuspended();
    if ( isFromSuspendState ) {

      // update flag started in the table channel session simulation
      if ( !cssiService.updateStartedChannelSessionSimulation( cssBean.getId() ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to start channel session simulation "
            + ", found failed to update flag start in table" );
        return result;
      }

    } else {

      // log first
      DLog.debug(
          lctx ,
          headerLog + "Storing simulation subscribers into "
              + "channel log simulation table with : simulationType = "
              + cssBean.getSimulationType() + " , simulationNumbers = "
              + cssBean.getSimulationNumbers() + " , clientId = "
              + clientOut.getClientId() + " , channelId = "
              + eventOut.getEventId() + " , subscriberGroupId = "
              + csBean.getGroupSubscriberId() + " , allowDuplicated = "
              + csBean.isAllowDuplicated() + " , useDateSend = "
              + csBean.isUseDateSend() );

      // store and get total record simulation subscribers
      // from client subscriber simulation to channel log simulation
      long totalSubscribers = storeSimulationSubscribers(
          cssBean.getChannelSessionId() , cssBean.getSimulationType() ,
          cssBean.getSimulationNumbers() , eventOut.getEventId() ,
          csBean.getGroupSubscriberId() , csBean.isAllowDuplicated() ,
          csBean.isUseDateSend() , clientOut.getClientId() );
      DLog.debug( lctx , headerLog + "Stored simulation subscribers inside "
          + "channel log simulation table , total = " + totalSubscribers
          + " record(s)" );

      // prepare the update field for start broadcast
      cssBean.setDateStarted( new Date() );
      cssBean.setTotalNumbers( totalSubscribers );
      cssBean.setTotalRemains( totalSubscribers );

      // log it
      DLog.debug( lctx , headerLog
          + "Starting channel session simulation from beginning" );

      // set update flag start in the table channel session
      if ( !cssiService.updateStartedChannelSessionSimulation( cssBean.getId() ,
          cssBean.getTotalNumbers() , cssBean.getDebitUnits() ,
          cssBean.getDateStarted() ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to start channel session simulation "
            + ", found failed to update flag start table" );
        return result;
      }

    }

    DLog.debug( lctx , headerLog
        + "Found started channel session simulation , running "
        + "the channel agent now" );

    // run the channel session
    result = channelApp.executeChannelAgentSimulation( cssBean );

    return result;
  }

  private boolean processFinishChannelSessionSimulation(
      ChannelSessionSimulationBean cssBean ) {
    boolean result = false;

    // get channelSessionId
    int channelSessionId = cssBean.getChannelSessionId();

    // compose the headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // update flag finished in the table channel session simulation
    if ( !cssiService.updateFinishedChannelSessionSimulation( cssBean ) ) {
      DLog.warning( lctx , headerLog
          + "Failed to update flag finish in channel session simulation table" );
      return result;
    }

    result = true;
    return result;
  }

  private long storeSimulationSubscribers( int channelSessionId ,
      String simulationType , long simulationNumbers , int channelId ,
      int subscriberGroupId , boolean allowDuplicated , boolean useDateSend ,
      int clientId ) {
    long totalRecords = 0;

    long deltaTime = System.currentTimeMillis();

    // headerLog
    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    // make sure to clean channel log subscriber
    if ( clsiService.cleanSubscribers( channelId ) ) {
      DLog.debug( lctx , headerLog
          + "Cleaned previous simulation subscriber(s) "
          + "from the channel log simulation table" );
    }

    // store simulation subscribers into channel log simulation table
    totalRecords = clsiService.storeSubscribers( simulationType ,
        simulationNumbers , channelId , subscriberGroupId , allowDuplicated ,
        useDateSend );
    if ( totalRecords < 1 ) {
      DLog.warning( lctx , headerLog + "No need to update simulation "
          + "subscribers country profile , found empty simulation subscribers" );
      return totalRecords;
    }
    DLog.debug( lctx , headerLog
        + "Successfully store simulation subscriber record(s) "
        + "from client subscriber simulation table into channel log "
        + "simulation table" );

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
      long totalUpdated = clsiService.updateSubscriberCountryProfile(
          channelId , countryId );
      DLog.debug( lctx , headerLog
          + "Updated simulation subscriber country profile , "
          + "for country id = " + countryId + " , total effected = "
          + totalUpdated + " record(s)" );
    }

    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , headerLog
        + "Successfully stored simulation subcribers , it takes = " + deltaTime
        + " ms" );
    return totalRecords;
  }

}
