package com.beepcast.channel;

import java.io.DataOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.channel.ChannelManagementReportFileClass.XipmeLink;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementReportFileCsv {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelManagementReportFileCsv" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportFileCsv() {

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean writeHeaders( boolean debug , String headerLog ,
      DataOutputStream dos , int sizeXipmeLinks ) {
    boolean result = false;
    try {
      StringBuffer csvLine = new StringBuffer();
      csvLine.append( "MessageId,PhoneNumber," );
      csvLine.append( "RetryStep,DateSubmitMessage," );
      csvLine.append( "DeliveryStatus,DateReceivedStatus," );
      csvLine.append( "SubscribedStatus,UnsubscribedDate," );
      csvLine.append( "GlobalSubscribedStatus,GlobalUnsubscribedDate," );
      csvLine.append( "MessageCount,MessageContent" );
      for ( int idx = 0 ; idx < sizeXipmeLinks ; idx++ ) {
        csvLine.append( ",XipmeLink" );
        csvLine.append( idx + 1 );
        csvLine.append( "Code" );
        csvLine.append( ",XipmeLink" );
        csvLine.append( idx + 1 );
        csvLine.append( "Clicks" );
      }
      if ( debug ) {
        DLog.debug( lctx ,
            headerLog + "Writing csvLine : " + csvLine.toString() );
      }
      result = ChannelManagementReportFileCommon.writeToFileTextStream( csvLine
          .toString().concat( "\n" ) , dos );
    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to write headers , " + e );
    }
    return result;
  }

  public boolean writeRecords( boolean debug , String headerLog ,
      DataOutputStream dos , String messageId , String phoneNumber ,
      int retryStep , Date dateSubmitMessage , String deliveryStatus ,
      Date dateReceivedStatus , String subscribedStatus ,
      Date unsubscribedDate , String globalSubscribedStatus ,
      Date globalUnsubscribedDate , int messageCount , String messageContent ,
      List listXipmeLinks ) {
    boolean result = false;
    try {
      StringBuffer csvLine = new StringBuffer();

      // clean string params

      messageId = ( messageId == null ) ? "None" : messageId;
      phoneNumber = ( phoneNumber == null ) ? "None" : phoneNumber;
      String strDateSubmitMessage = ( dateSubmitMessage == null ) ? "None"
          : DateTimeFormat.convertToString( dateSubmitMessage );
      deliveryStatus = ( deliveryStatus == null ) ? "None" : deliveryStatus;
      String strDateReceivedStatus = ( dateReceivedStatus == null ) ? "None"
          : DateTimeFormat.convertToString( dateReceivedStatus );
      subscribedStatus = ( subscribedStatus == null ) ? "None"
          : subscribedStatus;
      String strUnsubscribedDate = ( unsubscribedDate == null ) ? "None"
          : DateTimeFormat.convertToString( unsubscribedDate );
      globalSubscribedStatus = ( globalSubscribedStatus == null ) ? "None"
          : globalSubscribedStatus;
      String strGlobalUnsubscribedDate = ( globalUnsubscribedDate == null ) ? "None"
          : DateTimeFormat.convertToString( globalUnsubscribedDate );
      messageContent = ( messageContent == null ) ? "" : messageContent;

      // compose params

      csvLine.append( StringEscapeUtils.escapeCsv( messageId ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( phoneNumber ) );
      csvLine.append( "," );
      csvLine.append( retryStep );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( strDateSubmitMessage ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( deliveryStatus ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( strDateReceivedStatus ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( subscribedStatus ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( strUnsubscribedDate ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( globalSubscribedStatus ) );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( strGlobalUnsubscribedDate ) );
      csvLine.append( "," );
      csvLine.append( messageCount );
      csvLine.append( "," );
      csvLine.append( StringEscapeUtils.escapeCsv( messageContent ) );

      if ( listXipmeLinks != null ) {
        Iterator iterXipmeLinks = listXipmeLinks.iterator();
        while ( iterXipmeLinks.hasNext() ) {
          XipmeLink xipmeLink = (XipmeLink) iterXipmeLinks.next();
          if ( xipmeLink == null ) {
            continue;
          }
          csvLine.append( "," );
          csvLine.append( StringEscapeUtils.escapeCsv( xipmeLink.getCode() ) );
          csvLine.append( "," );
          csvLine.append( xipmeLink.getHit() );
        }
      }

      // write to file

      if ( debug ) {
        DLog.debug( lctx ,
            headerLog + "Writing csvLine : " + csvLine.toString() );
      }
      result = ChannelManagementReportFileCommon.writeToFileTextStream( csvLine
          .toString().concat( "\n" ) , dos );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to write records , " + e );
    }
    return result;
  }
  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
