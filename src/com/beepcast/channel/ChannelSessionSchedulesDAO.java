package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSchedulesDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSchedulesDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionSchedulesDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List select( int channelSessionId ) {
    List list = new ArrayList();
    if ( channelSessionId < 1 ) {
      return list;
    }

    // compose sql
    String sqlSelectFrom = ChannelSessionScheduleQuery.sqlSelectFrom();
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    String sqlOrder = "ORDER BY date_started ASC ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder;

    // execute sql
    // DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return list;
    }

    // populate record
    ChannelSessionScheduleBean bean = null;
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      bean = ChannelSessionScheduleQuery.populateRecord( qi );
      if ( bean == null ) {
        continue;
      }
      list.add( bean );
    }

    return list;
  }

  public int insert( List listBeans , int channelSessionId ) {
    int totalRecords = 0;
    if ( listBeans == null ) {
      return totalRecords;
    }

    StringBuffer sbValues = null;
    ChannelSessionScheduleBean bean = null;
    Iterator iterBeans = listBeans.iterator();
    while ( iterBeans.hasNext() ) {
      bean = (ChannelSessionScheduleBean) iterBeans.next();
      if ( bean == null ) {
        continue;
      }

      Date dateStarted = bean.getDateStarted();
      Date dateStopped = bean.getDateStopped();
      String dateStartedStr = DateTimeFormat.convertToString( dateStarted );
      String dateStoppedStr = DateTimeFormat.convertToString( dateStopped );
      dateStartedStr = ( dateStartedStr == null ) ? "NULL" : "'"
          + dateStartedStr + "'";
      dateStoppedStr = ( dateStoppedStr == null ) ? "NULL" : "'"
          + dateStoppedStr + "'";
      String marked = bean.getMarked();
      marked = ( marked == null ) ? "" : marked.trim();

      if ( sbValues == null ) {
        sbValues = new StringBuffer();
        sbValues.append( "VALUES " );
      } else {
        sbValues.append( "," );
      }
      sbValues.append( "(" );
      if ( channelSessionId > 0 ) {
        sbValues.append( channelSessionId );
      } else {
        sbValues.append( bean.getChannelSessionId() );
      }
      sbValues.append( "," );
      sbValues.append( dateStartedStr );
      sbValues.append( "," );
      sbValues.append( dateStoppedStr );
      sbValues.append( ",'" );
      sbValues.append( StringEscapeUtils.escapeSql( marked ) );
      sbValues.append( "',1,NOW(),NOW())" );
    }

    if ( sbValues == null ) {
      return totalRecords;
    }

    // compose sql
    String sqlInsert = "INSERT INTO channel_session_schedule ";
    sqlInsert += "(channel_session_id,date_started,date_stopped";
    sqlInsert += ",marked,active,date_inserted,date_updated) ";
    String sql = sqlInsert + sbValues.toString();

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.intValue();
    }

    return totalRecords;
  }

  public int delete( int channelSessionId ) {
    int totalRecords = 0;
    if ( channelSessionId < 1 ) {
      return totalRecords;
    }

    // compose sql
    String sqlDelete = "DELETE FROM channel_session_schedule ";
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) ";
    String sql = sqlDelete + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.intValue();
    }

    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
