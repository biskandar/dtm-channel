package com.beepcast.channel;

import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelGatewayLogDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelGatewayLogDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;
  private ChannelApp channelApp;
  private boolean debug;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelGatewayLogDAO() {
    dbLib = DatabaseLibrary.getInstance();
    channelApp = ChannelApp.getInstance();
    debug = channelApp.isDebug();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalUnprocessedNumbers( int eventId ) {
    long totalRecords = 0;

    if ( eventId < 1 ) {
      return totalRecords;
    }

    // compose sql

    String sqlSelect = "SELECT COUNT(cl.id) AS total ";
    String sql = sqlSelect + sqlFrom()
        + sqlWhereByEventIdAndEmptyGatewayLog( eventId );

    // query sql

    try {
      totalRecords = Long.parseLong( getFirstValueStr( sql ) );
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to get total unprocessed numbers , " + e );
    }

    return totalRecords;
  }

  public String readStrChannelLogIdsForUnprocessedNumbers( int eventId ) {
    String strChannelLogIds = null;

    if ( eventId < 1 ) {
      return strChannelLogIds;
    }

    // compose sql
    String sqlSelect = "SELECT cl.id ";
    String sql = sqlSelect + sqlFrom()
        + sqlWhereByEventIdAndEmptyGatewayLog( eventId );

    // query sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return strChannelLogIds;
    }

    // read records
    StringBuffer sbChannelLogIds = null;
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      String strChannelLogId = qi.getFirstValue();
      if ( ( strChannelLogId == null ) || ( strChannelLogId.equals( "" ) ) ) {
        continue;
      }
      if ( sbChannelLogIds == null ) {
        sbChannelLogIds = new StringBuffer();
      } else {
        sbChannelLogIds.append( "," );
      }
      sbChannelLogIds.append( strChannelLogId );
    }
    if ( sbChannelLogIds != null ) {
      strChannelLogIds = sbChannelLogIds.toString();
    }

    return strChannelLogIds;
  }

  public long updateMarkedChannelLogForUnprocessedNumbers(
      String strChannelLogIds ) {
    long totalRecords = 0;

    if ( ( strChannelLogIds == null ) || ( strChannelLogIds.equals( "" ) ) ) {
      return totalRecords;
    }

    // compose sql

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET marked = 1 , date_marked = NOW() ";
    String sqlWhere = "WHERE id IN ( " + strChannelLogIds + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
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

  public String sqlFrom() {
    String sqlFrom = "FROM channel_log cl ";
    sqlFrom += "LEFT OUTER JOIN gateway_log gl ";
    sqlFrom += "ON ( gl.channel_session_id = cl.channel_session_id ) ";
    sqlFrom += "AND ( gl.encrypt_phone = cl.encrypt_phone ) ";
    sqlFrom += "AND ( gl.message_id = cl.message_id ) ";
    return sqlFrom;
  }

  public String sqlWhereByEventIdAndEmptyGatewayLog( int eventId ) {
    String sqlWhere = "WHERE ( cl.event_id = " + eventId + " ) ";
    sqlWhere += "AND ( gl.log_id IS NULL ) ";
    return sqlWhere;
  }

  public String getFirstValueStr( String sql ) {
    String strResult = null;
    if ( sql == null ) {
      return strResult;
    }
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return strResult;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return strResult;
    }
    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return strResult;
    }
    strResult = qi.getFirstValue();
    return strResult;
  }

}
