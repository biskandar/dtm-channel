package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSessionDAO" );

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

  public ChannelSessionDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insertChannelSession( ChannelSessionBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // clean params

    int eventId = bean.getEventId();
    int groupSubscriberId = bean.getGroupSubscriberId();
    int allowDuplicated = bean.isAllowDuplicated() ? 1 : 0;
    int useDateSend = bean.isUseDateSend() ? 1 : 0;
    double expectResponse = bean.getExpectResponse();
    long totalNumbers = bean.getTotalNumbers();
    long totalRemains = bean.getTotalRemains();
    double debitUnits = bean.getDebitUnits();
    int priority = bean.getPriority();
    int suspended = bean.isSuspended() ? 1 : 0;
    int started = bean.isStarted() ? 1 : 0;
    int finished = bean.isFinished() ? 1 : 0;
    int newStatus = bean.getNewStatus();
    int active = bean.isActive() ? 1 : 0;

    String dateStarted = DateTimeFormat.convertToString( bean.getDateStarted() );
    dateStarted = ( dateStarted != null ) ? "'" + dateStarted + "'" : "NULL";

    String dateFinished = DateTimeFormat.convertToString( bean
        .getDateFinished() );
    dateFinished = ( dateFinished != null ) ? "'" + dateFinished + "'" : "NULL";

    String dateSuspended = DateTimeFormat.convertToString( bean
        .getDateSuspended() );
    dateSuspended = ( dateSuspended != null ) ? "'" + dateSuspended + "'"
        : "NULL";

    String dateSchedulledStart = DateTimeFormat.convertToString( bean
        .getDateSchedulledStart() );
    dateSchedulledStart = ( dateSchedulledStart != null ) ? "'"
        + dateSchedulledStart + "'" : "NULL";

    String dateSchedulledFinish = DateTimeFormat.convertToString( bean
        .getDateSchedulledFinish() );
    dateSchedulledFinish = ( dateSchedulledFinish != null ) ? "'"
        + dateSchedulledFinish + "'" : "NULL";

    // compose sql

    String sqlInsert = "INSERT INTO channel_session ( event_id , group_subscriber_id ";
    sqlInsert += ", allow_duplicated , use_date_send , expect_response , total_numbers ";
    sqlInsert += ", total_remains , debit_units , priority , suspended , started , finished ";
    sqlInsert += ", new_status , date_started , date_suspended , date_finished ";
    sqlInsert += ", date_schedulled_start , date_schedulled_finish , active , date_inserted ";
    sqlInsert += ", date_updated ) ";

    String sqlValues = "VALUES ( " + eventId + " , " + groupSubscriberId
        + " , " + allowDuplicated + " , " + useDateSend + " , "
        + expectResponse + " , " + totalNumbers + " , " + totalRemains + " , "
        + debitUnits + " , " + priority + " , " + suspended + " , " + started
        + " , " + finished + " , " + newStatus + " , " + dateStarted + " , "
        + dateSuspended + " , " + dateFinished + " , " + dateSchedulledStart
        + " , " + dateSchedulledFinish + " , " + active + " , NOW() , NOW() ) ";

    String sql = sqlInsert + sqlValues;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean deleteChannelSession( int channelSessionId ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    // execute sql
    String sql = "UPDATE channel_session ";
    sql += "SET active = 0 , date_updated = NOW() ";
    sql += "WHERE ( id = " + channelSessionId + " ) ";

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateChannelSession1( int channelSessionId ,
      int groupSubscriberId , boolean allowDuplicated , boolean useDateSend ,
      double expectResponse , long totalNumbers , long totalRemains ,
      double debitUnits , int newStatus , Date dateSchedulledStart ,
      Date dateSchedulledFinish ) {
    boolean updated = false;

    String strDateSchedulledStart = DateTimeFormat
        .convertToString( dateSchedulledStart );
    strDateSchedulledStart = ( strDateSchedulledStart != null ) ? "'"
        + strDateSchedulledStart + "'" : "NULL";

    String strDateSchedulledFinish = DateTimeFormat
        .convertToString( dateSchedulledFinish );
    strDateSchedulledFinish = ( strDateSchedulledFinish != null ) ? "'"
        + strDateSchedulledFinish + "'" : "NULL";

    // compose sql
    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET group_subscriber_id = " + groupSubscriberId;
    sqlSet += " , allow_duplicated = " + ( allowDuplicated ? 1 : 0 );
    sqlSet += " , use_date_send = " + ( useDateSend ? 1 : 0 );
    sqlSet += " , expect_response = " + expectResponse;
    sqlSet += " , total_numbers = " + totalNumbers;
    sqlSet += " , total_remains = " + totalRemains;
    sqlSet += " , debit_units = " + debitUnits;
    sqlSet += " , new_status = " + newStatus;
    sqlSet += " , date_schedulled_start = " + strDateSchedulledStart;
    sqlSet += " , date_schedulled_finish = " + strDateSchedulledFinish;
    sqlSet += " , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      updated = true;
    }

    return updated;
  }

  public boolean updateChannelSession2( int channelSessionId ,
      double expectResponse , int newStatus , Date dateSchedulledStart ,
      Date dateSchedulledFinish ) {
    boolean updated = false;

    String strDateSchedulledStart = DateTimeFormat
        .convertToString( dateSchedulledStart );
    strDateSchedulledStart = ( strDateSchedulledStart != null ) ? "'"
        + strDateSchedulledStart + "'" : "NULL";

    String strDateSchedulledFinish = DateTimeFormat
        .convertToString( dateSchedulledFinish );
    strDateSchedulledFinish = ( strDateSchedulledFinish != null ) ? "'"
        + strDateSchedulledFinish + "'" : "NULL";

    // compose sql
    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET expect_response = " + expectResponse;
    sqlSet += " , new_status = " + newStatus;
    sqlSet += " , date_schedulled_start = " + strDateSchedulledStart;
    sqlSet += " , date_schedulled_finish = " + strDateSchedulledFinish;
    sqlSet += " , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      updated = true;
    }

    return updated;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int restartChannelSessions() {
    int totalRecords = 0;

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET new_status = " + ChannelSessionNewStatus.ST_STARTED
        + " , suspended = 1 , date_suspended = NOW() , date_updated = NOW() ";
    String sqlWhere = "WHERE ( active = 1 ) AND ( suspended = 0 ) "
        + "AND ( started = 1 ) AND ( finished = 0 ) AND ( new_status = "
        + ChannelSessionNewStatus.ST_EMPTY + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.intValue();
    }

    return totalRecords;
  }

  public boolean updateNewStatus( int channelSessionId , int newStatus ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET new_status = " + newStatus
        + " , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateStartedChannelSession( int channelSessionId ,
      long totalSubscribers , double debitUnits , Date dateStarted ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    if ( totalSubscribers < 1 ) {
      return result;
    }

    dateStarted = ( dateStarted == null ) ? new Date() : dateStarted;
    String strDateStarted = DateTimeFormat.convertToString( dateStarted );

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET suspended = 0 , started = 1 ";
    sqlSet += ", date_started = '" + strDateStarted + "' ";
    sqlSet += ", total_numbers = " + totalSubscribers + " ";
    sqlSet += ", total_remains = " + totalSubscribers + " ";
    sqlSet += ", debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateStartedChannelSession( int channelSessionId ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET suspended = 0 , started = 1 ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateSuspendedChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;

    if ( csBean == null ) {
      return result;
    }

    int channelSessionId = csBean.getId();
    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET suspended = 1 , date_suspended = NOW() ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateFinishedChannelSession( ChannelSessionBean csBean ) {
    boolean result = false;

    if ( csBean == null ) {
      return result;
    }

    int channelSessionId = csBean.getId();
    if ( channelSessionId < 1 ) {
      return result;
    }

    long totalRemains = csBean.getTotalRemains();
    double debitUnits = csBean.getDebitUnits();

    Date dateFinished = csBean.getDateFinished();
    dateFinished = ( dateFinished == null ) ? new Date() : dateFinished;
    String strDateFinished = DateTimeFormat.convertToString( dateFinished );

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET suspended = 0 , finished = 1 ";
    sqlSet += ", date_finished = '" + strDateFinished + "' ";
    sqlSet += ", total_remains = " + totalRemains + " ";
    sqlSet += ", debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateTotalRemains( int channelSessionId , long totalRemains ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET total_remains = " + totalRemains + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateTotalRemainsAndDebitUnits( int channelSessionId ,
      long totalRemains , double debitUnits ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session ";
    String sqlSet = "SET total_remains = " + totalRemains + " ";
    sqlSet += " , debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelSessionBean getChannelSession( boolean debug ,
      int channelSessionId ) {
    ChannelSessionBean channelSessionBean = null;
    if ( channelSessionId < 1 ) {
      return channelSessionBean;
    }

    // compose sql
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( id = " + channelSessionId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlLimit;

    // execute sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return channelSessionBean;
    }

    // populate record
    Iterator iter = qr.iterator();
    if ( iter.hasNext() ) {
      channelSessionBean = populatedRecord( (QueryItem) iter.next() );
    }

    return channelSessionBean;
  }

  public ChannelSessionBean getLastChannelSession( boolean debug , int eventId ) {
    ChannelSessionBean channelSessionBean = null;

    if ( eventId < 1 ) {
      return channelSessionBean;
    }

    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    String sqlOrder = "ORDER BY id DESC ";
    String sqlLimit = "LIMIT 1 ";

    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      QueryItem qi = null;
      Iterator iter = qr.iterator();
      if ( iter.hasNext() ) {
        qi = (QueryItem) iter.next();
      }
      if ( qi != null ) {
        channelSessionBean = populatedRecord( qi );
      }
    }

    return channelSessionBean;
  }

  public List getAvailableChannelSessions( boolean debug , int limit ) {
    List csBeans = null;

    String sqlSelectFrom = sqlSelectFrom();

    String sqlWhere = "WHERE ";

    String sqlWhereActive = "( active = 1 ) ";

    String sqlWhereDemandStatus = "( new_status IN ( "
        + ChannelSessionNewStatus.ST_STARTED + " , "
        + ChannelSessionNewStatus.ST_SUSPENDED + " , "
        + ChannelSessionNewStatus.ST_FINISHED + " ) ) ";

    String sqlWhereTimedStartStatus = "( ( new_status = "
        + ChannelSessionNewStatus.ST_SCHEDULLED + " ) "
        + "AND ( date_schedulled_start IS NOT NULL ) "
        + "AND ( date_schedulled_start < NOW() ) ) ";

    sqlWhere += sqlWhereActive;
    sqlWhere += "AND ( " + sqlWhereDemandStatus;
    sqlWhere += "OR " + sqlWhereTimedStartStatus + ") ";

    String sqlOrder = "ORDER BY priority DESC , id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";

    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    csBeans = populateRecords( debug , sql );
    return csBeans;
  }

  public List getIdleScheduledChannelSessions( boolean debug , int limit ) {
    List csBeans = null;
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( suspended = 0 ) ";
    sqlWhere += "AND ( schedulled = 0 ) ";
    sqlWhere += "AND ( finished = 0 ) ";
    sqlWhere += "AND ( new_status = " + ChannelSessionNewStatus.ST_EMPTY
        + " ) ";
    sqlWhere += "AND ( date_schedulled_start IS NOT NULL ) ";
    String sqlOrder = "ORDER BY priority DESC , id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;
    csBeans = populateRecords( debug , sql );
    return csBeans;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isSuspended( boolean debug , int channelSessionId ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session ";
    String sqlWhere = "WHERE ( suspended = 1 ) ";
    sqlWhere += "AND ( id = " + channelSessionId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean isFinished( boolean debug , int channelSessionId ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session ";
    String sqlWhere = "WHERE ( finished = 1 ) ";
    sqlWhere += "AND ( id = " + channelSessionId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlSelectFrom() {
    String sqlSelect = "SELECT id , event_id , group_subscriber_id ";
    sqlSelect += ", allow_duplicated , use_date_send , expect_response ";
    sqlSelect += ", total_numbers , total_remains , debit_units , priority ";
    sqlSelect += ", suspended , started , finished , new_status , date_started ";
    sqlSelect += ", date_suspended , date_finished , date_schedulled_start ";
    sqlSelect += ", date_schedulled_finish , active ";
    String sqlFrom = "FROM channel_session ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  private List populateRecords( boolean debug , String sql ) {
    List csBeans = new ArrayList();

    if ( debug ) {
      DLog.debug( lctx , "Performed " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return csBeans;
    }

    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      ChannelSessionBean csBean = populatedRecord( qi );
      if ( csBean == null ) {
        continue;
      }
      csBeans.add( csBean );
    }

    return csBeans;
  }

  private ChannelSessionBean populatedRecord( QueryItem qi ) {
    ChannelSessionBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    String stemp;
    int itemp;
    double dtemp;
    long ltemp;

    bean = new ChannelSessionBean();

    stemp = (String) qi.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setEventId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 2 ); // group_subscriber_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setGroupSubscriberId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 3 ); // allow_duplicated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setAllowDuplicated( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 4 ); // use_date_send
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setUseDateSend( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 5 ); // expect_response
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        dtemp = Double.parseDouble( stemp );
        bean.setExpectResponse( dtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 6 ); // total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ltemp = Long.parseLong( stemp );
        bean.setTotalNumbers( ltemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 7 ); // total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ltemp = Long.parseLong( stemp );
        bean.setTotalRemains( ltemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // debit_units
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        dtemp = Double.parseDouble( stemp );
        bean.setDebitUnits( dtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setPriority( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 10 ); // suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSuspended( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 11 ); // started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setStarted( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 12 ); // finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFinished( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 13 ); // new_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setNewStatus( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 14 ); // date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateStarted( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 15 ); // date_suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSuspended( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 16 ); // date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateFinished( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 17 ); // date_schedulled_start
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledStart( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 18 ); // date_schedulled_finish
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledFinish( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 19 ); // active
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setActive( stemp.equals( "1" ) );
    }

    return bean;
  }

}
