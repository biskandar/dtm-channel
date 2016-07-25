package com.beepcast.channel.view;

import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.model.util.DateTimeFormat;

public class ListBroadcastSummaryQuery {

  public static String sqlWhere( String strClientIds , String strEventIds ,
      Date dateStarted , Date dateFinished ) {
    String sqlWhere = "WHERE ( cs.active = 1 ) ";
    sqlWhere += "AND ( cs.started = 1 ) ";
    sqlWhere += "AND ( ( cs.suspended = 1 ) OR ( cs.finished = 1 ) ) ";
    if ( !StringUtils.isBlank( strClientIds ) ) {
      sqlWhere += "AND ( cl.client_id IN ( "
          + StringEscapeUtils.escapeSql( strClientIds ) + " ) ) ";
    }
    if ( !StringUtils.isBlank( strEventIds ) ) {
      sqlWhere += "AND ( cs.event_id IN ( "
          + StringEscapeUtils.escapeSql( strEventIds ) + " ) ) ";
    }
    if ( dateStarted != null ) {
      sqlWhere += "AND ( cs.date_started >= '"
          + DateTimeFormat.convertToString( dateStarted ) + "' ) ";
    }
    if ( dateFinished != null ) {
      sqlWhere += "AND ( cs.date_started <= '"
          + DateTimeFormat.convertToString( dateFinished ) + "' ) ";
    }
    return sqlWhere;
  }

}
