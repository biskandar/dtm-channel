package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementReportDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelManagementReportDAO" );

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

  public ChannelManagementReportDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List getCurrentStoppedChannelSessionId( int lastDays , boolean debug ) {
    List list = null;

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session ";

    String sqlDateInterval = "INTERVAL " + lastDays + " DAY ";
    String sqlDateThreshold = "DATE_SUB( CURDATE() , " + sqlDateInterval + ") ";

    String sqlWhereSuspended = "( suspended = 1 ) ";
    sqlWhereSuspended += "AND ( " + sqlDateThreshold + " <= date_suspended ) ";

    String sqlWhereFinished = "( finished = 1 ) ";
    sqlWhereFinished += "AND ( " + sqlDateThreshold + " <= date_finished ) ";

    String sqlWhere = "WHERE ( active = 1 ) AND ( ( " + sqlWhereSuspended
        + " ) OR ( " + sqlWhereFinished + " ) ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult queryResult = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( queryResult != null ) && ( queryResult.size() > 0 ) ) {
      list = populateChannelSessionIdRecords( queryResult );
    }

    return list;
  }

  public List getCurrentFinishedChannelSessionId( int lastDays ) {
    List list = null;

    String sqlSelect = "SELECT id ";
    String sqlFrom = "FROM channel_session ";

    String sqlDateInterval = "INTERVAL " + lastDays + " DAY ";
    String sqlDateThreshold = "DATE_SUB( CURDATE() , " + sqlDateInterval + ") ";
    String sqlWhere = "WHERE ( active = 1 ) AND ( " + sqlDateThreshold
        + " <= date_finished ) ";

    String sql = sqlSelect + sqlFrom + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );

    QueryResult queryResult = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( queryResult != null ) && ( queryResult.size() > 0 ) ) {
      list = populateChannelSessionIdRecords( queryResult );
    }

    return list;
  }

  public long getTotalInsertedInGatewayLog( int channelSessionId , boolean debug ) {
    long totalInserted = 0;

    String sqlSelect = "SELECT COUNT(log_id) AS total ";
    String sqlFrom = "FROM gateway_log ";
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult queryResult = dbLib.simpleQuery( "transactiondb" , sql );
    totalInserted = getLongFirstValue( queryResult );
    return totalInserted;
  }

  public long getTotalInsertedInTransaction( int channelSessionId ,
      boolean debug ) {
    long totalInserted = 0;

    String sqlSelect = "SELECT COUNT(log_id) AS total ";
    String sqlFrom = "FROM `transaction` ";
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult queryResult = dbLib.simpleQuery( "reportdb" , sql );
    totalInserted = getLongFirstValue( queryResult );
    return totalInserted;
  }

  public long getTotalRetryInGatewayLog( int channelSessionId , boolean debug ) {
    long totalRetry = 0;

    String sqlSelect = "SELECT COUNT(log_id) AS retries ";
    String sqlFrom = "FROM gateway_log ";
    String sqlWhere = "WHERE ( retry > 0 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    totalRetry = getLongFirstValue( qr );
    return totalRetry;
  }

  public long getTotalRetryInTransaction( int channelSessionId , boolean debug ) {
    long totalRetry = 0;

    String sqlSelect = "SELECT COUNT(log_id) AS retries ";
    String sqlFrom = "FROM `transaction` ";
    String sqlWhere = "WHERE ( retry_count > 0 ) ";
    sqlWhere += "AND ( channel_session_id = " + channelSessionId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "reportdb" , sql );
    totalRetry = getLongFirstValue( qr );
    return totalRetry;
  }

  public boolean updateChannelSessionReportFromGatewayLog(
      ChannelSessionReportBean csrBean , boolean debug ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }
    if ( csrBean.getChannelSessionId() < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT status , COUNT(log_id) AS total ";
    String sqlFrom = "FROM gateway_log ";
    String sqlWhere = "WHERE ( channel_session_id = "
        + csrBean.getChannelSessionId() + " ) ";
    sqlWhere += "AND ( status IN ( 'SUBMITTED' , 'DELIVERED' , 'FAILED-INVALID' ) ) ";
    String sqlGroup = "GROUP BY status ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlGroup;

    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    result = populateChannelSessionReport1( qr , csrBean );
    return result;
  }

  public boolean updateChannelSessionReportFromTransaction(
      ChannelSessionReportBean csrBean , boolean debug ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }
    if ( csrBean.getChannelSessionId() < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT internal_status_code , COUNT(log_id) AS total ";
    String sqlFrom = "FROM `transaction` ";
    String sqlWhere = "WHERE ( channel_session_id = "
        + csrBean.getChannelSessionId() + " ) ";
    sqlWhere += "AND ( internal_status_code IN ( 'SUBMITTED' , 'DELIVERED' , 'FAILED-INVALID' ) ) ";
    String sqlGroup = "GROUP BY internal_status_code ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlGroup;

    QueryResult qr = dbLib.simpleQuery( "reportdb" , sql );
    result = populateChannelSessionReport1( qr , csrBean );
    return result;
  }

  public boolean deleteChannelSessionReport( int channelSessionId ) {
    boolean result = false;

    String sqlDelete = "DELETE FROM channel_session_report ";
    String sqlWhere = "WHERE channel_session_id = " + channelSessionId + " ";

    String sql = sqlDelete + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );

    Integer iresult = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( iresult != null ) && ( iresult.intValue() > 0 ) ) {
      result = true;
    } else {
      DLog.warning( lctx , "Failed to delete channel session report "
          + ", with channelSessionId = " + channelSessionId );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private boolean populateChannelSessionReport1( QueryResult qr ,
      ChannelSessionReportBean csrBean ) {
    boolean result = false;

    if ( csrBean == null ) {
      return result;
    }
    if ( qr == null ) {
      return result;
    }

    String stemp = null;
    Iterator it = qr.getIterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }

      String status = null;
      int total = 0;

      stemp = (String) qi.get( 0 );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        status = stemp;
      }
      stemp = (String) qi.get( 1 );
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          total = Integer.parseInt( stemp );
        } catch ( NumberFormatException e ) {
        }
      }

      // status must be exist
      if ( StringUtils.isBlank( status ) ) {
        continue;
      }

      result = true;
      if ( status.equals( "SUBMITTED" ) ) {
        csrBean.setTotalDnEmpty( csrBean.getTotalDnEmpty() + total );
      }
      if ( status.equals( "DELIVERED" ) ) {
        csrBean.setTotalDnDelivered( csrBean.getTotalDnDelivered() + total );
      }
      if ( status.equals( "FAILED-INVALID" ) ) {
        csrBean.setTotalDnInvalidNumber( csrBean.getTotalDnInvalidNumber()
            + total );
      }

    } // while ( iterQ.hasNext() ) {

    return result;
  }

  private ChannelSessionReportBean populateChannelSessionReport2(
      QueryResult queryResult ) {
    ChannelSessionReportBean bean = new ChannelSessionReportBean();

    Iterator iterQ = null;
    if ( queryResult != null ) {
      iterQ = queryResult.getIterator();
    }

    QueryItem queryItem = null;
    if ( iterQ.hasNext() ) {
      queryItem = (QueryItem) iterQ.next();
    }

    String stemp;
    int itemp;
    long ltemp;

    if ( queryItem != null ) {

      bean = new ChannelSessionReportBean();

      stemp = (String) queryItem.get( 0 ); // id
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          bean.setId( itemp );
        } catch ( NumberFormatException e ) {
        }
      }

      stemp = (String) queryItem.get( 1 ); // key
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setKey( stemp );
      }
      stemp = (String) queryItem.get( 2 ); // channel_session_id
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          bean.setChannelSessionId( itemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 3 ); // total_sent
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalSent( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 4 ); // total_retry
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalRetry( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 5 ); // total_dn_delivered
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalDnDelivered( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 6 ); // total_dn_failed
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalDnFailed( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 7 ); // total_dn_invalid_number
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalDnInvalidNumber( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 8 ); // total_dn_empty
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalDnEmpty( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 9 ); // total_unsubs
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalUnsubs( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 10 ); // total_global_unsubs
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          ltemp = Long.parseLong( stemp );
          bean.setTotalGlobalUnsubs( ltemp );
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) queryItem.get( 11 ); // active
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setActive( stemp.equals( "1" ) );
      }
      stemp = (String) queryItem.get( 12 ); // date_inserted
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setDateInserted( DateTimeFormat.convertToDate( stemp ) );
      }
      stemp = (String) queryItem.get( 13 ); // date_updated
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setDateUpdated( DateTimeFormat.convertToDate( stemp ) );
      }

    }

    return bean;
  }

  private List populateChannelSessionIdRecords( QueryResult queryResult ) {
    List list = new ArrayList();
    int channelSessionId = 0;
    QueryItem queryItem = null;
    Iterator iterQ = queryResult.getIterator();
    while ( iterQ.hasNext() ) {
      queryItem = (QueryItem) iterQ.next();
      if ( queryItem == null ) {
        continue;
      }
      channelSessionId = (int) getLongFirstValue( queryItem );
      if ( channelSessionId < 1 ) {
        continue;
      }
      list.add( new Integer( channelSessionId ) );
    } // iterate all records
    return list;
  }

  private long getLongFirstValue( QueryResult qr ) {
    long result = 0;
    if ( qr == null ) {
      return result;
    }
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return result;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return result;
    }
    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return result;
    }
    result = getLongFirstValue( qi );
    return result;
  }

  private long getLongFirstValue( QueryItem qi ) {
    long result = 0;
    if ( qi == null ) {
      return result;
    }
    String stemp = qi.getFirstValue();
    if ( ( stemp == null ) || ( stemp.equals( "" ) ) ) {
      return result;
    }
    try {
      result = Long.parseLong( stemp );
    } catch ( NumberFormatException e ) {
    }
    return result;
  }

}
