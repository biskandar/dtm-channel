package com.beepcast.channel.view;

import java.util.Date;
import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionInfoDAO {

  static final DLogContext lctx = new SimpleContext( "ChannelSessionInfoDAO" );

  private DatabaseLibrary dbLib;

  public ChannelSessionInfoDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public ChannelSessionInfoBean getChannelSessionInfo( int channelSessionId ) {
    ChannelSessionInfoBean bean = null;

    String sqlSelect = "SELECT cs.id , ev.event_id , ev.event_name ";
    sqlSelect += ", sg.id , sg.group_name , cs.allow_duplicated , cs.use_date_send ";
    sqlSelect += ", cs.expect_response , cs.total_numbers , cs.total_remains ";
    sqlSelect += ", cs.debit_units , cs.priority ";
    sqlSelect += ", cs.suspended , cs.started , cs.finished ";
    sqlSelect += ", cs.date_started , cs.date_finished ";
    sqlSelect += ", cs.date_schedulled_start , cs.date_schedulled_finish ";

    String sqlFrom = "FROM channel_session cs ";
    sqlFrom += "INNER JOIN event ev ON ev.event_id = cs.event_id ";
    sqlFrom += "INNER JOIN subscriber_group sg ON sg.id = cs.group_subscriber_id ";

    String sqlWhere = "WHERE ( cs.id = " + channelSessionId + " ) ";

    String sqlLimit = "LIMIT 1 ";

    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }

    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return bean;
    }

    bean = populateRecord( (QueryItem) it.next() );
    return bean;
  }

  private ChannelSessionInfoBean populateRecord( QueryItem qi ) {
    ChannelSessionInfoBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    bean = new ChannelSessionInfoBean();

    String stemp;
    int itemp;
    double ddtemp;
    Date dtemp;

    stemp = (String) qi.get( 0 ); // cs.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setChannelSessionId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // ev.event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setEventId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 2 ); // ev.event_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setEventName( stemp );
    }

    stemp = (String) qi.get( 3 ); // sg.id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setGroupSubscriberId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 4 ); // sg.group_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setGroupSubscriberName( stemp );
    }

    stemp = (String) qi.get( 5 ); // cs.allow_duplicated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setAllowDuplicated( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 6 ); // cs.use_date_send
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setUseDateSend( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 7 ); // cs.expect_response
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        bean.setExpectResponse( ddtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // cs.total_numbers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setTotalNumbers( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // cs.total_remains
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setTotalRemains( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 10 ); // cs.debit_units
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        ddtemp = Double.parseDouble( stemp );
        bean.setDebitUnits( ddtemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 11 ); // cs.priority
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setPriority( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 12 ); // cs.suspended
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setSuspended( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 13 ); // cs.started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setStarted( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 14 ); // cs.finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setFinished( stemp.equals( "1" ) );
    }

    stemp = (String) qi.get( 15 ); // cs.date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateStarted( dtemp );
    }

    stemp = (String) qi.get( 16 ); // cs.date_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateFinished( dtemp );
    }

    stemp = (String) qi.get( 17 ); // cs.date_schedulled_start
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateSchedulledStart( dtemp );
    }

    stemp = (String) qi.get( 18 ); // cs.date_schedulled_finished
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateSchedulledFinish( dtemp );
    }

    return bean;
  }

}
