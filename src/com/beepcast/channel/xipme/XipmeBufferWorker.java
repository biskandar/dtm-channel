package com.beepcast.channel.xipme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.beepcast.channel.ChannelLogBean;
import com.beepcast.common.util.concurrent.BoundedLinkedQueue;
import com.beepcast.model.event.EventProcessCommon;
import com.beepcast.oproperties.OnlinePropertiesApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

class XipmeBufferWorker extends Thread {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "XipmeBufferWorker" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private OnlinePropertiesApp opropsApp;

  private String headerLog;
  private int eventId;
  private BoundedLinkedQueue queueChannelLogBeans;
  private Map mapChannelLogCodes;

  private ArrayList messageLinks;
  private Map mapXipmeCloneParams;

  private XipmeResolver xipmeResolver;
  private boolean threadActiveWorker;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeBufferWorker( int idx , String headerLog , int eventId ,
      BoundedLinkedQueue queueChannelLogBeans , Map mapChannelLogCodes ) {
    super( "XipmeBufferWorker." + idx );

    opropsApp = OnlinePropertiesApp.getInstance();

    this.headerLog = headerLog;
    this.eventId = eventId;
    this.queueChannelLogBeans = queueChannelLogBeans;
    this.mapChannelLogCodes = mapChannelLogCodes;

    messageLinks = null;
    mapXipmeCloneParams = null;

    xipmeResolver = new XipmeResolver();
    threadActiveWorker = false;

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inherited Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public void run() {
    ArrayList listChannelLogBeans = new ArrayList();
    DLog.debug( lctx , headerLog + "Thread started." );
    threadActiveWorker = true;
    while ( threadActiveWorker ) {
      try {

        // load channel log from the queue and store into list
        {
          ChannelLogBean clBean = (ChannelLogBean) queueChannelLogBeans
              .poll( opropsApp.getLong( "Channel.XipmeBuffer.queueTimeout" ,
                  5000 ) );
          if ( clBean != null ) {
            listChannelLogBeans.add( clBean );
            if ( listChannelLogBeans.size() < opropsApp.getLong(
                "Channel.XipmeBuffer.queueThreshold" , 10 ) ) {
              continue;
            }
          }
        }

        // sleep for a while

        Thread.sleep( 10 );

        // validate must be params

        if ( listChannelLogBeans.size() < 1 ) {
          continue;
        }

        // prepared to resolve

        long deltaTime = System.currentTimeMillis();
        DLog.debug( lctx , headerLog + "Prepared " + listChannelLogBeans.size()
            + " record(s) of channel log bean to resolve the link codes" );

        // read all the response messages

        String[] arrResponseMessages = EventProcessCommon
            .getFirstAndNextExecutedProcessBeanResponses( eventId );

        // resolve link codes based of all the response messages

        Map localMapCodes = xipmeResolver.resolveLinkCodes( headerLog ,
            arrResponseMessages , listChannelLogBeans );

        // find unmatched list

        List listUnmatchedChannelLogBeans = xipmeResolver
            .listUnmatchedChannelLogBeans( headerLog , listChannelLogBeans ,
                localMapCodes );

        // clear the list back

        listChannelLogBeans.clear();

        // when found list unmatched will restore back into the original list

        if ( ( listUnmatchedChannelLogBeans != null )
            && ( listUnmatchedChannelLogBeans.size() > 0 ) ) {
          DLog.warning( lctx , headerLog + "Found unsolved "
              + listUnmatchedChannelLogBeans.size()
              + " codes , will store all the ones "
              + "into the list channel log beans to reprocess" );
          listChannelLogBeans.addAll( listUnmatchedChannelLogBeans );
        }

        // no need to continue the process when found empty codes

        if ( ( localMapCodes == null ) || ( localMapCodes.size() < 1 ) ) {
          DLog.warning( lctx , headerLog + "Failed to resolve link codes "
              + ", found null result , bypass the process" );
          continue;
        }

        // store map channel log codes

        int totalBuffered = bufferMapChannelLogCodes( localMapCodes );

        // log it

        deltaTime = System.currentTimeMillis() - deltaTime;
        DLog.debug( lctx , headerLog + "Successfully resolved link codes "
            + "and buffered into map channel log codes , total = "
            + totalBuffered + " record(s) , take = " + deltaTime + " ms" );

      } catch ( Exception e ) {
        DLog.warning( lctx , headerLog + "Failed to process channel log "
            + "bean from the queue , " + e );
      }
    } // while ( threadActive )
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
    threadActiveWorker = false;
  }

  public boolean isThreadActiveWorker() {
    return threadActiveWorker;
  }

  public int bufferMapChannelLogCodes( Map localMapCodes ) {
    int totalBuffered = 0;

    if ( localMapCodes == null ) {
      return totalBuffered;
    }

    StringBuffer sbLog = new StringBuffer();
    long deltaTime = System.currentTimeMillis();

    Set setKey = localMapCodes.keySet();
    Iterator iterKey = setKey.iterator();
    while ( iterKey.hasNext() ) {

      // string channel log id as map key

      String clId = (String) iterKey.next();
      if ( clId == null ) {
        continue;
      }

      // read and validate map inner codes

      Map localMapInnerCodes = (Map) localMapCodes.get( clId );
      if ( localMapInnerCodes == null ) {
        continue;
      }

      // store into buffer

      Map mapChannelLogInnerCodes = (Map) mapChannelLogCodes.get( clId );
      if ( mapChannelLogInnerCodes == null ) {
        mapChannelLogInnerCodes = new HashMap();
        mapChannelLogCodes.put( clId , mapChannelLogInnerCodes );
      }
      mapChannelLogInnerCodes.putAll( localMapInnerCodes );

      totalBuffered = totalBuffered + 1;
      sbLog.append( clId );
      sbLog.append( ", " );

    } // while ( iterKey.hasNext() )

    deltaTime = System.currentTimeMillis() - deltaTime;

    if ( totalBuffered > 0 ) {
      DLog.debug( lctx , headerLog
          + "Stored mapChannelLogInnerCodes into buffer " + totalBuffered
          + " record(s) ( takes " + deltaTime + " ms ) : " + sbLog );
    }

    return totalBuffered;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
