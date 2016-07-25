package com.beepcast.channel.common;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.channel.ChannelSessionBean;
import com.beepcast.dbmanager.table.TClient;
import com.beepcast.dbmanager.table.TEvent;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.model.event.ProcessCommon;
import com.beepcast.onm.OnmApp;
import com.beepcast.onm.alert.AlertMessageFactory;
import com.beepcast.onm.data.AlertMessage;
import com.beepcast.subscriber.SubscriberGroupBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionAlert {

  static final DLogContext lctx = new SimpleContext( "ChannelSessionAlert" );

  private static OnmApp onmApp = OnmApp.getInstance();

  public static void sendAlertTimeoutScheduledBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Scheduled broadcast event [" + event.getEventName()
        + "] has not been activated yet.";
    String headerMessage = "Found a broadcast event that is scheduled "
        + "to start in 15 minutes, but has not been activated yet.";
    String footerMessage = "  Scheduled Start Date : "
        + DateTimeFormat.convertToString( channelSession
            .getDateSchedulledStart() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertActivatedScheduledBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Scheduled broadcast event [" + event.getEventName()
        + "] was activated.";
    String headerMessage = "Found scheduled broadcast event was activated.";
    String footerMessage = "  Scheduled Start Date : "
        + DateTimeFormat.convertToString( channelSession
            .getDateSchedulledStart() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertInactivatedScheduledBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Scheduled broadcast event [" + event.getEventName()
        + "] was deactivated.";
    String headerMessage = "Found scheduled broadcast event was deactivated.";
    String footerMessage = "  Scheduled Start Date : "
        + DateTimeFormat.convertToString( channelSession
            .getDateSchedulledStart() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertStartManualBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Broadcast event [" + event.getEventName()
        + "] was started.";
    String headerMessage = "Found broadcast event was started manually.";
    String footerMessage = "  Started Date : "
        + DateTimeFormat.convertToString( channelSession.getDateStarted() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertStartScheduledBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Scheduled broadcast event [" + event.getEventName()
        + "] was started.";
    String headerMessage = "Found scheduled broadcast event was started.";
    String footerMessage = "  Started Date : "
        + DateTimeFormat.convertToString( channelSession.getDateStarted() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertSuspendedBroadcast(
      ChannelSessionBean channelSession , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Broadcast event [" + event.getEventName()
        + "] was suspended.";
    String headerMessage = "Found broadcast event was suspended.";
    String footerMessage = "  Suspended Date : "
        + DateTimeFormat.convertToString( new Date() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertFinishedBroadcast(
      ChannelSessionBean channelSession , long totalDropNumbers ,
      SubscriberGroupBean subscriberGroup , TClient client , TEvent event ) {
    String subject = "Broadcast event [" + event.getEventName()
        + "] has finished. ";
    if ( totalDropNumbers > 0 ) {
      subject += "( with drop " + totalDropNumbers + " numbers ) ";
    }
    String headerMessage = "Found broadcast event has finished.";
    String footerMessage = "  Finished Date : "
        + DateTimeFormat.convertToString( new Date() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession ,
            totalDropNumbers , subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertStopBroadcast( ChannelSessionBean channelSession ,
      SubscriberGroupBean subscriberGroup , TClient client , TEvent event ,
      String reasonMessage ) {
    String subject = "Broadcast event [" + event.getEventName()
        + "] was stopped.";
    String headerMessage = "Broadcast event was stopped. " + reasonMessage;
    String footerMessage = "  Stopped Date : "
        + DateTimeFormat.convertToString( new Date() );
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession , 0 ,
            subscriberGroup , client , event , footerMessage ) );
  }

  public static void sendAlertDropMessages( ChannelSessionBean channelSession ,
      long totalDropNumbers , SubscriberGroupBean subscriberGroup ,
      TClient client , TEvent event ) {
    String subject = "Broadcast event [" + event.getEventName() + "] has "
        + totalDropNumbers + " drop numbers.";
    String headerMessage = "Found broadcast event has drop numbers.";
    String footerMessage = "";
    sendAlertMessage(
        subject ,
        composeAlertBodyMessage( headerMessage , channelSession ,
            totalDropNumbers , subscriberGroup , client , event , footerMessage ) );
  }

  private static String composeAlertBodyMessage( String headerMessage ,
      ChannelSessionBean channelSession , long totalDropNumbers ,
      SubscriberGroupBean subscriberGroup , TClient client , TEvent event ,
      String footerMessage ) {
    StringBuffer sb = new StringBuffer();
    try {
      sb.append( headerMessage + "\n" );
      sb.append( "\n" );
      if ( client != null ) {
        sb.append( "  Client Name   : " + client.getCompanyName() + "\n" );
      }
      if ( event != null ) {
        sb.append( "  Event Name    : " + event.getEventName() + "\n" );
        sb.append( "  Event Message : "
            + StringEscapeUtils.escapeJava( ProcessCommon
                .getFirstProcessBeanResponse( event.getEventId() ) ) + "\n" );
      }
      if ( subscriberGroup != null ) {
        sb.append( "  List Name     : " + subscriberGroup.getGroupName() + "\n" );
      }
      if ( channelSession != null ) {
        // sb.append( " Broadcast Id : " + channelSession.getId() + "\n" );
        sb.append( "  Total Prepare : " + channelSession.getTotalNumbers()
            + " number(s)\n" );
        sb.append( "  Total Remains : " + channelSession.getTotalRemains()
            + " number(s)\n" );
      }
      if ( totalDropNumbers > 0 ) {
        sb.append( "  Total Drop    : " + totalDropNumbers + " number(s)\n" );
      }
      sb.append( "\n" );
      sb.append( footerMessage + "\n" );
      sb.append( "\n" );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to compose alert body message , " + e );
    }
    return sb.toString();
  }

  private static void sendAlertMessage( String subject , String message ) {
    sendAlertMessage( AlertMessageFactory.createAlertEmailMessage(
        AlertMessage.DESTNODE_SUPPORT_EMAIL , null , subject , message , 3 ) );
  }

  private static boolean sendAlertMessage( AlertMessage alertMessage ) {
    boolean result = false;
    if ( alertMessage == null ) {
      DLog.warning( lctx , "Failed to send alert message "
          + ", found null message" );
      return result;
    }
    try {
      result = onmApp.sendAlert( alertMessage );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to send alert message , " + e );
    }
    return result;
  }

}
