package com.beepcast.channel;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionScheduleDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionScheduleDAO" );

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

  public ChannelSessionScheduleDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionScheduleBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      return result;
    }

    Date dateStarted = bean.getDateStarted();
    Date dateStopped = bean.getDateStopped();
    String dateStartedStr = DateTimeFormat.convertToString( dateStarted );
    String dateStoppedStr = DateTimeFormat.convertToString( dateStopped );
    dateStartedStr = ( dateStartedStr == null ) ? "NULL" : "'" + dateStartedStr
        + "'";
    dateStoppedStr = ( dateStoppedStr == null ) ? "NULL" : "'" + dateStoppedStr
        + "'";
    String marked = bean.getMarked();
    marked = ( marked == null ) ? "" : marked.trim();

    // compose sql
    String sqlInsert = "INSERT INTO channel_session_schedule ";
    sqlInsert += "(channel_session_id,date_started,date_stopped";
    sqlInsert += ",marked,active,date_inserted,date_updated) ";
    String sqlValues = "VALUES (" + bean.getChannelSessionId() + ","
        + dateStartedStr + "," + dateStoppedStr + ",'"
        + StringEscapeUtils.escapeSql( marked ) + "',1,NOW(),NOW()) ";
    String sql = sqlInsert + sqlValues;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( ( irslt == null ) || ( irslt.intValue() < 1 ) ) {
      return result;
    }

    result = true;
    return result;
  }

  public boolean updateInactive( int id ) {
    boolean result = false;
    if ( id < 1 ) {
      return result;
    }

    // compose sql
    String sqlUpdate = "UPDATE channel_session_schedule ";
    String sqlSet = "SET active = 0 , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + id + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( ( irslt == null ) || ( irslt.intValue() < 1 ) ) {
      return result;
    }

    result = true;
    return result;
  }

  public ChannelSessionScheduleBean selectNext( int channelSessionId ,
      Date dateAfter ) {
    ChannelSessionScheduleBean bean = null;
    if ( channelSessionId < 1 ) {
      return bean;
    }

    dateAfter = ( dateAfter == null ) ? new Date() : dateAfter;
    String dateAfterStr = DateTimeFormat.convertToString( dateAfter );

    // compose sql
    String sqlSelectFrom = ChannelSessionScheduleQuery.sqlSelectFrom();
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    sqlWhere += "AND ( date_started > '" + dateAfterStr + "' ) ";
    String sqlOrder = "ORDER BY date_started ASC ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql
    // DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return bean;
    }
    QueryItem qi = (QueryItem) it.next();

    // populate record
    bean = ChannelSessionScheduleQuery.populateRecord( qi );

    return bean;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

}
