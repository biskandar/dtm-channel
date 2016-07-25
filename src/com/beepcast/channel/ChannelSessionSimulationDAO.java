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

public class ChannelSessionSimulationDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSimulationDAO" );

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

  public ChannelSessionSimulationDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List getAvailableChannelSessionSimulations( boolean debug , int limit ) {
    List cssBeans = new ArrayList();

    String sqlSelectFrom = sqlSelectFrom();

    String sqlWhere = "WHERE ";

    String sqlWhereActive = "( active = 1 ) ";

    String sqlWhereDemandStatus = "( new_status IN ( "
        + ChannelSessionNewStatus.ST_STARTED + " , "
        + ChannelSessionNewStatus.ST_SUSPENDED + " , "
        + ChannelSessionNewStatus.ST_FINISHED + " ) ) ";

    sqlWhere += sqlWhereActive;
    sqlWhere += "AND " + sqlWhereDemandStatus;

    String sqlOrder = "ORDER BY priority DESC , id ASC ";
    String sqlLimit = "LIMIT " + limit + " ";

    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return cssBeans;
    }

    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      ChannelSessionSimulationBean cssBean = populateRecord( qi );
      if ( cssBean == null ) {
        continue;
      }
      cssBeans.add( cssBean );
    }

    return cssBeans;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateNewStatus( int channelSessionSimulationId , int newStatus ) {
    boolean result = false;

    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sql = "UPDATE channel_session_simulation ";
    sql += "SET new_status = " + newStatus + " , date_updated = NOW() ";
    sql += "WHERE ( id = " + channelSessionSimulationId + " ) ";

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateStartedChannelSessionSimulation(
      int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET suspended = 0 , started = 1 ";
    String sqlWhere = "WHERE ( id = " + channelSessionSimulationId + " ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateStartedChannelSessionSimulation(
      int channelSessionSimulationId , long totalSubscribers ,
      double debitUnits , Date dateStarted ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    dateStarted = ( dateStarted == null ) ? new Date() : dateStarted;
    String strDateStarted = DateTimeFormat.convertToString( dateStarted );

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET suspended = 0 , started = 1 ";
    sqlSet += ", date_started = '" + strDateStarted + "' ";
    sqlSet += ", total_numbers = " + totalSubscribers + " ";
    sqlSet += ", total_remains = " + totalSubscribers + " ";
    sqlSet += ", debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionSimulationId + " ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateFinishedChannelSessionSimulation(
      ChannelSessionSimulationBean cssBean ) {
    boolean result = false;

    if ( cssBean == null ) {
      return result;
    }

    int channelSessionSimulationId = cssBean.getId();
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    long totalRemains = cssBean.getTotalRemains();
    double debitUnits = cssBean.getDebitUnits();

    Date dateFinished = cssBean.getDateFinished();
    dateFinished = ( dateFinished == null ) ? new Date() : dateFinished;
    String strDateFinished = DateTimeFormat.convertToString( dateFinished );

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET suspended = 0 , finished = 1 ";
    sqlSet += ", date_finished = '" + strDateFinished + "' ";
    sqlSet += ", total_remains = " + totalRemains + " ";
    sqlSet += ", debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionSimulationId + " ) ";

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

  public boolean updateTotalRemains( int channelSessionSimulationId ,
      long totalRemains ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET total_remains = " + totalRemains + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionSimulationId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }
    return result;
  }

  public boolean updateTotalRemainsAndDebitUnits(
      int channelSessionSimulationId , long totalRemains , double debitUnits ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET total_remains = " + totalRemains + " ";
    sqlSet += " , debit_units = " + debitUnits + " ";
    String sqlWhere = "WHERE ( id = " + channelSessionSimulationId + " ) ";
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
  // Support Select Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean isSuspended( boolean debug , int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session_simulation ";
    String sqlWhere = "WHERE ( suspended = 1 ) ";
    sqlWhere += "AND ( id = " + channelSessionSimulationId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    result = isAnyRecords( sql );
    return result;
  }

  public boolean isFinished( boolean debug , int channelSessionSimulationId ) {
    boolean result = false;
    if ( channelSessionSimulationId < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session_simulation ";
    String sqlWhere = "WHERE ( finished = 1 ) ";
    sqlWhere += "AND ( id = " + channelSessionSimulationId + " ) ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    result = isAnyRecords( sql );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Backend Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionSimulationBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      return result;
    }

    // read params

    String simulationType = bean.getSimulationType();
    long simulationNumbers = bean.getSimulationNumbers();
    int channelSessionId = bean.getChannelSessionId();
    long totalNumbers = bean.getTotalNumbers();
    long totalRemains = bean.getTotalRemains();
    double debitUnits = bean.getDebitUnits();
    int priority = bean.getPriority();
    int suspended = bean.isSuspended() ? 1 : 0;
    int started = bean.isStarted() ? 1 : 0;
    int finished = bean.isFinished() ? 1 : 0;
    int newStatus = bean.getNewStatus();
    Date dateStarted = bean.getDateStarted();
    Date dateSuspended = bean.getDateSuspended();
    Date dateFinished = bean.getDateFinished();

    // clean params

    simulationType = ( simulationType == null ) ? "" : simulationType.trim();
    String strDateStarted = ( dateStarted == null ) ? "NULL" : "'"
        + DateTimeFormat.convertToString( dateStarted ) + "'";
    String strDateSuspended = ( dateSuspended == null ) ? "NULL" : "'"
        + DateTimeFormat.convertToString( dateSuspended ) + "'";
    String strDateFinished = ( dateFinished == null ) ? "NULL" : "'"
        + DateTimeFormat.convertToString( dateFinished ) + "'";

    // compose sql

    String sqlInsert = "INSERT INTO channel_session_simulation(simulation_type";
    sqlInsert += ",simulation_numbers,channel_session_id,total_numbers";
    sqlInsert += ",total_remains,debit_units,priority,suspended,started";
    sqlInsert += ",finished,new_status,date_started,date_suspended,date_finished";
    sqlInsert += ",active,date_inserted,date_updated) ";
    String sqlValues = "VALUES('"
        + StringEscapeUtils.escapeSql( simulationType ) + "',"
        + simulationNumbers + "," + channelSessionId + "," + totalNumbers + ","
        + totalRemains + "," + debitUnits + "," + priority + "," + suspended
        + "," + started + "," + finished + "," + newStatus + ","
        + strDateStarted + "," + strDateSuspended + "," + strDateFinished
        + ",1,NOW(),NOW()) ";
    String sql = sqlInsert + sqlValues;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public ChannelSessionSimulationBean selectByChannelSessionId( boolean debug ,
      int channelSessionId ) {
    ChannelSessionSimulationBean bean = null;
    if ( channelSessionId < 1 ) {
      return bean;
    }

    // compose sql
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    String sqlOrder = "ORDER BY id DESC ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return bean;
    }
    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return bean;
    }

    // populate record
    bean = populateRecord( qi );

    return bean;
  }

  public boolean deleteById( int id ) {
    boolean result = false;

    // compose sql

    String sqlUpdate = "UPDATE channel_session_simulation ";
    String sqlSet = "SET active = 0 , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + id + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "profiledb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean isAnyRecords( String sql ) {
    boolean result = false;
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      result = true;
    }
    return result;
  }

  private String sqlSelectFrom() {
    String sqlSelect = "SELECT id , simulation_type , simulation_numbers ";
    sqlSelect += ", channel_session_id , total_numbers , total_remains ";
    sqlSelect += ", debit_units , priority , suspended , started ";
    sqlSelect += ", finished , new_status , date_started ";
    sqlSelect += ", date_suspended , date_finished , active ";
    String sqlFrom = "FROM channel_session_simulation ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  private ChannelSessionSimulationBean populateRecord( QueryItem qi ) {
    ChannelSessionSimulationBean bean = null;
    if ( qi == null ) {
      return bean;
    }

    String stemp;

    bean = new ChannelSessionSimulationBean();

    stemp = (String) qi.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // simulation_type
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSimulationType( stemp );
    }

    stemp = (String) qi.get( 2 ); // simulation_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setSimulationNumbers( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 3 ); // channel_session_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setChannelSessionId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 4 ); // total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalNumbers( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 5 ); // total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalRemains( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 6 ); // debit_units
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setDebitUnits( Double.parseDouble( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 7 ); // priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setPriority( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSuspended( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 9 ); // started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setStarted( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 10 ); // finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFinished( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 11 ); // new_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setNewStatus( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 12 ); // date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateStarted( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 13 ); // date_suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSuspended( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 14 ); // date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateFinished( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 15 ); // active
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setActive( stemp.equals( "1" ) );
    }

    return bean;
  }

}
