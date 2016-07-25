package com.beepcast.channel.xipme;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.common.util.concurrent.ConcurrentHashMap;
import com.beepcast.model.event.EventProcessCommon;
import com.beepcast.model.transaction.xipme.XipmeSupport;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class XipmeBuffer {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "XipmeBuffer" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;

  private String headerLog;

  private BoundedLinkedQueue queueChannelLogBeans;
  private Map mapChannelLogCodes;

  private int eventId;
  private boolean useDateSend;

  private boolean threadActive;
  private XipmeBufferWriter threadWriter;
  private XipmeBufferWorker[] threadWorkers;
  private XipmeBufferManager threadManager;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeBuffer( String headerLog , int eventId , boolean useDateSend ,
      long totalAvailableNumbers ) {

    opropsApp = OnlinePropertiesApp.getInstance();

    headerLog = ( headerLog == null ) ? "" : headerLog;
    this.headerLog = headerLog;

    DLog.debug( lctx , headerLog + "Initialized xipme buffer "
        + ", with : eventId = " + eventId + " , useDateSend = " + useDateSend
        + " , totalAvailableNumbers = " + totalAvailableNumbers );

    int queueSize = (int) opropsApp.getLong( "Channel.XipmeBuffer.queueSize" ,
        1000 );
    queueChannelLogBeans = new BoundedLinkedQueue( queueSize );
    DLog.debug( lctx , headerLog + "Created internal queue with capacity "
        + queueChannelLogBeans.capacity() + " msg(s)" );

    mapChannelLogCodes = new ConcurrentHashMap();
    DLog.debug( lctx , headerLog + "Created internal buffer with "
        + "hash map class type" );

    this.eventId = eventId;
    this.useDateSend = useDateSend;

    threadActive = false;

    threadWriter = new XipmeBufferWriter( headerLog , queueChannelLogBeans ,
        eventId , useDateSend , totalAvailableNumbers );
    DLog.debug( lctx , headerLog + "Created writer thread" );

    threadWorkers = new XipmeBufferWorker[(int) opropsApp.getLong(
        "Channel.XipmeBuffer.workerSize" , 1 )];
    for ( int idx = 0 ; idx < threadWorkers.length ; idx++ ) {
      threadWorkers[idx] = new XipmeBufferWorker( idx , headerLog , eventId ,
          queueChannelLogBeans , mapChannelLogCodes );
    }
    DLog.debug( lctx , headerLog + "Created " + threadWorkers.length
        + " worker(s) thread" );

    threadManager = new XipmeBufferManager( headerLog , queueChannelLogBeans ,
        mapChannelLogCodes );
    DLog.debug( lctx , headerLog + "Created manager thread" );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void start( long recordOffset ) {

    // invalid xipme buffer when found invalid event

    if ( eventId < 1 ) {
      DLog.warning( lctx , headerLog + "Xipme buffer doesnt apply with "
          + "invalid event id, thread stopped." );
      return;
    }

    // no need to do the xipme buffer when found use date send

    if ( useDateSend ) {
      DLog.debug( lctx , headerLog + "Xipme buffer doesnt apply with "
          + "broadcast that use date send, thread stopped." );
      return;
    }

    // no need to do the xipme buffer when found plain text response

    if ( isPlainTextMessageResponses() ) {
      DLog.debug( lctx , headerLog + "Xipme buffer doesnt apply with "
          + "plain text message responses, thread stopped." );
      return;
    }

    // start threads

    DLog.debug( lctx , headerLog + "All workers are starting" );
    threadActive = true;
    threadWriter.moduleStart( recordOffset );
    for ( int idx = 0 ; idx < threadWorkers.length ; idx++ ) {
      threadWorkers[idx].moduleStart();
    }
    threadManager.moduleStart();

    // sleep for awhile before the broadcast started

    try {
      int startTimeout = (int) opropsApp.getLong(
          "Channel.XipmeBuffer.startTimeout" , 30000 );
      DLog.debug( lctx , headerLog + "Suspend the broadcast running "
          + "for awhile ( " + startTimeout + " ms ) , so that xipme "
          + "buffer can start first" );
      int delayCtr = 0 , delay1s = 1000;
      while ( threadActive && ( delayCtr < startTimeout ) ) {
        Thread.sleep( delay1s );
        delayCtr = delayCtr + delay1s;
      }
    } catch ( Exception e ) {
    }

  }

  public void stop() {

    // stop threads

    DLog.debug( lctx , headerLog + "All workers are stopping" );
    threadActive = false;
    threadWriter.moduleStop();
    for ( int idx = 0 ; idx < threadWorkers.length ; idx++ ) {
      threadWorkers[idx].moduleStop();
    }
    threadManager.moduleStop();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean isPlainTextMessageResponses() {
    boolean result = true;

    String[] arrTextMessageResponses = EventProcessCommon
        .getFirstAndNextExecutedProcessBeanResponses( eventId );
    if ( ( arrTextMessageResponses == null )
        || ( arrTextMessageResponses.length < 1 ) ) {
      return result;
    }

    String textMessageResponse = null;
    String textMessageResponseUpperCase = null;
    ArrayList messageLinks = null;
    for ( int idx = 0 ; idx < arrTextMessageResponses.length ; idx++ ) {
      textMessageResponse = arrTextMessageResponses[idx];
      if ( ( textMessageResponse == null )
          || ( textMessageResponse.equals( "" ) ) ) {
        continue;
      }

      // validate based on reserved keyword

      textMessageResponseUpperCase = textMessageResponse.toUpperCase();
      if ( textMessageResponseUpperCase.indexOf( "<#XIPME" ) > -1 ) {
        DLog.debug( lctx , headerLog + "This is not plain text message "
            + "responses , found reserved keyword in the message : "
            + StringEscapeUtils.escapeJava( textMessageResponse ) );
        result = false;
        break;
      }

      // validate based on shortener link

      messageLinks = XipmeSupport.extractMessageLinks( textMessageResponse );
      if ( ( messageLinks != null ) && ( messageLinks.size() > 0 ) ) {
        DLog.debug( lctx , headerLog + "This is not plain text message "
            + "responses , found shortener link in the message : "
            + StringEscapeUtils.escapeJava( textMessageResponse ) );
        result = false;
        break;
      }

    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long updateRecordOffset( long totalRemains ) {
    return threadWriter.updateRecordOffset( totalRemains );
  }

  public Map readCodes( int channelLogId ) {
    Map map = null;

    if ( channelLogId < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to read codes "
          + ", found zero channel log id" );
      return map;
    }

    long deltaTime = System.currentTimeMillis();
    String key = Integer.toString( channelLogId );
    map = (Map) mapChannelLogCodes.remove( key );
    deltaTime = System.currentTimeMillis() - deltaTime;

    if ( map != null ) {
      int queueSize = queueChannelLogBeans.size();
      int bufferSize = mapChannelLogCodes.size();
      DLog.debug( lctx , headerLog + "Read xipme codes from buffer with key = "
          + key + " , result = " + map + " , takes " + deltaTime
          + " ms ; latest info : queued = " + queueSize
          + " msg(s) , buffered = " + bufferSize + " msg(s)" );
    }

    return map;
  }

}
