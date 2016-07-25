package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastHistoryDAO {

  static final DLogContext lctx = new SimpleContext( "ListBroadcastHistoryDAO" );

  private DatabaseLibrary dbLib;

  public ListBroadcastHistoryDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public long totalBeans( int clientId , int top , int limit ) {
    long totalRecords = 0;
    if ( clientId < 1 ) {
      return totalRecords;
    }

    // compose sql
    String sqlSelect = "SELECT COUNT(*) AS total ";
    String sqlFrom = sqlFrom();
    String sqlWhere = sqlWhere( clientId );
    String sql = sqlSelect + sqlFrom + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

    // execute and fetch query
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      Iterator iqr = qr.iterator();
      QueryItem qi = null;
      if ( iqr.hasNext() ) {
        qi = (QueryItem) iqr.next();
      }
      if ( qi != null ) {
        try {
          totalRecords = Long.parseLong( qi.getFirstValue() );
        } catch ( NumberFormatException e ) {
        }
      }
    }

    return totalRecords;
  }

  public List selectBeans( int clientId , int top , int limit ) {
    List listChannelSessionInfoBeans = new ArrayList();
    if ( clientId < 1 ) {
      return listChannelSessionInfoBeans;
    }

    // compose sql
    String sqlSelect = "SELECT cs.id "
        + ", c.client_id , c.company_name , c.payment_type "
        + ", e.event_id , e.event_name "
        + ", sg.id , sg.group_name "
        + ", cs.expect_response , cs.total_numbers , cs.total_remains , cs.debit_units , cs.priority "
        + ", cs.suspended , cs.started , cs.finished , cs.new_status "
        + ", cs.date_started , cs.date_finished "
        + ", cs.date_schedulled_start , cs.date_schedulled_finish ";
    String sqlFrom = sqlFrom();
    String sqlWhere = sqlWhere( clientId );
    String sqlOrderBy = "ORDER BY cs.id DESC ";
    String sqlLimit = "LIMIT " + top + " , " + limit + " ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrderBy + sqlLimit;

    DLog.debug( lctx , "Perform " + sql );

    // execute and fetch query
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      Iterator iqr = qr.iterator();
      QueryItem qi = null;
      while ( iqr.hasNext() ) {
        qi = (QueryItem) iqr.next();
        if ( qi == null ) {
          continue;
        }
        ChannelSessionInfoBean bean = populateBean( qi );
        if ( bean == null ) {
          continue;
        }
        listChannelSessionInfoBeans.add( bean );
      }
    }

    return listChannelSessionInfoBeans;
  }

  private String sqlFrom() {
    String sqlFrom = "FROM client c ";
    sqlFrom += "INNER JOIN event e ON e.client_id = c.client_id ";
    sqlFrom += "INNER JOIN channel_session cs ON cs.event_id = e.event_id ";
    sqlFrom += "LEFT JOIN subscriber_group sg ON cs.group_subscriber_id = sg.id ";
    return sqlFrom;
  }

  private String sqlWhere( int clientId ) {
    String sqlWhere = "WHERE ( cs.started = 1 ) ";
    sqlWhere += "AND ( cs.finished = 1 ) ";
    sqlWhere += "AND ( cs.active = 1 ) ";
    sqlWhere += "AND ( c.client_id = " + clientId + " ) ";
    return sqlWhere;
  }

  private ChannelSessionInfoBean populateBean( QueryItem qi ) {
    String stemp;
    int itemp;
    double ddtemp;

    boolean valid = true;
    ChannelSessionInfoBean bean = new ChannelSessionInfoBean();

    stemp = (String) qi.get( 0 ); // cs.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp < 1 ) {
          DLog.warning( lctx , "Failed to get channel session info "
              + ", found zero channelSessionId" );
          valid = false;
        } else {
          bean.setChannelSessionId( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // c.client_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp < 1 ) {
          DLog.warning( lctx , "Failed to get channel session info "
              + ", found zero clientId" );
          valid = false;
        } else {
          bean.setClientId( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 2 ); // c.company_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setClientName( stemp );
    }
    stemp = (String) qi.get( 3 ); // c.payment_type
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setPaymentType( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 4 ); // e.event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp < 1 ) {
          DLog.warning( lctx , "Failed to get channel session info "
              + ", found zero eventId" );
          valid = false;
        } else {
          bean.setEventId( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 5 ); // e.event_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setEventName( stemp );
    }

    stemp = (String) qi.get( 6 ); // sg.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          bean.setGroupSubscriberId( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 7 ); // sg.group_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setGroupSubscriberName( stemp );
    }

    stemp = (String) qi.get( 8 ); // cs.expect_response
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        bean.setExpectResponse( ddtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // cs.total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          bean.setTotalNumbers( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 10 ); // cs.total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          bean.setTotalRemains( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 11 ); // cs.debit_units
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        if ( ddtemp > 0 ) {
          bean.setDebitUnits( ddtemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 12 ); // cs.priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          bean.setPriority( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 13 ); // cs.suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSuspended( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 14 ); // cs.started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setStarted( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 15 ); // cs.finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFinished( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 16 ); // cs.new_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        if ( itemp > 0 ) {
          bean.setNewStatus( itemp );
        }
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 17 ); // cs.date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateStarted( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) qi.get( 18 ); // cs.date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateFinished( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) qi.get( 19 ); // cs.date_schedulled_start
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledStart( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) qi.get( 20 ); // cs.date_schedulled_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledFinish( DateTimeFormat.convertToDate( stemp ) );
    }

    if ( !valid ) {
      bean = null;
    }

    return bean;
  }

}
