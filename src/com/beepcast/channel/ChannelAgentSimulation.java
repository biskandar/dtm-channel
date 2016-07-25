package com.beepcast.channel;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.idgen.IdGenApp;
import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.Node;
import com.beepcast.model.transaction.ProcessCode;
import com.beepcast.model.transaction.TransactionInputMessage;
import com.beepcast.model.transaction.TransactionMessageFactory;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.model.transaction.TransactionProcess;
import com.beepcast.model.transaction.TransactionProcessFactory;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.util.throttle.Throttle;
import com.beepcast.util.throttle.ThrottleProcess;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelAgentSimulation implements Runnable , ThrottleProcess {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelAgentSimulation" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;
  private IdGenApp idGenApp;

  private String headerLog;

  private ChannelApp channelApp;
  private ChannelConf channelConf;

  private ChannelSessionSimulationBean cssBean;
  private int channelSessionSimulationId;
  private TClient clientOut;
  private int clientId;
  private TEvent eventOut;
  private int eventId;
  private TClientLevel clientLevelOut;

  private long idxSubscribers;
  private long ctrSubscribers;
  private List listSubscribers;
  private Iterator iterSubscribers;

  private ChannelSessionSimulationService cssService;
  private ChannelLogSimulationService clsService;

  private int thresholdValueUpdateRemains;
  private int thresholdValueUpdateMonitor;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelAgentSimulation( ChannelApp channelApp ,
      ChannelSessionSimulationBean channelSessionSimulationBean ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();
    if ( opropsApp == null ) {
      DLog.error( lctx , "Failed to initialized "
          + ", found null online properties app" );
      return;
    }

    idGenApp = IdGenApp.getInstance();
    if ( idGenApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null id generator app" );
      return;
    }

    headerLog = "";

    this.channelApp = channelApp;
    if ( this.channelApp == null ) {
      DLog.error( lctx , "Failed to initialized , found null channel app" );
      return;
    }
    this.channelConf = channelApp.getChannelConf();
    if ( this.channelConf == null ) {
      DLog.error( lctx , "Failed to initialized , found null channel conf" );
      return;
    }

    // get channel session simulation object and params
    this.cssBean = channelSessionSimulationBean;
    if ( cssBean == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null channel session simulation bean " );
      return;
    }
    channelSessionSimulationId = cssBean.getId();
    if ( channelSessionSimulationId < 1 ) {
      DLog.warning( lctx , "Can not start the chanel agent simulation "
          + ", found zero channelSessionSimulationId" );
      return;
    }

    // get channel session object and params
    ChannelSessionBean csBean = cssBean.getChannelSessionBean();
    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", read null channel session bean from simulation bean" );
      return;
    }
    int channelSessionId = csBean.getId();
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Can not start the channel agent simulation "
          + ", found zero channelSessionId" );
      return;
    }

    // create header log
    headerLog = "[ChannelSessionSimulation-" + channelSessionId + "] ";

    // get client object and params
    this.clientOut = csBean.getClientOut();
    if ( clientOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to initialized "
          + ", found null client profile" );
      return;
    }
    clientId = clientOut.getClientId();
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
          + ", found zero clientId" );
      return;
    }

    // get event object and params
    this.eventOut = csBean.getEventOut();
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Failed to initialized "
          + ", found null event profile" );
      return;
    }
    eventId = eventOut.getEventId();
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
          + ", found zero eventId" );
      return;
    }

    // get client level
    this.clientLevelOut = csBean.getClientLevelOut();
    if ( clientLevelOut == null ) {
      DLog.warning( lctx , headerLog + "Found empty client level profile" );
    }

    // log it
    DLog.debug( lctx ,
        headerLog + "Define eventId = " + eventId + " , eventName = "
            + StringEscapeUtils.escapeJava( eventOut.getEventName() )
            + " , clientId = " + clientId + " , companyName = "
            + StringEscapeUtils.escapeJava( clientOut.getCompanyName() ) );

    // get temporary channel session last summary
    long totalNumbers = cssBean.getTotalNumbers();
    long totalRemains = cssBean.getTotalRemains();

    // log it
    DLog.debug( lctx , headerLog + "Define channel session last summary "
        + ": total all numbers = " + totalNumbers + " record(s) "
        + ", total remains = " + totalRemains + " record(s)" );

    // init list phone numbers
    ctrSubscribers = 0;
    listSubscribers = null;
    iterSubscribers = null;
    if ( channelConf.isDebug() ) {
      DLog.debug( lctx , headerLog + "Reset counter subscriber : "
          + "ctr = 0 , list = null , iter = null " );
    }
    idxSubscribers = totalNumbers - totalRemains;
    DLog.debug( lctx , headerLog + "Init index subscriber start from = "
        + idxSubscribers );

    // create support object service
    cssService = new ChannelSessionSimulationService();
    clsService = new ChannelLogSimulationService();

    // defined threshold value
    initThresholdRecords( totalNumbers );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {
    DLog.debug( lctx , headerLog + "Channel Agent Simulation Thread started" );

    if ( !initialized ) {
      DLog.warning( lctx , headerLog
          + "Failed to started the broadcast , not initialized properly" );
      return;
    }

    // prepare the throttle params
    int burstSize = workerBurstSize();
    int maxLoad = workerMaxLoad();

    // iterate all record with throttle mechanism
    DLog.debug( lctx , headerLog
        + "Running the simulation broadcast with burstSize = " + burstSize
        + " pipe(s) , maxLoad = " + maxLoad + " msg/sec" );
    Throttle throttle = new Throttle( burstSize , maxLoad );
    try {
      throttle.run( 1 , this );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to run the broadcast "
          + ", found throttle problem , " + e );
    }
    DLog.debug( lctx , headerLog + "Successfully run the broadcast" );

    DLog.debug( lctx , headerLog + "Channel Agent Simulation Thread finished" );
  }

  public boolean process() {

    // calculate the latency
    long deltaTime = System.currentTimeMillis();

    // set default as not looping
    boolean result = true;

    // if the list of phone numbers is empty ?
    if ( ( listSubscribers == null ) || ( listSubscribers.size() < 1 ) ) {

      // validate channel session was finished or suspended
      try {

        // is the current status of channel finished ?
        boolean finished = cssService.isFinished( channelSessionSimulationId );
        if ( finished ) {
          DLog.debug( lctx , headerLog + "Updating zero total number remains "
              + "from the simulation broadcast table" );
          cssService.updateTotalRemainsAndDebitUnits(
              channelSessionSimulationId , 0 , 0 );
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "Found finished status of "
              + "channel object , stop the current pool process , take "
              + deltaTime + " ms" );
          return result;
        }

        // is the time for scheduled stop broadcast ?
        // ...

        // is the current status of channel suspended ?
        boolean suspended = cssService.isSuspended( channelSessionSimulationId );
        if ( suspended ) {
          // get total number remains from db
          long totalNumberRemains = clsService.getTotalRemains( eventId );
          DLog.debug( lctx , headerLog + "Updating total number remains = "
              + totalNumberRemains + " number(s) from the broadcast table" );
          cssService.updateTotalRemainsAndDebitUnits(
              channelSessionSimulationId , totalNumberRemains , 0 );
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog
              + "Found suspended status of channel object "
              + "and there is other record(s) need to be processed"
              + ", stop the current pool process , take " + deltaTime + " ms" );
          return result;
        }

      } catch ( Exception e ) {
        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.warning( lctx , headerLog + "Failed to verify "
            + "channel session simulation was finished "
            + "or suspended state , take " + deltaTime + " ms , " + e );
        return result;
      }

      // validate and load subscriber phone number(s)
      try {

        // load list subscribers
        listSubscribers = clsService.getMarkedChannelLogSimulationBeans(
            eventId , false , channelConf.getWorkerMaxBatchRecords() );

        // is it finished already ?
        if ( ( listSubscribers == null ) || ( listSubscribers.size() < 1 ) ) {

          DLog.debug( lctx , headerLog + "Found all subscriber(s) sent "
              + "already , trying to finish the channel agent process" );
          // there is no remain number
          cssBean.setTotalRemains( 0 );
          cssBean.setDebitUnits( 0 );
          // re-update date finished
          cssBean.setDateFinished( null );
          boolean updatedFinished = cssService
              .updateFinishedChannelSessionSimulation( cssBean );
          deltaTime = System.currentTimeMillis() - deltaTime;
          // log it
          DLog.debug( lctx , headerLog
              + ( updatedFinished ? "Successfully updated "
                  : "Failed to update " )
              + "flag finished in channel session simulation, take "
              + deltaTime + " ms" );
          return result;

        }

        // refresh total remains
        {
          long totalNumberRemains = clsService.getTotalRemains( eventId );
          boolean updated = cssService.updateTotalRemains(
              channelSessionSimulationId , totalNumberRemains );
          DLog.debug( lctx , headerLog
              + ( updated ? "Updated " : "Failed to update " )
              + "total number remains = " + totalNumberRemains + " number(s)" );
        }

        // generate the iterator object
        iterSubscribers = listSubscribers.iterator();
        if ( iterSubscribers == null ) {
          DLog.warning( lctx , headerLog + "Can not start the channel agent "
              + ", failed to generate iterator of phone number(s)" );
          return result;
        }
        DLog.debug( lctx , headerLog + "Found a new list of subscriber(s) "
            + "ready to process , reset the counter to begin" );

      } catch ( Exception e ) {
        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.warning( lctx , headerLog + "Failed to compose list of "
            + "phone number(s) , take " + deltaTime + " ms , " + e );
        return result;
      }

    } // when listSubscribers is empty

    // set default as looping
    result = false;

    // validate is the vector of subscriber empty ? than restart the loop
    if ( !iterSubscribers.hasNext() ) {
      listSubscribers = null;
      DLog.debug( lctx , headerLog
          + "Finished process all simulation subscriber(s) "
          + "data in the list , creating the next list "
          + "of subscriber number(s)" );
      return result;
    }

    // verify threshold and update total remains
    if ( ( ctrSubscribers % thresholdValueUpdateRemains ) == 0 ) {
      long totalRemains = clsService.getTotalRemains( eventId );
      cssService.updateTotalRemains( channelSessionSimulationId , totalRemains );
    }

    // prepared the next channel log profile record
    ChannelLogSimulationBean channelLogSimulationBean = (ChannelLogSimulationBean) iterSubscribers
        .next();

    // process per channel log simulation bean
    int processCode = process( channelLogSimulationBean );

    // calculate latency time
    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it
    DLog.debug(
        lctx ,
        headerLog
            + "Processed channel log simulation , for phone = "
            + channelLogSimulationBean.getPhone()
            + " , channelCode = "
            + eventOut.getCodes()
            + " , dateSend = "
            + DateTimeFormat.convertToString( channelLogSimulationBean
                .getDateSend() ) + " , resultCode = "
            + ProcessCode.processCodeToString( processCode ) + " , take = "
            + deltaTime + " ms" );

    // calculate latency time
    deltaTime = System.currentTimeMillis() - deltaTime;

    return result;
  }

  public int process( ChannelLogSimulationBean clsBean ) {
    int processCode = ProcessCode.PROCESS_SUCCEED;
    try {

      // track the number of channel log traffic counter
      idxSubscribers = idxSubscribers + 1;
      ctrSubscribers = ctrSubscribers + 1;

      // validate must be parameter
      if ( clsBean == null ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel log simulation "
            + ", found empty channel log bean" );
        return processCode;
      }
      if ( StringUtils.isBlank( clsBean.getPhone() ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel log simulation "
            + ", found empty phoneNumber" );
        return processCode;
      }
      if ( StringUtils.isBlank( eventOut.getCodes() ) ) {
        DLog.warning( lctx , headerLog
            + "Failed to process channel log simulation "
            + ", found empty channelCode" );
        return processCode;
      }

      // prepare transaction input message
      TransactionInputMessage imsg = TransactionMessageFactory
          .createInputMessage( newMessageId() , MessageType.TEXT_TYPE ,
              eventOut.getCodes() , Node.DTM , clsBean.getPhone() , null ,
              null , channelSessionSimulationId , channelConf.getMsgPriority() );

      // transaction header log
      String headerLogInMsg = "[Msg-".concat( imsg.getMessageId() ).concat(
          "] " );

      // add message params
      if ( clsBean != null ) {
        DLog.debug(
            lctx ,
            headerLog + headerLogInMsg + "Add message param "
                + TransactionMessageParam.HDR_CHANNEL_LOG_BEAN + " : clsId = "
                + clsBean.getId() + " , csId = "
                + clsBean.getClientSubscriberId() + " , cscId = "
                + clsBean.getClientSubscriberCustomId() + " , evId = "
                + clsBean.getEventId() + " , phone = " + clsBean.getPhone()
                + " , dateSend = "
                + DateTimeFormat.convertToString( clsBean.getDateSend() ) );
        imsg.addMessageParam( TransactionMessageParam.HDR_CHANNEL_LOG_BEAN ,
            clsBean );
      }

      // build transaction process
      TransactionProcess transProcess = TransactionProcessFactory
          .generateTransactionProcessSimulation();

      // execute the process based on transaction input message
      processCode = transProcess.main( imsg );

      // update flag unmarked
      if ( !clsService.updateSubscriberUnmarked( clsBean.getId() ,
          imsg.getMessageId() ) ) {
        DLog.warning( lctx , headerLog + headerLogInMsg
            + "Failed to update unmarked subscriber "
            + "from the channel log simulation" );
      } else {
        DLog.debug( lctx , headerLog + headerLogInMsg
            + "Updated unmarked subscriber "
            + "from the channel log simulation" );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog
          + "Failed to process channel log simulation , " + e );
      processCode = ProcessCode.PROCESS_ERROR;
    }
    return processCode;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String getHeaderLog() {
    return headerLog;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String newMessageId() {
    return "CLG".concat( idGenApp.nextIdentifier() );
  }

  private void initThresholdRecords( long totalNumbers ) {
    thresholdValueUpdateRemains = 100;
    thresholdValueUpdateMonitor = 100;
    if ( totalNumbers > 1000 ) {
      thresholdValueUpdateMonitor = 250;
    }
    if ( totalNumbers > 5000 ) {
      thresholdValueUpdateMonitor = 500;
    }
    if ( totalNumbers > 10000 ) {
      thresholdValueUpdateMonitor = 1000;
    }
    if ( totalNumbers > 100000 ) {
      thresholdValueUpdateMonitor = 2500;
    }
    if ( totalNumbers > 500000 ) {
      thresholdValueUpdateMonitor = 5000;
    }
    DLog.debug( lctx , headerLog + "initialized threshold : update remains = "
        + thresholdValueUpdateRemains + " record(s) , update monitor = "
        + thresholdValueUpdateMonitor + " record(s)" );
  }

  private int workerBurstSize() {
    int burstSize = 1;
    if ( channelConf != null ) {
      burstSize = channelConf.getWorkerBurstSize();
    }
    if ( opropsApp != null ) {
      burstSize = (int) opropsApp.getLong( "Channel.Worker.BurstSize" ,
          burstSize );
    }
    return burstSize;
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
