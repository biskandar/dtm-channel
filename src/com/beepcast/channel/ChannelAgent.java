package com.beepcast.channel;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.billing.BillingApp;
import com.beepcast.billing.BillingResult;
import com.beepcast.billing.profile.PaymentType;
import com.beepcast.channel.common.ChannelSessionAlert;
import com.beepcast.channel.common.ChannelSessionReportSupport;
import com.beepcast.channel.xipme.XipmeBuffer;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.ClientLevelCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.idgen.IdGenApp;
import com.beepcast.loadmng.LoadManagement;
import com.beepcast.loadmng.LoadManagementApi;
import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.Node;
import com.beepcast.model.transaction.ProcessCode;
import com.beepcast.model.transaction.TransactionInputMessage;
import com.beepcast.model.transaction.TransactionMessageFactory;
import com.beepcast.model.transaction.TransactionMessageParam;
import com.beepcast.model.transaction.billing.AccountProfile;
import com.beepcast.model.util.DateTimeFormat;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.router.mt.SendBufferService;
import com.beepcast.subscriber.SubscriberApp;
import com.beepcast.subscriber.SubscriberGroupBean;
import com.beepcast.util.throttle.Throttle;
import com.beepcast.util.throttle.ThrottleProcess;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelAgent implements Runnable , ThrottleProcess {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelAgent" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean initialized;

  private OnlinePropertiesApp opropsApp;
  private IdGenApp idGenApp;
  private BillingApp billingApp;
  private LoadManagement loadMng;
  private SubscriberApp subscriberApp;

  private String headerLog;

  private ChannelApp channelApp;
  private ChannelConf channelConf;

  private ChannelSessionBean csBean;
  private int channelSessionId;
  private SubscriberGroupBean sgBean;
  private int subscriberGroupId;
  private TClient clientOut;
  private int clientId;
  private TEvent eventOut;
  private int eventId;
  private TClientLevel clientLevelOut;

  private ChannelMonitorService monitorService;

  private int paymentType;
  private double totalDebitAmountPerNumber;

  private long idxSubscribers;
  private long ctrSubscribers;
  private List listSubscribers;
  private Iterator iterSubscribers;

  private ChannelSessionService csService;
  private ChannelLogService clService;
  private ChannelSessionReportService csrService;
  private SendBufferService sendBufferService;

  private int thresholdValueUpdateRemains;
  private int thresholdValueUpdateMonitor;

  private XipmeBuffer xipmeBuffer;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelAgent( ChannelApp channelApp ,
      ChannelSessionBean channelSessionBean ) {
    initialized = false;

    opropsApp = OnlinePropertiesApp.getInstance();
    if ( opropsApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null online properties app" );
      return;
    }

    idGenApp = IdGenApp.getInstance();
    if ( idGenApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null id generator app" );
      return;
    }

    billingApp = BillingApp.getInstance();
    if ( billingApp == null ) {
      DLog.warning( lctx , "Failed to initialized , found null billing app" );
      return;
    }

    loadMng = LoadManagement.getInstance();
    if ( loadMng == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null load management app" );
      return;
    }

    subscriberApp = SubscriberApp.getInstance();
    if ( subscriberApp == null ) {
      DLog.warning( lctx , "Failed to initialized "
          + ", found null subscriber app" );
      return;
    }

    headerLog = "";

    this.channelApp = channelApp;
    if ( this.channelApp == null ) {
      DLog.warning( lctx , "Failed to initialized , found null channel app" );
      return;
    }
    channelConf = channelApp.getChannelConf();
    if ( channelConf == null ) {
      DLog.warning( lctx , "Failed to initialized , found null channel conf" );
      return;
    }

    // get channel session object and params
    csBean = channelSessionBean;
    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to initialized , found null ChannelSession" );
      return;
    }
    channelSessionId = csBean.getId();
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Can not start the channel agent "
          + ", found zero channelSessionId" );
      return;
    }

    // create header log
    headerLog = "[ChannelSession-" + channelSessionId + "] ";

    // get subscriber group object
    sgBean = subscriberApp.getSubscriberGroupBean( csBean
        .getGroupSubscriberId() );
    if ( sgBean == null ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
          + ", found null subscriber group profile" );
      return;
    }
    subscriberGroupId = sgBean.getId();
    if ( subscriberGroupId < 1 ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
          + ", found zero subscriberGroupId" );
      return;
    }

    // get client object and params
    clientOut = channelSessionBean.getClientOut();
    if ( clientOut == null ) {
      clientOut = ClientCommon.getClient( eventOut.getClientId() );
    }
    if ( clientOut == null ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
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
    eventOut = channelSessionBean.getEventOut();
    if ( eventOut == null ) {
      eventOut = EventCommon.getEvent( csBean.getEventId() );
    }
    if ( eventOut == null ) {
      DLog.warning( lctx , headerLog + "Can not start the channel agent "
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
    clientLevelOut = channelSessionBean.getClientLevelOut();
    if ( clientLevelOut == null ) {
      clientLevelOut = ClientLevelCommon.getClientLevel( clientOut
          .getClientLevelId() );
    }
    if ( clientLevelOut == null ) {
      DLog.warning( lctx , headerLog + "Found empty client level profile" );
    }

    // created channel monitor service
    monitorService = new ChannelMonitorService(
        channelApp.getChannelMonitorHolder() );
    DLog.debug( lctx , headerLog + "Created monitor service" );

    // get paymentType
    paymentType = clientOut.getPaymentType();

    // calculate total debit amount
    totalDebitAmountPerNumber = ChannelSessionReportSupport
        .calculateTotalDebitAmountForRemainNumbers( csBean.getId() ,
            csBean.isAllowDuplicated() , csBean.isUseDateSend() ,
            csBean.getGroupSubscriberId() , eventId , 1 );

    // log it
    DLog.debug(
        lctx ,
        headerLog + "Define eventId = " + eventId + " , eventName = "
            + StringEscapeUtils.escapeJava( eventOut.getEventName() )
            + " , clientId = " + clientId + " , companyName = "
            + StringEscapeUtils.escapeJava( clientOut.getCompanyName() )
            + " , paymentType = "
            + PaymentType.paymentTypeToString( paymentType )
            + " , totalDebitAmountPerNumber = " + totalDebitAmountPerNumber
            + " unit(s)" );

    // get temporary channel session last summary
    long totalNumbers = csBean.getTotalNumbers();
    long totalRemains = csBean.getTotalRemains();

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

    // update first monitor
    doMonitor( workerMaxLoad() , idxSubscribers );

    // create support object service
    csService = new ChannelSessionService();
    clService = new ChannelLogService();
    csrService = new ChannelSessionReportService();

    // create support object DAO
    sendBufferService = new SendBufferService();
    if ( channelConf.isDebug() ) {
      DLog.debug( lctx , headerLog + "Created object "
          + ": channel session dao , channel session report dao "
          + ", channel log dao , send buffer dao " );
    }

    // defined threshold value
    initThresholdRecords( totalNumbers );

    // setup xipme buffer
    xipmeBuffer = new XipmeBuffer( headerLog , eventId ,
        csBean.isUseDateSend() , totalNumbers );

    initialized = true;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {
    if ( !initialized ) {
      DLog.warning( lctx , headerLog + "Failed to started the broadcast "
          + ", not initialized properly" );
      return;
    }

    // trap delta time and log
    long deltaTime = System.currentTimeMillis();
    DLog.debug( lctx , headerLog + "Thread started" );

    // xipme buffer started
    DLog.debug( lctx , headerLog + "Xipme buffer started "
        + ", with : idxSubscribers = " + idxSubscribers );
    xipmeBuffer.start( idxSubscribers );

    // prepare the throttle params
    int burstSize = workerBurstSize();
    int maxLoad = workerMaxLoad();

    // iterate all record with throttle mechanism
    DLog.debug( lctx , headerLog + "Running the broadcast with burstSize = "
        + burstSize + " pipe(s) , maxLoad = " + maxLoad + " msg/sec" );
    Throttle throttle = new Throttle( burstSize , maxLoad );
    try {
      throttle.run( 1 , this );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to run the broadcast "
          + ", found throttle problem , " + e );
    }
    DLog.debug( lctx , headerLog + "Successfully run the broadcast" );

    // xipme buffer stop
    DLog.debug( lctx , headerLog + "Xipme buffer stopped" );
    xipmeBuffer.stop();

    // trap delta time and log
    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , headerLog + "Thread finished ( takes " + deltaTime
        + " ms )" );
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
        if ( verifyFinished() ) {
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "Found finished status of "
              + "channel object , stop the current pool process , take "
              + deltaTime + " ms" );
          return result;
        }

        // is the time for scheduled stop broadcast ?
        if ( verifyDateScheduledFinish() ) {
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "The current broadcast will "
              + "be stop here , found date schedule finished "
              + "is reached the expiry , take " + deltaTime + " ms" );
          return result;
        }

        // is the current status of channel suspended ?
        if ( verifySuspended() ) {
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "Found suspended status of "
              + "channel object , stop the current pool process , take "
              + deltaTime + " ms" );
          return result;
        }

      } catch ( Exception e ) {
        // update total remains and refresh report
        updateTotalRemainsAndRefreshReport();
        // unreserve client balance if found any
        ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
        // end monitor here
        endMonitor();
        // trap delta time and log
        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.warning( lctx , headerLog + "Failed to verify "
            + "channel session was finished or suspended state , take "
            + deltaTime + " ms , " + e );
        return result;
      }

      // validate and load subscriber phone number(s)
      try {

        // load list subscribers
        listSubscribers = clService.getMarkedChannelLogBeans( eventId , csBean
            .isUseDateSend() , (int) opropsApp.getLong(
            "Channel.Worker.MaxBatchRecords" ,
            channelConf.getWorkerMaxBatchRecords() ) );

        // is it finished already ?
        if ( ( listSubscribers == null ) || ( listSubscribers.size() < 1 ) ) {

          if ( csBean.isUseDateSend() ) {
            // get total number remains from db
            long totalNumberRemains = clService.getTotalRemains( eventId );
            if ( totalNumberRemains > 0 ) {
              // update monitor first
              doMonitor( workerMaxLoad() , idxSubscribers );
              // log and pending
              DLog.debug( lctx , headerLog + "Found empty scheduled record(s) "
                  + "but there are " + totalNumberRemains
                  + " record(s) remains , pending the broadcast for a while." );
              Thread.sleep( 60000 );
              // go loop !
              result = false;
              return result;
            }
          }

          // there is no remain numbers , updated to the channel session bean
          DLog.debug( lctx ,
              headerLog + "Found all subscriber(s) are "
                  + "processed already , updated : csBean.totalRemains : "
                  + csBean.getTotalRemains() + " -> 0 , csBean.debitUnits : "
                  + csBean.getDebitUnits() + " -> 0.0 " );
          csBean.setTotalRemains( 0 );
          csBean.setDebitUnits( 0.0 );

          // trap delta time and log it
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.debug( lctx , headerLog + "Found no more available records "
              + "to be process , takes ( " + deltaTime + " ms ) " );

          // start the channel agent finisher
          DLog.debug( lctx , headerLog
              + "Started the thread channel agent finisher" );
          ChannelAgentFinisher channelAgentFinisher = new ChannelAgentFinisher(
              channelApp , channelConf , csBean , sgBean , eventOut ,
              clientOut , clientLevelOut );
          Thread threadFinisher = new Thread( channelAgentFinisher );
          threadFinisher.start();

          // stop here
          return result;
        }

        // refresh last total remains
        {
          long totalNumberRemains = clService.getTotalRemains( eventId );
          boolean updated = csService.updateTotalRemains( channelSessionId ,
              totalNumberRemains );
          DLog.debug( lctx , headerLog
              + ( updated ? "Updated " : "Failed to update " )
              + "total number remains = " + totalNumberRemains + " number(s)" );

          long delay = xipmeBuffer.updateRecordOffset( totalNumberRemains );
          if ( delay > 0 ) {
            DLog.warning( lctx , "Need sometime to update xipme buffer "
                + "record offset and fill up the one , delay " + delay + " ms" );
            Thread.sleep( delay );
          }
        }

        // generate the iterator object
        iterSubscribers = listSubscribers.iterator();
        if ( iterSubscribers == null ) {
          // unreserve client balance if found any
          ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
          // end monitor here
          endMonitor();
          // trap delta time and log
          deltaTime = System.currentTimeMillis() - deltaTime;
          DLog.warning( lctx , headerLog + "Can not start the channel agent "
              + ", failed to generate iterator of phone number(s) , takes "
              + deltaTime + " ms" );
          return result;
        }

        // log to inform ready to process all available subscribers
        DLog.debug( lctx , headerLog + "Found a new list of subscriber(s) "
            + "ready to process , reset the counter to begin" );

      } catch ( Exception e ) {
        // unreserve client balance if found any
        ChannelSession.unreserveBalance( clientOut , eventOut , csBean );
        // end monitor here
        endMonitor();
        // trap delta time and log
        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.warning( lctx , headerLog + "Failed to read and compose "
            + "list of subscribers , take " + deltaTime + " ms , " + e );
        return result;
      }

    } // when listSubscribers is empty

    // set default as looping
    result = false;

    // perform delay if send buffer reached threshold
    if ( performDelayWhenMaxTotalSendBufferReached() ) {
      return result;
    }

    // validate is the vector of subscriber empty ? than restart the loop
    if ( !iterSubscribers.hasNext() ) {
      listSubscribers = null;
      DLog.debug( lctx , headerLog + "Finished process all subscriber(s) "
          + "data in the list , creating the next list "
          + "of subscriber number(s)" );
      return result;
    }

    // validate prepaid balance if found any total debit amount per message
    if ( ( paymentType == PaymentType.PREPAID )
        && ( totalDebitAmountPerNumber > 0 ) ) {
      Double balance = null;
      BillingResult billingResult = billingApp.getBalance(
          AccountProfile.EVENT_PREPAID , new Integer( eventId ) );
      if ( billingResult != null ) {
        balance = billingResult.getBalanceAfter();
      }
      if ( balance == null ) {
        listSubscribers = null;
        csService.updateSuspendedChannelSession( csBean );
        DLog.warning( lctx , headerLog + "Suspend the running broadcast "
            + ", failed to get event prepaid balance" );
        ChannelSessionAlert.sendAlertStopBroadcast( csBean , sgBean ,
            clientOut , eventOut , "Found failed to get event prepaid balance" );
        return result;
      } else if ( balance.doubleValue() < 1 ) {
        listSubscribers = null;
        csService.updateSuspendedChannelSession( csBean );
        DLog.warning( lctx , headerLog + "Suspend the running broadcast "
            + ", found there is no event prepaid balance left" );
        ChannelSessionAlert.sendAlertStopBroadcast( csBean , sgBean ,
            clientOut , eventOut ,
            "Found there is no event prepaid balance left" );
        return result;
      }
    }

    // verify threshold and update monitor
    if ( ( idxSubscribers % thresholdValueUpdateMonitor ) == 0 ) {
      int maxLoad = workerMaxLoad();
      doMonitor( maxLoad , idxSubscribers );
    }

    // prepared the next channel log profile record
    ChannelLogBean channelLogBean = (ChannelLogBean) iterSubscribers.next();

    // process per channel log bean
    int processCode = process( channelLogBean );

    // when found unsuceed process code than stop the broadcast
    if ( processCode != ProcessCode.PROCESS_SUCCEED ) {
      listSubscribers = null;
      csService.updateSuspendedChannelSession( csBean );
      DLog.warning( lctx , headerLog + "Stop process channel log "
          + ", found result process code is not succeed" );
      ChannelSessionAlert.sendAlertStopBroadcast( csBean , sgBean , clientOut ,
          eventOut , "Found failed result to broadcast the message" );
      return result;
    }

    // calculate latency time
    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it
    DLog.debug(
        lctx ,
        headerLog + "Processed channel log , for phone = "
            + channelLogBean.getPhone() + " , channelCode = "
            + eventOut.getCodes() + " , dateSend = "
            + DateTimeFormat.convertToString( channelLogBean.getDateSend() )
            + " , resultCode = "
            + ProcessCode.processCodeToString( processCode ) + " , take = "
            + deltaTime + " ms" );

    return result;
  }

  public int process( ChannelLogBean channelLogBean ) {
    int processCode = ProcessCode.PROCESS_SUCCEED;
    try {

      // track the number of channel log traffic counter
      idxSubscribers = idxSubscribers + 1;
      ctrSubscribers = ctrSubscribers + 1;

      // validate must be parameter
      if ( channelLogBean == null ) {
        DLog.warning( lctx , headerLog + "Failed to process channel log "
            + ", found empty channel log bean" );
        return processCode;
      }
      if ( StringUtils.isBlank( channelLogBean.getPhone() ) ) {
        DLog.warning( lctx , headerLog + "Failed to process channel log "
            + ", found empty phoneNumber" );
        return processCode;
      }
      if ( StringUtils.isBlank( eventOut.getCodes() ) ) {
        DLog.warning( lctx , headerLog + "Failed to process channel log "
            + ", found empty channelCode" );
        return processCode;
      }

      // log load management
      loadMng.hit( LoadManagementApi.HDRPROF_CHANNEL ,
          LoadManagementApi.CONTYPE_SMSMT , ( "EVENT-" + eventId ) , 1 , true ,
          false );

      // prepare xipme link if any
      long deltaTime = System.currentTimeMillis();
      Map xipmeCodesMap = xipmeBuffer.readCodes( channelLogBean.getId() );
      deltaTime = System.currentTimeMillis() - deltaTime;
      if ( xipmeCodesMap != null ) {
        DLog.debug( lctx , headerLog + "Read xipme codes from xipme buffer "
            + ": codes = " + xipmeCodesMap + " , takes " + deltaTime + " ms" );
      }

      // prepare transaction input message
      TransactionInputMessage imsg = TransactionMessageFactory
          .createInputMessage( newMessageId() , MessageType.TEXT_TYPE ,
              eventOut.getCodes() , Node.DTM , channelLogBean.getPhone() ,
              null , null , channelSessionId , channelConf.getMsgPriority() );

      // transaction header log
      String headerLogInMsg = "[Msg-".concat( imsg.getMessageId() ).concat(
          "] " );

      // add message params
      DLog.debug(
          lctx ,
          headerLog + headerLogInMsg + "Add message param "
              + TransactionMessageParam.HDR_CHANNEL_LOG_BEAN + " : id = "
              + channelLogBean.getId() + " , csId = "
              + channelLogBean.getClientSubscriberId() + " , cscId = "
              + channelLogBean.getClientSubscriberCustomId() + " , evId = "
              + channelLogBean.getEventId() + " , phone = "
              + channelLogBean.getPhone() + " , dateSend = "
              + DateTimeFormat.convertToString( channelLogBean.getDateSend() ) );
      imsg.addMessageParam( TransactionMessageParam.HDR_CHANNEL_LOG_BEAN ,
          channelLogBean );
      DLog.debug( lctx , headerLog + headerLogInMsg + "Add message param "
          + TransactionMessageParam.HDR_BYPASS_SUSPENDED_EVENT + " : True" );
      imsg.addMessageParam( TransactionMessageParam.HDR_BYPASS_SUSPENDED_EVENT ,
          "True" );
      if ( xipmeCodesMap != null ) {
        DLog.debug( lctx , headerLog + headerLogInMsg + "Add message param "
            + TransactionMessageParam.HDR_XIPME_CODES_MAP + " : "
            + xipmeCodesMap );
        imsg.addMessageParam( TransactionMessageParam.HDR_XIPME_CODES_MAP ,
            xipmeCodesMap );
      }

      // execute transaction processor
      processCode = channelApp.executeChannelProcessor( imsg );

      // update flag unmarked
      if ( !clService.updateSubscriberUnmarked( channelLogBean.getId() ,
          imsg.getMessageId() ) ) {
        DLog.warning( lctx , headerLog + headerLogInMsg
            + "Failed to update unmarked subscriber from the channel log" );
      } else {
        DLog.debug( lctx , headerLog + headerLogInMsg
            + "Updated unmarked subscriber from the channel log" );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to process channel log , " + e );
      processCode = ProcessCode.PROCESS_ERROR;
      return processCode;
    }

    // nothing to do here ...

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

  private boolean verifyDateScheduledFinish() {
    boolean result = false;

    // validate is has scheduled finished now ?
    Date dateNow = new Date();
    Date dateScheduledFinish = csBean.getDateSchedulledFinish();
    if ( ( dateScheduledFinish == null )
        || ( dateScheduledFinish.getTime() >= dateNow.getTime() ) ) {
      return result;
    }

    // log first
    DLog.debug( lctx , headerLog + "Reached the schedule stop date "
        + DateTimeFormat.convertToString( dateScheduledFinish ) );

    // set suspend the broadcast
    if ( !csService.updateSuspendedChannelSession( csBean ) ) {
      DLog.warning( lctx , headerLog + "Failed to suspend "
          + "the running broadcast" );
    }
    DLog.debug( lctx , headerLog + "Suspended the running broadcast" );

    // update total remains and refresh report
    updateTotalRemainsAndRefreshReport();

    // unreserve client balance
    ChannelSession.unreserveBalance( clientOut , eventOut , csBean );

    // setup next schedule broadcast
    if ( ChannelSession.nextSchedule( clientOut , eventOut , csBean ) ) {
      DLog.debug( lctx , headerLog + "Successfully setup next "
          + "scheduled broadcast " );
    } else {
      DLog.debug( lctx , headerLog + "Defined this as the last "
          + "scheduled broadcast " );
    }

    // end monitor here
    endMonitor();

    result = true;
    return result;
  }

  private boolean verifySuspended() {
    boolean result = false;

    // validate is has suspended ?
    if ( !csService.isSuspended( channelSessionId ) ) {
      return result;
    }

    // log first
    DLog.debug( lctx , headerLog + "Found status suspended on the "
        + "current running broadcast " );

    // update total remains and refresh report
    updateTotalRemainsAndRefreshReport();

    // unreserve client balance if found any
    ChannelSession.unreserveBalance( clientOut , eventOut , csBean );

    // end monitor here
    endMonitor();

    result = true;
    return result;
  }

  private boolean verifyFinished() {
    boolean result = false;

    // validate is has finished ?
    if ( !csService.isFinished( channelSessionId ) ) {
      return result;
    }

    // log first
    DLog.debug( lctx , headerLog + "Found status finished on the "
        + "current running broadcast " );

    // update total remains and refresh report
    updateTotalRemainsAndRefreshReport();

    // unreserve the client balance if found any
    ChannelSession.unreserveBalance( clientOut , eventOut , csBean );

    // end monitor here
    endMonitor();

    result = true;
    return result;
  }

  private boolean updateTotalRemainsAndRefreshReport() {
    boolean result = false;
    try {

      // update total remains
      long totalNumberRemains = clService.getTotalRemains( eventId );
      double totalDebitRemains = clService
          .getTotalCreditCostForRemainsSubscriber( channelSessionId , eventId ,
              clientId , false );
      DLog.debug( lctx , headerLog + "Updating total number remains = "
          + totalNumberRemains + " number(s) and total debit remains = "
          + totalDebitRemains + " unit(s) from the broadcast table" );
      csService.updateTotalRemainsAndDebitUnits( channelSessionId ,
          totalNumberRemains , totalDebitRemains );

      // refresh the broadcast report
      csrService.updateDateInserted( channelSessionId , null );

      result = true;
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to process update total "
          + "remains and refresh report , " + e );
    }
    return result;
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

  private boolean performDelayWhenMaxTotalSendBufferReached() {
    boolean result = false;
    long workerSleepTime = workerSleepTime();
    long maxTotalSendBuffer = workerMaxSendBufferRecords();
    long curTotalSendBuffer = getTotalSendBuffer();
    if ( curTotalSendBuffer >= maxTotalSendBuffer ) {
      DLog.warning( lctx , headerLog + "Found total SendBuffer reached "
          + curTotalSendBuffer + " , hold for awhile ( delay "
          + workerSleepTime + " ms ) " );
      try {
        Thread.sleep( workerSleepTime );
        result = true;
      } catch ( InterruptedException e ) {
      }
    }
    return result;
  }

  private long getTotalSendBuffer() {
    long result = 0;
    if ( !initialized ) {
      DLog.warning( lctx , "Failed to get total send buffer "
          + ", not yet initialized" );
      return result;
    }
    result = sendBufferService
        .totalRecordsByChannelSessionId( channelSessionId );
    return result;
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

  private long workerMaxSendBufferRecords() {
    long maxRecords = 1000;
    if ( channelConf != null ) {
      maxRecords = channelConf.getWorkerMaxSendBufferRecords();
    }
    if ( opropsApp != null ) {
      maxRecords = opropsApp.getLong( "Channel.Worker.MaxSendBufferRecords" ,
          maxRecords );
    }
    return maxRecords;
  }

  private long workerSleepTime() {
    long sleepTime = 5000;
    if ( channelConf != null ) {
      sleepTime = channelConf.getWorkerSleepTime();
    }
    if ( opropsApp != null ) {
      sleepTime = opropsApp.getLong( "Channel.Worker.SleepTime" , sleepTime );
    }
    return sleepTime;
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

}
