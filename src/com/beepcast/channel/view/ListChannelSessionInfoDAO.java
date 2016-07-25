package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListChannelSessionInfoDAO {

  static final DLogContext lctx = new SimpleContext(
      "ListChannelSessionInfoDAO" );

  private static final String QUERY_SELECT = "SELECT cs.id "
      + ", c.client_id , c.company_name , c.payment_type "
      + ", e.event_id , e.event_name , sg.id , sg.group_name "
      + ", cs.allow_duplicated , cs.use_date_send , cs.expect_response "
      + ", cs.total_numbers , cs.total_remains , cs.debit_units , cs.priority "
      + ", cs.suspended , cs.started , cs.finished , cs.new_status "
      + ", cs.date_started , cs.date_finished "
      + ", cs.date_schedulled_start , cs.date_schedulled_finish ";

  private static final String QUERY_FROM = "FROM client c "
      + "INNER JOIN event e ON e.client_id = c.client_id "
      + "INNER JOIN channel_session cs ON cs.event_id = e.event_id "
      + "LEFT JOIN subscriber_group sg ON cs.group_subscriber_id = sg.id ";

  private static final String QUERY_ORDER = "ORDER BY c.company_name ASC , cs.id DESC ";

  private DatabaseLibrary dbLib;

  public ListChannelSessionInfoDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public List getListHistoryBroadcast( int clientId , boolean debug ) {
    String sqlWhere = "WHERE ( cs.started = 1 ) AND ( cs.finished = 1 ) "
        + "AND ( cs.active = 1 ) AND ( c.client_id = " + clientId + " ) ";
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public List getListSchedulledBroadcast( int clientId , boolean debug ) {
    String sqlWhere = "WHERE ( cs.finished = 0 ) "
        + "AND ( ( ( cs.started = 0 ) AND ( cs.suspended = 0 ) ) "
        + "OR ( ( cs.started = 1 ) AND ( cs.suspended = 1 ) ) ) "
        + "AND ( cs.active = 1 ) AND ( c.client_id = " + clientId + " ) ";
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public List getListRunningBroadcast( int clientId , boolean debug ) {
    String sqlWhere = "WHERE ( cs.finished = 0 ) AND ( cs.started = 1 ) "
        + "AND ( cs.suspended = 0 ) "
        + "AND ( cs.active = 1 ) AND ( c.client_id = " + clientId + " ) ";
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public List getListHistoryBroadcastFromMaster( int masterClientId ,
      boolean debug ) {
    String sqlWhere = "WHERE ( cs.started = 1 ) AND ( cs.finished = 1 ) "
        + "AND ( cs.active = 1 ) ";
    if ( masterClientId > 0 ) {
      String sqlSelectIn = "SELECT ct.client_id ";
      String sqlFromIn = "FROM `client` ct ";
      String sqlWhereIn = "WHERE ( ct.active = 1 ) ";
      sqlWhereIn += "AND ( ct.master_client_id = " + masterClientId + " ) ";
      String sqlIn = sqlSelectIn + sqlFromIn + sqlWhereIn;
      sqlWhere += "AND ( c.client_id IN ( " + sqlIn + " ) ) ";
    }
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public List getListSchedulledBroadcastFromMaster( int masterClientId ,
      boolean debug ) {
    String sqlWhere = "WHERE ( cs.finished = 0 ) "
        + "AND ( ( ( cs.started = 0 ) AND ( cs.suspended = 0 ) ) "
        + "OR ( ( cs.started = 1 ) AND ( cs.suspended = 1 ) ) ) "
        + "AND ( cs.active = 1 ) ";
    if ( masterClientId > 0 ) {
      String sqlSelectIn = "SELECT ct.client_id ";
      String sqlFromIn = "FROM `client` ct ";
      String sqlWhereIn = "WHERE ( ct.active = 1 ) ";
      sqlWhereIn += "AND ( ct.master_client_id = " + masterClientId + " ) ";
      String sqlIn = sqlSelectIn + sqlFromIn + sqlWhereIn;
      sqlWhere += "AND ( c.client_id IN ( " + sqlIn + " ) ) ";
    }
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public List getListRunningBroadcastFromMaster( int masterClientId ,
      boolean debug ) {
    String sqlWhere = "WHERE ( cs.finished = 0 ) AND ( cs.started = 1 ) "
        + "AND ( cs.suspended = 0 ) " + "AND ( cs.active = 1 ) ";
    if ( masterClientId > 0 ) {
      String sqlSelectIn = "SELECT ct.client_id ";
      String sqlFromIn = "FROM `client` ct ";
      String sqlWhereIn = "WHERE ( ct.active = 1 ) ";
      sqlWhereIn += "AND ( ct.master_client_id = " + masterClientId + " ) ";
      String sqlIn = sqlSelectIn + sqlFromIn + sqlWhereIn;
      sqlWhere += "AND ( c.client_id IN ( " + sqlIn + " ) ) ";
    }
    String sql = QUERY_SELECT + QUERY_FROM + sqlWhere + QUERY_ORDER;
    return getListBroadcast( sql , debug );
  }

  public int getTotalRunningBroadcast( int clientId , boolean debug ) {
    int totalRecords = 0;

    String sqlWhere = "WHERE ( cs.finished = 0 ) AND ( cs.started = 1 ) "
        + "AND ( cs.suspended = 0 ) "
        + "AND ( cs.active = 1 ) AND ( c.client_id = " + clientId + " ) ";

    String sql = "SELECT COUNT(cs.id) AS total " + QUERY_FROM + sqlWhere
        + QUERY_ORDER;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return totalRecords;
    }

    Iterator it = qr.iterator();
    if ( it == null ) {
      return totalRecords;
    }
    if ( !it.hasNext() ) {
      return totalRecords;
    }

    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return totalRecords;
    }

    String stemp = qi.getFirstValue();
    if ( ( stemp == null ) || ( stemp.equals( "" ) ) ) {
      return totalRecords;
    }

    try {
      totalRecords = Integer.parseInt( stemp );
    } catch ( NumberFormatException e ) {
    }

    return totalRecords;
  }

  private List getListBroadcast( String sql , boolean debug ) {
    List list = null;
    if ( StringUtils.isBlank( sql ) ) {
      return list;
    }
    list = new ArrayList();
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr != null ) && ( qr.size() > 0 ) ) {
      DLog.debug( lctx , "Found " + qr.size() + " record(s)" );
      populateRecords( qr , list );
    }
    return list;
  }

  private int populateRecords( QueryResult qr , List list ) {
    int totalRecords = 0;
    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      ChannelSessionInfoBean bean = populateRecord( qi );
      if ( bean == null ) {
        continue;
      }
      list.add( bean );
      totalRecords = totalRecords + 1;
    }
    return totalRecords;
  }

  private ChannelSessionInfoBean populateRecord( QueryItem qi ) {
    ChannelSessionInfoBean bean = new ChannelSessionInfoBean();

    String stemp;
    int itemp;
    double ddtemp;

    stemp = (String) qi.get( 0 ); // cs.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setChannelSessionId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 1 ); // c.client_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setClientId( itemp );
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
        bean.setEventId( itemp );
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
        bean.setGroupSubscriberId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 7 ); // sg.group_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setGroupSubscriberName( stemp );
    }

    stemp = (String) qi.get( 8 ); // cs.allow_duplicated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setAllowDuplicated( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 9 ); // cs.use_date_send
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setUseDateSend( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 10 ); // cs.expect_response
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        bean.setExpectResponse( ddtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 11 ); // cs.total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setTotalNumbers( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 12 ); // cs.total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setTotalRemains( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 13 ); // cs.debit_units
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        bean.setDebitUnits( ddtemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 14 ); // cs.priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setPriority( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 15 ); // cs.suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSuspended( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 16 ); // cs.started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setStarted( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 17 ); // cs.finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFinished( stemp.equals( "1" ) );
    }
    stemp = (String) qi.get( 18 ); // cs.new_status
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setNewStatus( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 19 ); // cs.date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateStarted( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) qi.get( 20 ); // cs.date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateFinished( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 21 ); // cs.date_schedulled_start
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledStart( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) qi.get( 22 ); // cs.date_schedulled_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSchedulledFinish( DateTimeFormat.convertToDate( stemp ) );
    }

    return bean;
  }

}
