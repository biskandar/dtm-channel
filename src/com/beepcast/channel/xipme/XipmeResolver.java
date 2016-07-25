package com.beepcast.channel.xipme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.beepcast.channel.ChannelLogBean;
import com.beepcast.model.event.EventBean;
import com.beepcast.model.event.EventService;
import com.beepcast.model.mobileUser.MobileUserBean;
import com.beepcast.model.mobileUser.MobileUserService;
import com.beepcast.model.transaction.xipme.XipmeSupport;
import com.beepcast.model.transaction.xipme.XipmeTranslator;
import com.beepcast.model.transaction.xipme.XipmeUtils;
import com.beepcast.subscriber.ClientSubscriberBean;
import com.beepcast.subscriber.ClientSubscriberCustomBean;
import com.beepcast.subscriber.SubscriberApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class XipmeResolver {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "XipmeResolver" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private EventService evService;
  private MobileUserService muService;
  private SubscriberApp subscriberApp;
  private XipmeTranslator xipmeTranslator;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeResolver() {
    evService = new EventService();
    muService = new MobileUserService();
    subscriberApp = SubscriberApp.getInstance();
    xipmeTranslator = new XipmeTranslator();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Map resolveLinkCodes( String headerLog , String[] arrMsgInputs ,
      List listChannelLogBeans ) {
    Map mapCodes = null;

    // validate must be params

    if ( ( arrMsgInputs == null ) || ( arrMsgInputs.length < 1 ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link codes "
          + ", found empty messages input" );
      return mapCodes;
    }

    // prepare for the map xipme clone params

    Map mapXipmeCloneParams = new HashMap();
    ArrayList listMsgLinks = new ArrayList();

    for ( int idx = 0 ; idx < arrMsgInputs.length ; idx++ ) {

      // resolve map xipme clone params from the message content

      String msgInputNew = XipmeSupport.extractMapXipmeCloneParams(
          arrMsgInputs[idx] , mapXipmeCloneParams );

      // resolve xipme links from the message content

      listMsgLinks.addAll( XipmeSupport.extractMessageLinks( msgInputNew ) );

    }

    // log it

    DLog.debug( lctx , headerLog + "Generated total " + listMsgLinks.size()
        + " message link(s) : " + listMsgLinks + " , extract from "
        + arrMsgInputs.length + " message(s)" );

    // resolve xipme link codes

    mapCodes = resolveLinkCodes( headerLog , listMsgLinks ,
        mapXipmeCloneParams , listChannelLogBeans );

    return mapCodes;
  }

  public Map resolveLinkCodes( String headerLog , ArrayList listMsgLinks ,
      Map mapXipmeCloneParams , List listChannelLogBeans ) {
    Map mapCodes = null;

    headerLog = ( headerLog == null ) ? "" : headerLog;

    if ( ( listMsgLinks == null ) || ( listMsgLinks.size() < 1 ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link codes "
          + ", found empty list message links" );
      return mapCodes;
    }

    ArrayList cloneMapsParams = new ArrayList();
    long deltaTime = System.currentTimeMillis();

    // load list of clone maps params

    EventBean evBean = null;
    ChannelLogBean clBean = null;
    Iterator iterChannelLogBeans = listChannelLogBeans.iterator();
    while ( iterChannelLogBeans.hasNext() ) {
      try {
        clBean = (ChannelLogBean) iterChannelLogBeans.next();
        if ( clBean == null ) {
          continue;
        }
        if ( clBean.getEventId() < 1 ) {
          continue;
        }
        if ( ( evBean == null )
            || ( evBean.getEventID() != clBean.getEventId() ) ) {
          evBean = evService.select( (int) clBean.getEventId() );
        }
        resolveLinkCode( headerLog + "[ChannelLog-" + clBean.getId() + "] " ,
            cloneMapsParams , listMsgLinks , clBean , evBean ,
            mapXipmeCloneParams );
      } catch ( Exception e ) {
      }
    }
    if ( cloneMapsParams.size() < 1 ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link codes "
          + ", found empty list clone maps params" );
      return mapCodes;
    }

    // execute clone maps

    boolean tagChannelLogId = true;
    mapCodes = xipmeTranslator.executeCloneMaps( headerLog , cloneMapsParams ,
        tagChannelLogId );

    // calculate size

    int sizeCodes = 0;
    if ( mapCodes != null ) {
      sizeCodes = mapCodes.size();
    }

    // log it

    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , headerLog + "Resolved link codes , take " + deltaTime
        + " ms , result : size = " + sizeCodes + " record(s)" );

    return mapCodes;
  }

  public List listUnmatchedChannelLogBeans( String headerLog ,
      List listChannelLogBeans , Map mapCodes ) {
    List listUnmatched = null;

    headerLog = ( headerLog == null ) ? "" : headerLog;

    if ( listChannelLogBeans == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to list unmatched channel log beans "
          + ", found null list channel log beans" );
      return listUnmatched;
    }
    if ( mapCodes == null ) {
      DLog.warning( lctx , headerLog
          + "Failed to list unmatched channel log beans "
          + ", found null map codes" );
      return listUnmatched;
    }

    listUnmatched = new ArrayList();

    int count = 0;
    StringBuffer sbLog = new StringBuffer();
    long deltaTime = System.currentTimeMillis();

    Iterator iterChannelLogBeans = listChannelLogBeans.iterator();
    while ( iterChannelLogBeans.hasNext() ) {

      ChannelLogBean clBean = (ChannelLogBean) iterChannelLogBeans.next();
      if ( clBean == null ) {
        continue;
      }
      String clId = Integer.toString( clBean.getId() );
      if ( clId == null ) {
        continue;
      }
      Map mapInnerCodes = (Map) mapCodes.get( clId );
      if ( mapInnerCodes != null ) {
        continue;
      }
      listUnmatched.add( clBean );

      count = count + 1;
      sbLog.append( clId );
      sbLog.append( "," );

    }

    deltaTime = System.currentTimeMillis() - deltaTime;

    if ( count > 0 ) {
      DLog.warning( lctx , headerLog + "Found unresolved " + count
          + " codes ( take " + deltaTime + " ms ) : " + sbLog.toString() );
    }

    return listUnmatched;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean resolveLinkCode( String headerLog ,
      ArrayList cloneMapsParams , ArrayList listMsgLinks ,
      ChannelLogBean clBean , EventBean evBean , Map mapXipmeCloneParams ) {
    boolean result = false;

    if ( clBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link code "
          + ", found null channel log bean" );
      return result;
    }
    if ( evBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link code "
          + ", found null event bean" );
      return result;
    }

    if ( StringUtils.isBlank( clBean.getPhone() ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link code "
          + ", found blank mobile number" );
      return result;
    }

    String gatewayLogToXipMeId = "CLG"
        .concat( Integer.toString( clBean.getId() ) );

    MobileUserBean muBean = muService.select( (int) evBean.getClientID() ,
        clBean.getPhone() );

    int csId = clBean.getClientSubscriberId();

    ClientSubscriberBean csBean = null;
    if ( csId > 0 ) {
      csBean = subscriberApp.getClientSubscriberBean( csId );
    }

    ClientSubscriberCustomBean cscBean = null;
    if ( csBean != null ) {
      cscBean = csBean.getCsCustomBean();
    }

    // set clone maps
    if ( !xipmeTranslator.setCloneMaps( headerLog , cloneMapsParams ,
        listMsgLinks , XipmeUtils.cleanMobileNumber( clBean.getPhone() ) ,
        gatewayLogToXipMeId , muBean , clBean , csBean , cscBean , evBean ,
        mapXipmeCloneParams ) ) {
      DLog.warning( lctx , headerLog + "Failed to resolve link code "
          + ", found failed to set clone maps" );
      return result;
    }

    result = true;
    return result;
  }

  private int totalUnmatchLinkCodes( List listChannelLogBeans , Map mapCodes ) {
    int total = -1;

    if ( listChannelLogBeans == null ) {
      return total;
    }
    if ( mapCodes == null ) {
      return total;
    }

    // ...

    return total;
  }

}
