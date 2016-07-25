package com.beepcast.channel.common;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.beepcast.api.provider.ProviderCommon;
import com.beepcast.channel.ChannelSessionBean;
import com.beepcast.channel.view.ChannelSessionInfoBean;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.ClientLevelCommon;
import com.beepcast.dbmanager.common.CountryCommon;
import com.beepcast.dbmanager.table.QClientLevel;
import com.beepcast.dbmanager.table.TClientLevel;
import com.beepcast.dbmanager.table.TCountry;
import com.beepcast.dbmanager.table.TProvider;
import com.beepcast.model.event.EventBean;
import com.beepcast.model.event.EventService;
import com.beepcast.model.event.ProcessBean;
import com.beepcast.model.event.ProcessCommon;
import com.beepcast.model.transaction.MessageType;
import com.beepcast.model.transaction.TransactionProcessBeanUtils;
import com.beepcast.model.transaction.TransactionUtil;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.beepcast.smsenc.impl.WapPush;
import com.beepcast.subscriber.SubscriberApp;
import com.beepcast.subscriber.SubscriberGroupCommon;
import com.beepcast.subscriber.SubscriberGroupReportService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionReportSupport {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionReportSupport" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static TClientLevel getClientLevelBean( ChannelSessionInfoBean csiBean ) {
    TClientLevel clientLevelBean = null;

    if ( csiBean == null ) {
      DLog.warning( lctx , "Failed to get client level bean "
          + ", found null channel session info bean" );
      return clientLevelBean;
    }

    String headerLog = ChannelSessionLogSupport.headerLog( csiBean );

    QClientLevel qClientLevelBean = ClientCommon.getClientLevel( csiBean
        .getClientId() );
    if ( qClientLevelBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to get client level bean "
          + " , found null map q client level" );
      return clientLevelBean;
    }

    clientLevelBean = ClientLevelCommon.getClientLevel( qClientLevelBean
        .getClientLevelId() );

    return clientLevelBean;
  }

  public static double calculateTotalDebitAmount( ChannelSessionBean csBean ) {
    double totalDebitAmount = 0;
    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to calculate total debit amount "
          + ", found null channel session bean" );
      return totalDebitAmount;
    }
    totalDebitAmount = calculateTotalDebitAmount( csBean.getId() ,
        csBean.isAllowDuplicated() , csBean.isUseDateSend() ,
        csBean.getGroupSubscriberId() , csBean.getEventId() ,
        csBean.getTotalNumbers() , csBean.getTotalRemains() ,
        csBean.getDebitUnits() , csBean.getExpectResponse() );
    return totalDebitAmount;
  }

  public static double calculateTotalDebitAmount( ChannelSessionInfoBean csiBean ) {
    double totalDebitAmount = 0;
    if ( csiBean == null ) {
      DLog.warning( lctx , "Failed to calculate total debit amount "
          + ", found null channel session info bean" );
      return totalDebitAmount;
    }
    totalDebitAmount = calculateTotalDebitAmount(
        csiBean.getChannelSessionId() , csiBean.isAllowDuplicated() ,
        csiBean.isUseDateSend() , csiBean.getGroupSubscriberId() ,
        csiBean.getEventId() , csiBean.getTotalNumbers() ,
        csiBean.getTotalRemains() , csiBean.getDebitUnits() ,
        csiBean.getExpectResponse() );
    return totalDebitAmount;
  }

  public static double calculateTotalDebitAmountForRemainNumbers(
      int channelSessionId , boolean allowDuplicated , boolean useDateSend ,
      int groupSubscriberId , int eventId , long totalRemainNumbers ) {
    double totalDebitAmount = 0;

    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    if ( groupSubscriberId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero group subscriber id" );
      return totalDebitAmount;
    }
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero event id" );
      return totalDebitAmount;
    }

    // append header log
    headerLog += "[List-" + groupSubscriberId + "] ";
    headerLog += "[Event-" + eventId + "] ";

    // get event profile
    EventBean eventBean = null;
    try {
      EventService eventService = new EventService();
      eventBean = eventService.select( eventId );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found invalid event bean , " + e );
      return totalDebitAmount;
    }
    if ( eventBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found unmatched event bean for event id = "
          + eventId );
      return totalDebitAmount;
    }
    DLog.debug( lctx ,
        headerLog + "Defined event : id = " + eventBean.getEventID()
            + " , name = " + eventBean.getEventName() );

    // get client profile
    int clientId = (int) eventBean.getClientID();
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found invalid client profile id" );
      return totalDebitAmount;
    }

    // read the first event's process profile
    ProcessBean processBean = ProcessCommon.getFirstProcessBean( eventBean );
    if ( processBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to calculate total debit "
          + "amount , found empty first event's process profile" );
      return totalDebitAmount;
    }

    // read process's response from the first event's response
    String processResponse = processBean.getResponse();
    if ( StringUtils.isBlank( processResponse ) ) {
      DLog.warning( lctx , headerLog + "Failed to calculate total debit "
          + "amount , found blank first event's process's response" );
      return totalDebitAmount;
    }

    // resolve message type
    int messageType = resolveMessageType( processBean.getType() ,
        processResponse );
    if ( messageType == MessageType.UNKNOWN_TYPE ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found unknown message type" );
      return totalDebitAmount;
    }

    // calculate total message send
    int totalMessageSend = calculateTotalMessageSend( messageType ,
        processResponse );
    if ( totalMessageSend < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero total message send" );
      return totalDebitAmount;
    }

    // calculate based credit cost
    double baseCreditCost = calculateBaseCreditCost( messageType );

    // log it
    DLog.debug(
        lctx ,
        headerLog + "Resolved : processType = " + processBean.getType()
            + " , processResponse.length = " + processResponse.length()
            + " , messageType = "
            + MessageType.messageTypeToString( messageType )
            + " , totalMessageSend = " + totalMessageSend + " msg(s)"
            + " , baseCreditCost = " + baseCreditCost + " unit(s)" );

    // calculate total debit amount
    double totalDebitAmountFromNumber = 1.0; // assume all countries ~ 1.0
    double totalDebitAmountFromNumbers = totalDebitAmountFromNumber
        * totalRemainNumbers;
    totalDebitAmount = baseCreditCost * totalDebitAmountFromNumbers
        * totalMessageSend;

    return totalDebitAmount;
  }

  public static double calculateTotalDebitAmount( int channelSessionId ,
      boolean allowDuplicated , boolean useDateSend , int groupSubscriberId ,
      int eventId , long totalNumbers , long totalRemains ,
      double csDebitUnits , double csExpectResponse ) {
    double totalDebitAmount = 0;

    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    if ( groupSubscriberId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero group subscriber id" );
      return totalDebitAmount;
    }
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero event id" );
      return totalDebitAmount;
    }

    // append header log
    headerLog += "[List-" + groupSubscriberId + "] ";
    headerLog += "[Event-" + eventId + "] ";

    // get event profile
    EventBean eventBean = null;
    try {
      EventService eventService = new EventService();
      eventBean = eventService.select( eventId );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found invalid event bean , " + e );
      return totalDebitAmount;
    }
    if ( eventBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found unmatched event bean for event id = "
          + eventId );
      return totalDebitAmount;
    }
    DLog.debug( lctx ,
        headerLog + "Defined event : id = " + eventBean.getEventID()
            + " , name = " + eventBean.getEventName() );

    // get client profile
    int clientId = (int) eventBean.getClientID();
    if ( clientId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found invalid client profile id" );
      return totalDebitAmount;
    }

    // read the first event's process profile
    ProcessBean processBean = ProcessCommon.getFirstProcessBean( eventBean );
    if ( processBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to calculate total debit "
          + "amount , found empty first event's process profile" );
      return totalDebitAmount;
    }

    // read process's response from the first event's response
    String processResponse = processBean.getResponse();
    if ( StringUtils.isBlank( processResponse ) ) {
      DLog.warning( lctx , headerLog + "Failed to calculate total debit "
          + "amount , found blank first event's process's response" );
      return totalDebitAmount;
    }

    // resolve message type
    int messageType = resolveMessageType( processBean.getType() ,
        processResponse );
    if ( messageType == MessageType.UNKNOWN_TYPE ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found unknown message type" );
      return totalDebitAmount;
    }

    // calculate total message send
    int totalMessageSend = calculateTotalMessageSend( messageType ,
        processResponse );
    if ( totalMessageSend < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to calculate "
          + "total debit amount , found zero total message send" );
      return totalDebitAmount;
    }

    // calculate based credit cost
    double baseCreditCost = calculateBaseCreditCost( messageType );

    // log it
    DLog.debug(
        lctx ,
        headerLog + "Resolved : processType = " + processBean.getType()
            + " , processResponse.length = " + processResponse.length()
            + " , messageType = "
            + MessageType.messageTypeToString( messageType )
            + " , totalMessageSend = " + totalMessageSend + " msg(s)"
            + " , baseCreditCost = " + baseCreditCost + " unit(s)" );

    // calculate total debit amount based on total number and/or remains
    if ( totalNumbers == totalRemains ) {
      // when the broadcast haven't start
      // will calculate from subscriber service
      totalDebitAmount = calculateTotalDebitAmountFromSubscribedNumbers(
          baseCreditCost , clientId , groupSubscriberId , allowDuplicated ,
          useDateSend , false );
      DLog.debug( lctx , headerLog + "Calculated debitAmountPerMessage = "
          + totalDebitAmount );
      totalDebitAmount = totalDebitAmount * totalMessageSend;
    } else {
      // when the broadcast was ever started
      // will get from channel session table
      DLog.debug( lctx , headerLog + "The broadcast is started before "
          + ", read total debit amount from the channel session table." );
      totalDebitAmount = csDebitUnits;
    }

    // add expect response debit amount
    double exctRespDebitAmount = ( csExpectResponse / 100.0 )
        * totalDebitAmount;
    DLog.debug( lctx , headerLog + "Calculated total debit amount units : "
        + totalDebitAmount + " + " + exctRespDebitAmount );

    // calculate total debit amount
    totalDebitAmount = totalDebitAmount + exctRespDebitAmount;
    return totalDebitAmount;
  }

  public static double calculateTotalDebitAmountFromSubscribedNumbers(
      double baseCreditCost , int clientId , int groupSubscriberId ,
      boolean allowDuplicated , boolean useDateSend , boolean debug ) {
    double totalDebitAmount = 0;

    if ( clientId < 1 ) {
      DLog.warning( lctx , "Failed to calculate total debit amount "
          + "from subscriber , found null client id" );
      return totalDebitAmount;
    }

    if ( groupSubscriberId < 1 ) {
      DLog.warning( lctx , "Failed to calculate total debit amount "
          + "from subscriber , found null group subscriber id" );
      return totalDebitAmount;
    }

    String headerLog = SubscriberGroupCommon.headerLog( groupSubscriberId );

    DLog.debug( lctx , headerLog + "Calculating total debit amount from "
        + "subscribed numbers , with : clientId = " + clientId
        + " , groupSubscriberId = " + groupSubscriberId
        + " , allowDuplicated = " + allowDuplicated + " , useDateSend = "
        + useDateSend );

    SubscriberApp subscriberApp = SubscriberApp.getInstance();
    if ( subscriberApp == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to calculate total debit amount "
          + "from subscriber , found null subscriber app" );
      return totalDebitAmount;
    }

    SubscriberGroupReportService sgrService = subscriberApp
        .getSubscriberGroupReportService();
    if ( sgrService == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to calculate total debit amount "
          + "from subscriber , found null subscriber group report service" );
      return totalDebitAmount;
    }

    Map map = null;
    if ( allowDuplicated ) {
      if ( useDateSend ) {
        DLog.debug( lctx , headerLog + "Getting map country code total "
            + "subscribed scheduled" );
        map = sgrService
            .getMapCountryCodeTotalSubscribedScheduled( groupSubscriberId );
      } else {
        DLog.debug( lctx , headerLog + "Getting map country code total "
            + "subscribed numbers" );
        map = sgrService
            .getMapCountryCodeTotalSubscribedNumbers( groupSubscriberId );
      }
    } else {
      if ( useDateSend ) {
        DLog.debug( lctx , headerLog + "Getting map country code total "
            + "subscribed unique scheduled" );
        map = sgrService
            .getMapCountryCodeTotalSubscribedUniqueScheduled( groupSubscriberId );
      } else {
        DLog.debug( lctx , headerLog + "Getting map country code total "
            + "subscribed unique numbers" );
        map = sgrService
            .getMapCountryCodeTotalSubscribedUniqueNumbers( groupSubscriberId );
      }
    }
    if ( map == null ) {
      DLog.warning( lctx , headerLog + "Failed to calculate total debit "
          + "amount , found null map country code total subscribed numbers" );
      return totalDebitAmount;
    }

    StringBuffer sbLog = null;

    Set setKey = map.keySet();
    Iterator iterKey = setKey.iterator();
    while ( iterKey.hasNext() ) {
      String countryCode = (String) iterKey.next();
      if ( StringUtils.isBlank( countryCode ) ) {
        continue;
      }
      Integer totalNumbers = (Integer) map.get( countryCode );
      if ( totalNumbers == null ) {
        continue;
      }

      // formula debit amount
      double totalDebitAmountFromNumber = calculateTotalDebitAmountFromNumber(
          countryCode , clientId , debug );
      double debitAmount = baseCreditCost * totalDebitAmountFromNumber
          * totalNumbers.intValue();

      // log it
      if ( sbLog == null ) {
        sbLog = new StringBuffer();
      } else {
        sbLog.append( "; " );
      }
      sbLog.append( countryCode );
      sbLog.append( ":" );
      sbLog.append( debitAmount );
      sbLog.append( "=" );
      sbLog.append( baseCreditCost );
      sbLog.append( "*" );
      sbLog.append( totalDebitAmountFromNumber );
      sbLog.append( "*" );
      sbLog.append( totalNumbers.intValue() );

      totalDebitAmount = totalDebitAmount + debitAmount;
    } // while ( iterKey.hasNext() ) {

    if ( sbLog != null ) {
      DLog.debug( lctx , headerLog + "Calculated total debit amount "
          + "from subscribed numbers = " + totalDebitAmount + " unit(s) : "
          + sbLog.toString() );
    }

    return totalDebitAmount;
  }

  public static double calculateTotalDebitAmountFromNumber( String countryCode ,
      int clientId , boolean debug ) {
    double totalDebitAmount = 0;

    // find default credit cost
    OnlinePropertiesApp opropsApp = OnlinePropertiesApp.getInstance();
    if ( opropsApp != null ) {
      totalDebitAmount = opropsApp
          .getDouble( "CountryProfile.CreditCost" , 0.0 );
      if ( debug ) {
        DLog.debug( lctx , "Defined total credit cost "
            + "from global properties : " + totalDebitAmount + " unit(s)" );
      }
    }

    // find country credit cost
    TCountry countryBean = null;
    if ( !StringUtils.isBlank( countryCode ) ) {
      countryBean = CountryCommon.getCountryByCode( countryCode );
      if ( countryBean != null ) {
        totalDebitAmount = countryBean.getCreditCost();
        if ( debug ) {
          DLog.debug( lctx , "Defined total credit cost from country code "
              + countryCode + " : " + totalDebitAmount + " unit(s)" );
        }
      } else {
        DLog.warning( lctx , "Failed to calculate total debit amount "
            + "from number , found null country bean , using default value" );
      }
    } else {
      DLog.warning( lctx , "Failed to calculate total debit amount "
          + "from number , found blank country code , using default value" );
    }

    // calculate final total debit amount
    if ( debug ) {
      DLog.debug( lctx , "Define total debit amount per number = "
          + totalDebitAmount + " unit(s)" );
    }

    return totalDebitAmount;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static int resolveMessageType( String processType ,
      String processResponse ) {
    int messageType = MessageType.TEXT_TYPE;

    if ( StringUtils.equalsIgnoreCase( processType ,
        TransactionProcessBeanUtils.PROCESS_TYPE_CREATE_QR_IMAGE ) ) {
      messageType = MessageType.QRPNG_TYPE;
      return messageType;
    }

    if ( processResponse.startsWith( "WAP_PUSH=" ) ) {
      messageType = MessageType.WAPPUSH_TYPE;
      return messageType;
    }

    if ( TransactionUtil.isUnicodeMessage( processResponse ) ) {
      messageType = MessageType.UNICODE_TYPE;
      return messageType;
    }

    return messageType;
  }

  private static int calculateTotalMessageSend( int messageType ,
      String processResponse ) {
    int totalMessageSend = 0;

    // calculate total message send based on message type
    switch ( messageType ) {
    case MessageType.TEXT_TYPE :
    case MessageType.RINGTONE_TYPE :
    case MessageType.PICTURE_TYPE :
    case MessageType.UNICODE_TYPE :
      totalMessageSend = TransactionUtil.calculateTotalSms( messageType ,
          processResponse );
      break;
    case MessageType.WAPPUSH_TYPE :
      totalMessageSend = StringUtils.split( processResponse ,
          WapPush.DELIMITER_PDU_PDU ).length;
      break;
    case MessageType.MMS_TYPE :
      totalMessageSend = 1;
      break;
    case MessageType.QRPNG_TYPE :
    case MessageType.QRGIF_TYPE :
    case MessageType.QRJPG_TYPE :
      totalMessageSend = 1;
      break;
    }

    return totalMessageSend;
  }

  private static double calculateBaseCreditCost( int messageType ) {
    double baseCreditCost = 0.0;

    // calculate base credit cost based on message type
    switch ( messageType ) {
    case MessageType.TEXT_TYPE :
    case MessageType.RINGTONE_TYPE :
    case MessageType.PICTURE_TYPE :
    case MessageType.UNICODE_TYPE :
    case MessageType.WAPPUSH_TYPE :
      baseCreditCost = 1.0;
      break;
    case MessageType.MMS_TYPE :
      baseCreditCost = 1.0;
      break;
    case MessageType.QRPNG_TYPE :
    case MessageType.QRGIF_TYPE :
    case MessageType.QRJPG_TYPE :
      TProvider providerOut = ProviderCommon.getProvider( "QR" );
      if ( providerOut != null ) {
        baseCreditCost = providerOut.getOuCreditCost();
      }
      break;
    }

    return baseCreditCost;
  }

}
