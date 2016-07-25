package com.beepcast.channel;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.dbmanager.common.CountryCommon;
import com.beepcast.dbmanager.table.TCountry;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelLogSimulationService {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelLogSimulationService" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;
  private ChannelApp app;
  private ChannelLogSimulationDAO dao;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogSimulationService() {
    opropsApp = OnlinePropertiesApp.getInstance();
    app = ChannelApp.getInstance();
    dao = new ChannelLogSimulationDAO();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Function
  //
  // ////////////////////////////////////////////////////////////////////////////

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
          + ", found zero channel log simulation id" );
      return updated;
    }
    updated = dao.updateSubscriberUnmarked( id , messageId );
    return updated;
  }

  public boolean updateMessageResult( ChannelLogSimulationBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      DLog.warning( lctx , "Failed to update message result "
          + ", found null channel log simulation bean" );
      return result;
    }
    if ( bean.getId() < 1 ) {
      DLog.warning( lctx , "Failed to update message result "
          + ", found zero channel log simulation id" );
      return result;
    }
    result = dao.updateMessageResult( bean );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Select Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogSimulationBean selectById( int id ) {
    ChannelLogSimulationBean bean = null;
    if ( id < 1 ) {
      return bean;
    }
    bean = dao.selectById( app.isDebug() , id );
    return bean;
  }

  public List getMarkedChannelLogSimulationBeans( int eventId ,
      boolean useDateSend , int limit ) {
    return dao.getChannelLogSimulationBeans( app.isDebug() , eventId ,
        new Boolean( true ) , new Boolean( useDateSend ) , 0 , limit );
  }

  public List getMarkedChannelLogSimulationBeans( int eventId ,
      boolean useDateSend , int offset , int limit ) {
    return dao.getChannelLogSimulationBeans( app.isDebug() , eventId ,
        new Boolean( true ) , new Boolean( useDateSend ) , offset , limit );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalSubscriber( int eventId ) {
    return dao.getTotalSubscriber( app.isDebug() , eventId );
  }

  public long getTotalRemains( int eventId ) {
    return dao.getTotalRemains( app.isDebug() , eventId );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Store / Clean Subscribers Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long storeSubscribers( String simulationType , long simulationNumbers ,
      int eventId , int subscriberGroupId , boolean allowDuplicated ,
      boolean useDateSend ) {
    long totalRecords = 0;

    // trap delta time
    long deltaTime = System.currentTimeMillis();

    // header log
    String headerLog = "[ClientSubscriber-" + subscriberGroupId
        + "] [ChannelLog-" + eventId + "] ";

    // validate must be params
    if ( StringUtils.isBlank( simulationType ) ) {
      simulationType = ChannelSessionSimulationType.SEQUENCE;
      DLog.debug( lctx , headerLog + "Changed simulation type into "
          + simulationType );
    }
    if ( simulationNumbers < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to store simulation "
          + "subscribers , found zero simulation numbers" );
      return totalRecords;
    }
    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to store simulation "
          + "subscribers , found null event id" );
      return totalRecords;
    }
    if ( subscriberGroupId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to store simulation "
          + "subscribers , found null subscriber group id" );
      return totalRecords;
    }

    // get default credit cost from online properties
    double defaultCountryCreditCost = opropsApp.getDouble(
        "CountryProfile.CreditCost" , 0.0 );
    DLog.debug( lctx , headerLog + "Prepare default country credit cost = "
        + defaultCountryCreditCost );

    // copy subscribers
    totalRecords = dao.storeSubscribers( app.isDebug() , eventId ,
        subscriberGroupId , allowDuplicated , defaultCountryCreditCost ,
        simulationType , simulationNumbers );

    // calculate delta time
    deltaTime = System.currentTimeMillis() - deltaTime;

    // log it
    DLog.debug( lctx , headerLog + "Stored simulation subscribers from "
        + "client subscriber table into channel log simulation table "
        + ", total effected = " + totalRecords + " record(s) , and takes = "
        + deltaTime + " ms" );
    return totalRecords;
  }

  public boolean cleanSubscribers( int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to clean subscribers "
          + ", found zero event id" );
      return result;
    }
    result = dao.cleanSubscribers( app.isDebug() , eventId );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ...

}
