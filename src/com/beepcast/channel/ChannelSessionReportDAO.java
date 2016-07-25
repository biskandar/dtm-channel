package com.beepcast.channel;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionReportDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSessionReportDAO" );

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

  public ChannelSessionReportDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insert( ChannelSessionReportBean csrBean ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }

    // read and clean params

    String key = StringUtils.trimToEmpty( csrBean.getKey() );
    int channelSessionId = csrBean.getChannelSessionId();

    // compose sql

    String sqlInsert = "INSERT INTO channel_session_report "
        + "( `key` , channel_session_id , total_sent , total_retry "
        + " , total_dn_delivered , total_dn_failed , total_dn_invalid_number , total_dn_empty "
        + ", total_unsubs , total_global_unsubs "
        + ", active , date_inserted , date_updated ) ";
    String sqlValues = "VALUES ( '" + key + "' , " + channelSessionId + " , "
        + csrBean.getTotalSent() + " , " + csrBean.getTotalRetry() + " , "
        + csrBean.getTotalDnDelivered() + " , " + csrBean.getTotalDnFailed()
        + " , " + csrBean.getTotalDnInvalidNumber() + " , "
        + csrBean.getTotalDnEmpty() + " , " + csrBean.getTotalUnsubs() + " , "
        + csrBean.getTotalGlobalUnsubs() + " , 1 , NOW() , NOW() ) ";
    String sql = sqlInsert + sqlValues;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public ChannelSessionReportBean selectByChannelSessionId( int channelSessionId ) {
    ChannelSessionReportBean csrBean = null;

    // compose sql
    String sqlSelect = "SELECT id , `key` , channel_session_id , total_sent ";
    sqlSelect += ", total_retry , total_dn_delivered , total_dn_failed ";
    sqlSelect += ", total_dn_invalid_number , total_dn_empty , total_unsubs ";
    sqlSelect += ", total_global_unsubs , active , date_inserted , date_updated ";
    String sqlFrom = "FROM channel_session_report ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    String sqlOrder = "ORDER BY id DESC ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return csrBean;
    }

    // populate record
    Iterator it = qr.iterator();
    if ( it.hasNext() ) {
      csrBean = populateRecord( (QueryItem) it.next() );
    }

    return csrBean;
  }

  public boolean update( ChannelSessionReportBean csrBean ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }

    // compose sql

    String sqlUpdate = "UPDATE channel_session_report ";
    String sqlSet = "SET total_sent = " + csrBean.getTotalSent() + " , ";
    sqlSet += "total_retry = " + csrBean.getTotalRetry() + " , ";
    sqlSet += "total_dn_delivered = " + csrBean.getTotalDnDelivered() + " , ";
    sqlSet += "total_dn_failed = " + csrBean.getTotalDnFailed() + " , ";
    sqlSet += "total_dn_invalid_number = " + csrBean.getTotalDnInvalidNumber()
        + " , ";
    sqlSet += "total_dn_empty = " + csrBean.getTotalDnEmpty() + " , ";
    sqlSet += "total_unsubs = " + csrBean.getTotalUnsubs() + " , ";
    sqlSet += "total_global_unsubs = " + csrBean.getTotalGlobalUnsubs() + " , ";
    sqlSet += "date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + csrBean.getId() + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer iresult = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateDateInserted( int channelSessionId , Date dateInserted ) {
    boolean result = false;

    // read and clean params

    dateInserted = ( dateInserted == null ) ? new Date() : dateInserted;

    // compose sql

    String sqlUpdate = "UPDATE channel_session_report ";
    String sqlSet = "SET date_inserted = '"
        + DateTimeFormat.convertToString( dateInserted ) + "' ";
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelSessionReportBean populateRecord( QueryItem qi ) {
    ChannelSessionReportBean csrBean = null;

    if ( qi == null ) {
      return csrBean;
    }

    String stemp = null;

    csrBean = new ChannelSessionReportBean();

    stemp = (String) qi.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // key
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      csrBean.setKey( stemp );
    }

    stemp = (String) qi.get( 2 ); // channel_session_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setChannelSessionId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 3 ); // total_sent
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalSent( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 4 ); // total_retry
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalRetry( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 5 ); // total_dn_delivered
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalDnDelivered( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 6 ); // total_dn_failed
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalDnFailed( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 7 ); // total_dn_invalid_number
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalDnInvalidNumber( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // total_dn_empty
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalDnEmpty( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // total_unsubs
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalUnsubs( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 10 ); // total_global_unsubs
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        csrBean.setTotalGlobalUnsubs( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 11 ); // active
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      csrBean.setActive( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 12 ); // date_inserted
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      csrBean.setDateInserted( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 13 ); // date_updated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      csrBean.setDateUpdated( DateTimeFormat.convertToDate( stemp ) );
    }

    return csrBean;
  }

}
