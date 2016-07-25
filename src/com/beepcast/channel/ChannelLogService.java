package com.beepcast.channel;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.dbmanager.common.BandCommon;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.CountryCommon;
import com.beepcast.dbmanager.table.QClientBand;
import com.beepcast.dbmanager.table.TBand;
import com.beepcast.dbmanager.table.TCountry;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelLogService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelLogService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;
  private ChannelApp app;
  private ChannelLogDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogService() {
    opropsApp = OnlinePropertiesApp.getInstance();
    app = ChannelApp.getInstance();
    dao = new ChannelLogDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insertChannelLogBean( ChannelLogBean clBean ) {
    boolean result = false;
    if ( clBean == null ) {
      DLog.warning( lctx , "Failed to insert channel log bean "
          + ", found null bean" );
      return result;
    }
    result = dao.insertChannelLogBean( clBean );
    return result;
  }

  public long updateSubscriberCountryProfile( int eventId , int countryId ) {
    long totalRecords = 0;

    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to update subscriber country profile "
          + ", found null event id" );
      return totalRecords;
    }

    if ( countryId < 1 ) {
      DLog.warning( lctx , "Failed to update subscriber country profile "
          + ", found blank country id" );
      return totalRecords;
    }

    TCountry countryBean = CountryCommon.getCountry( countryId );
    if ( countryBean == null ) {
      DLog.warning( lctx , "Failed to update subscriber country profile "
          + ", found null country bean" );
      return totalRecords;
    }
    if ( !countryBean.isActive() ) {
      DLog.warning( lctx , "Failed to update subscriber country profile "
          + ", found inactive country bean : " + countryBean.getCode() );
      return totalRecords;
    }

    String countryPhonePrefix = countryBean.getPhonePrefix();
    if ( StringUtils.isBlank( countryPhonePrefix ) ) {
      DLog.warning( lctx , "Failed to update subscriber country profile "
          + ", found blank country phone prefix" );
      return totalRecords;
    }
    if ( !countryPhonePrefix.startsWith( "+" ) ) {
      countryPhonePrefix = "+" + countryPhonePrefix;
    }

    String countryCode = StringUtils.trimToEmpty( countryBean.getCode() );
    double countryCreditCost = countryBean.getCreditCost();

    totalRecords = dao.updateSubscriberCountryProfile( eventId ,
        countryPhonePrefix , countryCode , countryCreditCost );

    return totalRecords;
  }

  public long updateSubscriberMarked( int eventId ) {
    long totalUpdated = 0;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to update subscriber marked "
          + ", found zero event id" );
      return totalUpdated;
    }
    totalUpdated = dao.updateSubscriberMarked( eventId );
    return totalUpdated;
  }

  public boolean updateSubscriberUnmarked( int id , String messageId ) {
    boolean updated = false;
    if ( id < 1 ) {
      DLog.warning( lctx , "Failed to update subscriber unmarked "
          + ", found zero channel log id" );
      return updated;
    }
    updated = dao.updateSubscriberUnmarked( id , messageId );
    return updated;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogBean getChannelLogBean( int eventId , String phoneNumber ) {
    ChannelLogBean clBean = null;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get channel log bean "
          + ", found null event id" );
      return clBean;
    }
    if ( StringUtils.isBlank( phoneNumber ) ) {
      DLog.warning( lctx , "Failed to get channel log bean "
          + ", found blank phone number" );
      return clBean;
    }
    clBean = dao.getChannelLogBean( app.isDebug() , eventId , phoneNumber );
    return clBean;
  }

  public List getSubscriberPhoneNumber( int eventId , int limit ) {
    return dao.getSubscriberPhoneNumber( eventId , limit );
  }

  public List getChannelLogBeans( int eventId , boolean useDateSend ,
      int offset , int limit ) {
    Boolean marked = null;
    return dao.getChannelLogBeans( app.isDebug() , eventId , marked ,
        new Boolean( useDateSend ) , offset , limit );
  }

  public List getMarkedChannelLogBeans( int eventId , boolean useDateSend ,
      int limit ) {
    Boolean marked = new Boolean( true );
    return dao.getChannelLogBeans( app.isDebug() , eventId , marked ,
        new Boolean( useDateSend ) , 0 , limit );
  }

  public List getMarkedChannelLogBeans( int eventId , boolean useDateSend ,
      int offset , int limit ) {
    Boolean marked = new Boolean( true );
    return dao.getChannelLogBeans( app.isDebug() , eventId , marked ,
        new Boolean( useDateSend ) , offset , limit );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalSubscriber( int eventId ) {
    return dao.getTotalSubscriber( app.isDebug() , eventId );
  }

  public long getTotalRemains( int eventId ) {
    return dao.getTotalRemains( app.isDebug() , eventId );
  }

  public double getTotalCreditCostForRemainsSubscriber( int channelSessionId ,
      int eventId , int clientId , boolean debug ) {
    double totalCreditCost = 0;

    String headerLog = ChannelSessionLogSupport.headerLog( channelSessionId );

    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to get total country "
          + "credit cost for remains subscriber , found null event id" );
      return totalCreditCost;
    }

    totalCreditCost = dao.getTotalCountryCreditCostForRemainsSubscriber(
        app.isDebug() , eventId );
    DLog.debug( lctx , headerLog + "Read total country credit cost "
        + "for remains subscriber = " + totalCreditCost + " unit(s)" );

    // find client band discount
    double clientBandDiscount = 1.0;
    {
      QClientBand clientBand = ClientCommon.getClientBand( clientId );
      if ( clientBand != null ) {
        TBand band = BandCommon.getBand( clientBand.getBandId() );
        if ( band != null ) {
          clientBandDiscount = band.getDiscount();
          if ( debug ) {
            DLog.debug( lctx , headerLog
                + "Define client band discount factor = " + clientBandDiscount );
          }
        } else {
          if ( debug ) {
            DLog.debug( lctx , headerLog + "Can not find band bean "
                + ", based on band id = " + clientBand.getBandId()
                + " , no discount added" );
          }
        }
      } else {
        if ( debug ) {
          DLog.debug( lctx , headerLog + "Can not find client band bean "
              + ", based on client id = " + clientId + " , no discount added" );
        }
      }
    }

    DLog.debug( lctx , headerLog + "Read client band discount = "
        + clientBandDiscount );

    totalCreditCost = totalCreditCost * clientBandDiscount;

    return totalCreditCost;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Store / Clean Subscribers Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long storeSubscribers( int channelSessionId , int eventId ,
      int subscriberGroupId , boolean allowDuplicated , boolean useDateSend ) {
    long totalRecords = 0;

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to store subscribers "
          + ", found zero channel session id" );
      return totalRecords;
    }
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to store subscribers "
          + ", found zero event id" );
      return totalRecords;
    }
    if ( subscriberGroupId < 1 ) {
      DLog.warning( lctx , "Failed to store subscribers "
          + ", found zero subscriber group id" );
      return totalRecords;
    }

    // trap delta time
    long deltaTime = System.currentTimeMillis();

    // header log
    String headerLog = "[ChannelSession-" + channelSessionId + "] [ChannelLog-"
        + eventId + "] [ClientSubscriber-" + subscriberGroupId + "] ";

    // get default credit cost from online properties
    double defaultCountryCreditCost = opropsApp.getDouble(
        "CountryProfile.CreditCost" , 0.0 );
    DLog.debug( lctx , headerLog + "Prepare default country credit cost = "
        + defaultCountryCreditCost );

    // copy subscribers
    totalRecords = dao.storeSubscribers( app.isDebug() , channelSessionId ,
        eventId , subscriberGroupId , allowDuplicated , useDateSend ,
        defaultCountryCreditCost );

    // calculate delta time
    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it
    DLog.debug( lctx , headerLog + "Stored subscribers from "
        + "clientSubscriber into channelLog , total effected = " + totalRecords
        + " record(s) , and takes = " + deltaTime + " ms" );

    return totalRecords;
  }

  public boolean cleanSubscribers( int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to clean subscribers "
          + ", found null event id" );
      return result;
    }
    result = dao.cleanSubscribers( app.isDebug() , eventId );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Un/Subscribed Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean subscribeNumber( int eventId , String phoneNumber ,
      Date dateSubscribed ) {
    boolean subscribed = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to subscribe number "
          + ", found zero eventId" );
      return subscribed;
    }
    if ( ( phoneNumber == null ) || phoneNumber.equals( "" ) ) {
      DLog.warning( lctx , "Failed to subscribe number "
          + ", found null phone number" );
      return subscribed;
    }
    dateSubscribed = ( dateSubscribed == null ) ? new Date() : dateSubscribed;
    ChannelLogBean channelLogBean = dao.getChannelLogBean( app.isDebug() ,
        eventId , phoneNumber );
    if ( channelLogBean == null ) {
      channelLogBean = new ChannelLogBean();
      channelLogBean.setMarked( 0 );
      channelLogBean.setPhone( phoneNumber );
      channelLogBean.setEventId( eventId );
      channelLogBean.setSubscribed( 1 );
      channelLogBean.setDateTm( new Date() );
      channelLogBean.setDateSubscribed( dateSubscribed );
      subscribed = dao.insertChannelLogBean( channelLogBean );
      if ( !subscribed ) {
        DLog.warning( lctx , "Failed to insert new subscriber "
            + "in the channel log table" );
      }
      return subscribed;
    }
    if ( channelLogBean.getSubscribed() > 0 ) {
      DLog.warning( lctx , "Failed to subscribe number "
          + ", the number is already subscribed" );
      return subscribed;
    }
    subscribed = dao.updateSubscribedStatus( eventId , phoneNumber ,
        dateSubscribed );
    return subscribed;
  }

  public boolean unsubscribeNumber( int eventId , String phoneNumber ,
      Date dateUnsubscribed ) {
    boolean unsubscribed = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to unsubscribe number "
          + ", found zero eventId" );
      return unsubscribed;
    }
    if ( ( phoneNumber == null ) || phoneNumber.equals( "" ) ) {
      DLog.warning( lctx , "Failed to unsubscribe number "
          + ", found null phone number" );
      return unsubscribed;
    }
    dateUnsubscribed = ( dateUnsubscribed == null ) ? new Date()
        : dateUnsubscribed;
    if ( ( phoneNumber == null ) || phoneNumber.equals( "" ) ) {
      DLog.warning( lctx , "Failed to unsubscribe number "
          + ", can not find subscriber in channel log table for eventId = "
          + eventId + " and phoneNumber = " + phoneNumber );
      return unsubscribed;
    }
    unsubscribed = dao.updateUnsubscribedStatus( eventId , phoneNumber ,
        dateUnsubscribed );
    return unsubscribed;
  }

}
