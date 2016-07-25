package com.beepcast.channel;

import java.util.Date;

import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.model.util.DateTimeFormat;

public class ChannelSessionScheduleQuery {

  public static String sqlSelectFrom() {
    String sqlSelect = "SELECT id , channel_session_id , date_started ";
    sqlSelect += ", date_stopped , marked , active ";
    String sqlFrom = "FROM channel_session_schedule ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  public static ChannelSessionScheduleBean populateRecord( QueryItem qi ) {
    ChannelSessionScheduleBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    String stemp;
    int itemp;
    Date dtemp;

    bean = new ChannelSessionScheduleBean();

    stemp = (String) qi.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // channel_session_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setChannelSessionId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 2 ); // date_started
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateStarted( dtemp );
    }

    stemp = (String) qi.get( 3 ); // date_stopped
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      dtemp = DateTimeFormat.convertToDate( stemp );
      bean.setDateStopped( dtemp );
    }

    stemp = (String) qi.get( 4 ); // marked
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setMarked( stemp );
    }

    stemp = (String) qi.get( 5 ); // active
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setActive( stemp.equals( "1" ) );
    }

    return bean;
  }

}
