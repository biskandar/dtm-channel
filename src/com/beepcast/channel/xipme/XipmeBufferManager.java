package com.beepcast.channel.xipme;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.beepcast.channel.ChannelLogBean;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

class XipmeBufferManager extends Thread {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "XipmeBufferManager" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;
  private String headerLog;
  private BoundedLinkedQueue queueChannelLogBeans;
  private Map mapChannelLogCodes;

  private boolean threadActiveManager;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeBufferManager( String headerLog ,
      BoundedLinkedQueue queueChannelLogBeans , Map mapChannelLogCodes ) {
    super( "XipmeBufferManager" );

    opropsApp = OnlinePropertiesApp.getInstance();
    this.headerLog = headerLog;
    this.queueChannelLogBeans = queueChannelLogBeans;
    this.mapChannelLogCodes = mapChannelLogCodes;

    threadActiveManager = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {
    int delay1s = 1000 , counter = 0;
    DLog.debug( lctx , headerLog + "Thread started." );
    threadActiveManager = true;
    while ( threadActiveManager ) {
      try {

        Thread.sleep( delay1s );
        counter = counter + delay1s;
        if ( counter < opropsApp.getLong( "Channel.XipmeBuffer.managerLatency" ,
            60000 ) ) {
          continue;
        }
        counter = 0;

        int totalQueued = queueChannelLogBeans.size();
        int totalBuffered = mapChannelLogCodes.size();
        DLog.debug( lctx , headerLog + "Info : queued = " + totalQueued
            + " msg(s) , buffered = " + totalBuffered + " msg(s)" );

      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to process management , " + e );
      }
    } // while ( threadActiveManager )
    debugLogQueue();
    debugLogBuffer();
    DLog.debug( lctx , headerLog + "Thread stopped." );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void moduleStart() {
    DLog.debug( lctx , headerLog + "Thread starting" );
    super.start();
  }

  public void moduleStop() {
    DLog.debug( lctx , headerLog + "Thread stopping" );
    threadActiveManager = false;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void debugLogQueue() {
    int count = 0;
    StringBuffer sbLog = new StringBuffer();
    while ( queueChannelLogBeans.size() > 0 ) {
      try {

        long pollTimeout = opropsApp.getLong(
            "Channel.XipmeBuffer.queueTimeout" , 5000 );
        ChannelLogBean clBean = (ChannelLogBean) queueChannelLogBeans
            .poll( pollTimeout );
        if ( clBean == null ) {
          break;
        }

        count = count + 1;
        sbLog.append( clBean.getId() );
        sbLog.append( "=" );
        sbLog.append( clBean.getPhone() );
        sbLog.append( ", " );

      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to take out all the rest msg(s) "
            + "from queue , " + e );
        break;
      }
    }
    if ( count > 0 ) {
      DLog.debug( lctx , "Take out all the rest " + count
          + " msg(s) from queue : " + sbLog.toString() );
    }
  }

  private void debugLogBuffer() {
    int count = 0;
    StringBuffer sbLog = new StringBuffer();
    Set setKey = mapChannelLogCodes.keySet();
    Iterator iterKey = setKey.iterator();
    while ( iterKey.hasNext() ) {
      try {

        String key = (String) iterKey.next();
        if ( key == null ) {
          continue;
        }

        Map map = (Map) mapChannelLogCodes.remove( key );
        if ( map == null ) {
          continue;
        }

        count = count + 1;
        sbLog.append( key );
        sbLog.append( "=" );
        sbLog.append( map );
        sbLog.append( ", " );

      } catch ( Exception e ) {
        DLog.warning( lctx , "Failed to take out all the rest msg(s) "
            + "from buffer , " + e );
        break;
      }
    }
    if ( count > 0 ) {
      DLog.debug( lctx , "Take out all the rest " + count
          + " msg(s) from buffer : " + sbLog.toString() );
    }
  }

}
