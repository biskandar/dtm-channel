package com.beepcast.channel.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.channel.ChannelSessionBean;
import com.beepcast.channel.ChannelSessionService;
import com.beepcast.channel.ChannelSessionXipmeBean;
import com.beepcast.channel.ChannelSessionXipmeService;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.client.ClientBean;
import com.beepcast.model.client.ClientService;
import com.beepcast.model.event.EventBean;
import com.beepcast.model.event.EventService;
import com.beepcast.model.gateway.GatewayLogService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionXipmeSupport {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionXipmeSupport" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;

  private ChannelSessionService csService;
  private GatewayLogService glService;
  private ChannelSessionXipmeService csxService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionXipmeSupport() {

    dbLib = DatabaseLibrary.getInstance();

    csService = new ChannelSessionService();
    glService = new GatewayLogService();
    csxService = new ChannelSessionXipmeService();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean persistMapReport( int channelSessionId ) {
    boolean result = false;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to persist map report "
          + ", found empty channelSessionId" );
      return result;
    }
    result = persistMapReport( csService.getChannelSession( channelSessionId ) );
    return result;
  }

  public boolean persistMapReport( ChannelSessionBean csBean ) {
    boolean persisted = false;

    if ( csBean == null ) {
      DLog.warning( lctx , "Failed to persist map report "
          + ", found null channel session bean" );
      return persisted;
    }

    String headerLog = ChannelSessionLogSupport.headerLog( csBean );

    try {

      EventService evService = new EventService();
      EventBean evBean = evService.select( csBean.getEventId() );
      if ( ( evBean == null ) || ( !evBean.isActive() ) ) {
        DLog.warning(
            lctx ,
            headerLog + "Failed to persist map report "
                + ", found invalid event profile , eventId = "
                + csBean.getEventId() );
        return persisted;
      }
      Thread.sleep( 100 );

      ClientService clService = new ClientService();
      ClientBean clBean = clService.select( evBean.getClientID() );
      if ( ( clBean == null ) || ( !clBean.isActive() ) ) {
        DLog.warning(
            lctx ,
            headerLog + "Failed to persist map report "
                + ", found invalid client profile , clientId = "
                + evBean.getClientID() );
        return persisted;
      }
      Thread.sleep( 100 );

      // generate list channel session xipme from existing
      // gateway xipme records , if there is none just bypass
      List listCsxBeans = listChannelSessionXipmeBeans( csBean.getId() );
      if ( ( listCsxBeans == null ) || ( listCsxBeans.size() < 1 ) ) {
        return persisted;
      }
      Thread.sleep( 100 );

      // iterate and persist each of the channel session xipme records
      ChannelSessionXipmeBean csxBeanNew = null , csxBeanOld = null;
      Iterator iterCsxBeans = listCsxBeans.iterator();
      while ( iterCsxBeans.hasNext() ) {
        csxBeanNew = (ChannelSessionXipmeBean) iterCsxBeans.next();
        if ( csxBeanNew == null ) {
          continue;
        }

        // trap delta time
        long deltaTime = System.currentTimeMillis();

        // calculate total visitors and hits from gateway xipme table
        csxBeanNew.setTotalVisitors( glService
            .totalGatewayXipmeVisitRecordsByChannelSessionId(
                csxBeanNew.getChannelSessionId() , csxBeanNew.getXipmeCode() ) );
        csxBeanNew.setTotalHits( glService
            .totalGatewayXipmeHitRecordsByChannelSessionId(
                csxBeanNew.getChannelSessionId() , csxBeanNew.getXipmeCode() ) );

        // select from history table
        csxBeanOld = csxService.selectBean( csxBeanNew.getChannelSessionId() ,
            csxBeanNew.getXipmeCode() );
        if ( csxBeanOld != null ) {
          if ( ( csxBeanOld.getTotalMembers() == csxBeanNew.getTotalMembers() )
              && ( csxBeanOld.getTotalVisitors() == csxBeanNew
                  .getTotalVisitors() )
              && ( csxBeanOld.getTotalHits() == csxBeanNew.getTotalHits() ) ) {
            // no need to persist if found all the field values is the same
            continue;
          }
        }

        // persist the channel session xipme record
        if ( !csxService.persistBean( csxBeanNew ) ) {
          continue;
        }

        // trap deltaTime
        deltaTime = System.currentTimeMillis() - deltaTime;

        // make sure the old is always not null
        if ( csxBeanOld == null ) {
          csxBeanOld = csxBeanNew;
        }

        // log it
        DLog.debug(
            lctx ,
            headerLog + "Persisted channel session xipme "
                + "record : xipmeCode = " + csxBeanNew.getXipmeCode()
                + " , totalMembers = " + csxBeanOld.getTotalMembers() + " -> "
                + csxBeanNew.getTotalMembers() + " , totalVisitors = "
                + csxBeanOld.getTotalVisitors() + " -> "
                + csxBeanNew.getTotalVisitors() + " , totalHits = "
                + csxBeanOld.getTotalHits() + " -> "
                + csxBeanNew.getTotalHits() + " , took " + deltaTime + " ms" );

        // flag as persisted
        if ( !persisted ) {
          persisted = true;
        }

        // give a breath for cpu to process the next xipme code
        Thread.sleep( 50 );
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to persist map report , " + e );
    }

    return persisted;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private List listChannelSessionXipmeBeans( int csId ) {
    List listCsxBeans = new ArrayList();

    if ( csId < 1 ) {
      return listCsxBeans;
    }

    // compose sql
    String sqlSelect = "SELECT gx.xipme_master_code ";
    sqlSelect += ", COUNT(gx.xipme_master_code) AS total ";
    String sqlFrom = "FROM gateway_xipme gx ";
    sqlFrom += "INNER JOIN gateway_log gl ON ( gl.gateway_xipme_id = gx.gateway_xipme_id ) ";
    String sqlWhere = "WHERE ( gl.channel_session_id = " + csId + " ) ";
    String sqlGroup = "GROUP BY gx.xipme_master_code ";
    String sqlOrder = "ORDER BY gx.xipme_master_code ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlGroup + sqlOrder;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listCsxBeans;
    }

    // prepare variable
    String stemp = null;
    ChannelSessionXipmeBean csxBean = null;

    // populate records
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      csxBean = new ChannelSessionXipmeBean();
      csxBean.setChannelSessionId( csId );
      stemp = (String) qi.get( 0 ); // xipme_master code
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        csxBean.setXipmeCode( stemp );
      }

      stemp = (String) qi.get( 1 ); // total
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          csxBean.setTotalMembers( Integer.parseInt( stemp ) );
        } catch ( NumberFormatException e ) {
        }
      }
      listCsxBeans.add( csxBean );
    }

    return listCsxBeans;
  }

}
