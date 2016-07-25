package com.beepcast.channel.view;

import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.dbmanager.util.DateTimeFormat;

public class ChannelSessionReportInfoQuery {

  public static String sqlSelectFrom() {

    String sqlSelect = "SELECT cs.id , ";
    sqlSelect += "cs.event_id , ev.event_name , ";
    sqlSelect += "ev.client_id , cl.company_name , ";
    sqlSelect += "cs.group_subscriber_id , sg.group_name , ";
    sqlSelect += "cs.total_numbers , cs.total_remains , ";
    sqlSelect += "csr.`key` , csr.total_sent , csr.total_retry , ";
    sqlSelect += "csr.total_dn_delivered , csr.total_dn_invalid_number , ";
    sqlSelect += "csr.total_dn_empty , csr.total_unsubs , ";
    sqlSelect += "csr.date_updated , cs.date_started , cs.date_finished , ";
    sqlSelect += "SUM(csx.total_visitors) + SUM(csx.total_members_visitors) AS 'total_visitors' , ";
    sqlSelect += "SUM(csx.total_hits) + SUM(csx.total_members_hits) AS 'total_hits' ";

    String sqlFrom = "FROM channel_session cs ";
    sqlFrom += "INNER JOIN event ev ON ev.event_id = cs.event_id ";
    sqlFrom += "INNER JOIN client cl ON cl.client_id = ev.client_id ";
    sqlFrom += "LEFT OUTER JOIN subscriber_group sg ON sg.id = cs.group_subscriber_id ";
    sqlFrom += "LEFT OUTER JOIN channel_session_report csr ON csr.channel_session_id = cs.id ";
    sqlFrom += "LEFT OUTER JOIN channel_session_xipme csx ON csx.channel_session_id = cs.id ";

    String sqlSelectFrom = sqlSelect + sqlFrom;

    return sqlSelectFrom;
  }

  public static ChannelSessionReportInfoBean populateRecord( QueryItem qi ) {
    ChannelSessionReportInfoBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    bean = new ChannelSessionReportInfoBean();

    String stemp;

    stemp = (String) qi.get( 0 ); // cs.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setChannelSessionId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 1 ); // ev.event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setEventId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 2 ); // ev.event_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setEventName( stemp );
    }
    stemp = (String) qi.get( 3 ); // cl.client_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setClientId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 4 ); // cl.company_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setClientName( stemp );
    }
    stemp = (String) qi.get( 5 ); // sg.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setSubscriberGroupId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 6 ); // sg.group_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSubscriberGroupName( stemp );
    }
    stemp = (String) qi.get( 7 ); // cs.total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalNumbers( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 8 ); // cs.total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalRemains( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 9 ); // csr.key
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setKey( stemp );
    }
    stemp = (String) qi.get( 10 ); // csr.total_sent
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalSent( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 11 ); // csr.total_retry
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalRetry( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 12 ); // csr.total_dn_delivered
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalDnDelivered( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 13 ); // csr.total_dn_invalid_number
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalDnInvalidNumber( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 14 ); // csr.total_dn_empty
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalDnEmpty( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 15 ); // csr.total_unsubs
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalUnsubs( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) qi.get( 16 ); // csr.date_updated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateReportCreated( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 17 ); // cs.date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateBroadcastStarted( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 18 ); // cs.date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateBroadcastFinished( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 19 ); // total_visitors
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalVisitors( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 20 ); // total_hits
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalHits( Long.parseLong( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    return bean;
  }

}
