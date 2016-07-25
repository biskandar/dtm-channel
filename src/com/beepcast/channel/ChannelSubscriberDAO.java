package com.beepcast.channel;

import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSubscriberDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSubscriberDAO" );

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

  public ChannelSubscriberDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalActiveSubscriber( int subscriberGroupId , boolean debug ) {
    long total = 0;

    String sqlSelect = "SELECT COUNT(id) AS total ";
    String sqlFrom = "FROM client_subscriber ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( ( subscribed = 1 ) AND ( global_subscribed = 1 ) ) ";
    sqlWhere += "AND ( subscriber_group_id = " + subscriberGroupId + " ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    total = getFirstValue( sql , debug );

    return total;
  }

  public long getTotalUnsubscribedFromSubscriberGroupId( int subscriberGroupId ,
      boolean debug ) {
    long total = 0;

    String sqlSelect = "SELECT COUNT(id) AS total ";
    String sqlFrom = "FROM client_subscriber ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( subscribed = 0 ) ";
    sqlWhere += "AND ( subscriber_group_id = " + subscriberGroupId + " ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    total = getFirstValue( sql , debug );

    return total;
  }

  public long getTotalUnsubscribedFromEventId( int eventId , boolean debug ) {
    long total = 0;

    String sqlSelect = "SELECT COUNT(id) AS total ";
    String sqlFrom = "FROM client_subscriber ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( subscribed = 0 ) ";
    sqlWhere += "AND ( subscribed_event_id = " + eventId + " ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    total = getFirstValue( sql , debug );

    return total;
  }

  public long getTotalGlobalUnsubscribed( int subscriberGroupId , boolean debug ) {
    long total = 0;

    String sqlSelect = "SELECT COUNT(id) AS total ";
    String sqlFrom = "FROM client_subscriber ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( global_subscribed = 0 ) AND ( subscribed = 1 ) ";
    sqlWhere += "AND ( subscriber_group_id = " + subscriberGroupId + " ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    total = getFirstValue( sql , debug );

    return total;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private long getFirstValue( String sql , boolean debug ) {
    long value = 0;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return value;
    }

    Iterator iter = qr.iterator();
    if ( !iter.hasNext() ) {
      return value;
    }

    QueryItem qi = (QueryItem) iter.next();
    if ( qi == null ) {
      return value;
    }

    String stemp = qi.getFirstValue();
    if ( ( stemp == null ) || ( stemp.equals( "" ) ) ) {
      return value;
    }

    try {
      value = Long.parseLong( stemp );
    } catch ( NumberFormatException e ) {
      return value;
    }

    return value;
  }

}
