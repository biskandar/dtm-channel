package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.common.ChannelSessionAlert;
import com.beepcast.channel.common.ChannelSessionLogSupport;
import com.beepcast.dbmanager.common.ClientCommon;
import com.beepcast.dbmanager.common.EventCommon;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.subscriber.SubscriberApp;
import com.beepcast.subscriber.SubscriberGroupBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class AlertForIdleScheduledChannelSession {

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

  private static List listAlertedIdleScheduledBroadcasts = new ArrayList();

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Functions
  //
  // ////////////////////////////////////////////////////////////////////////////

  public static boolean processAlertMessages() {
    boolean result = false;
    try {

      // list idle scheduled broadcasts
      ChannelSessionService csService = new ChannelSessionService();
      List listChannelSessionBeans = csService
          .getIdleScheduledChannelSessions( 100 );
      if ( ( listChannelSessionBeans == null )
          || ( listChannelSessionBeans.size() < 1 ) ) {
        return result;
      }

      // date now
      Calendar calNow = Calendar.getInstance();

      // millis scope
      long millis15mins = 15 * 60 * 1000;

      // profile broadcast
      ChannelSessionBean csBean = null;

      // iterate idle scheduled broadcasts
      Iterator iterChannelSessionBeans = listChannelSessionBeans.iterator();
      while ( iterChannelSessionBeans.hasNext() ) {
        csBean = (ChannelSessionBean) iterChannelSessionBeans.next();
        if ( csBean == null ) {
          continue;
        }
        Integer csId = new Integer( csBean.getId() );
        Date dateScheduledStart = csBean.getDateSchedulledStart();
        if ( dateScheduledStart == null ) {
          continue;
        }
        if ( calNow.getTime().getTime() < ( dateScheduledStart.getTime() - millis15mins ) ) {
          listAlertedIdleScheduledBroadcasts.remove( csId );
          continue;
        }
        if ( calNow.getTime().getTime() > dateScheduledStart.getTime() ) {
          listAlertedIdleScheduledBroadcasts.remove( csId );
          continue;
        }
        if ( listAlertedIdleScheduledBroadcasts.indexOf( csId ) < 0 ) {
          listAlertedIdleScheduledBroadcasts.add( csId );
          sendAlertMessage( csBean );
        }
      }

      result = true;
    } catch ( Exception e ) {
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Functions
  //
  // ////////////////////////////////////////////////////////////////////////////

  private static boolean sendAlertMessage( ChannelSessionBean csBean ) {
    boolean result = false;
    if ( csBean == null ) {
      return result;
    }
    // header log
    String headerLog = ChannelSessionLogSupport.headerLog( csBean );
    // get subscriber group bean
    SubscriberGroupBean sgBean = SubscriberApp.getInstance()
        .getSubscriberGroupBean( csBean.getGroupSubscriberId() );
    if ( sgBean == null ) {
      return result;
    }
    // get event bean
    TEvent eventOut = EventCommon.getEvent( csBean.getEventId() );
    if ( eventOut == null ) {
      return result;
    }
    // get client bean
    TClient clientOut = ClientCommon.getClient( eventOut.getClientId() );
    if ( clientOut == null ) {
      return result;
    }
    // send alert message
    DLog.debug( lctx , headerLog + "Sending an alert message for timeout idle "
        + "scheduled broadcast : eventName = " + eventOut.getEventName()
        + " , companyName = " + clientOut.getCompanyName() + " , listName = "
        + sgBean.getGroupName() );
    ChannelSessionAlert.sendAlertTimeoutScheduledBroadcast( csBean , sgBean ,
        clientOut , eventOut );
    result = true;
    return result;
  }

}
