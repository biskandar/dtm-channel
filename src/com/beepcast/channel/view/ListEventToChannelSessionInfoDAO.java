package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.event.EventBean;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListEventToChannelSessionInfoDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ListEventToChannelSessionInfoDAO" );

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

  public ListEventToChannelSessionInfoDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public List listEventOfHistoryBroadcast( int clientId ) {
    List listEvents = new ArrayList();

    // compose sql
    String sqlSelect = "SELECT ev.event_id , ev.event_name ";
    String sqlFrom = "FROM `client` cl ";
    sqlFrom += "INNER JOIN event ev ON ev.client_id = cl.client_id ";
    sqlFrom += "INNER JOIN channel_session cs ON cs.event_id = ev.event_id ";
    String sqlWhere = "WHERE ( cs.active = 1 ) ";
    sqlWhere += "AND ( cs.started = 1 ) ";
    sqlWhere += "AND ( ( cs.suspended = 1 ) OR ( cs.finished = 1 ) ) ";
    sqlWhere += "AND ( cl.client_id = " + clientId + " ) ";
    String sqlOrder = "ORDER BY cs.id DESC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listEvents;
    }

    // fetch records
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      EventBean eventBean = populateRecord( qi );
      if ( eventBean == null ) {
        continue;
      }
      listEvents.add( eventBean );
    }

    return listEvents;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private EventBean populateRecord( QueryItem qi ) {
    EventBean eventBean = null;

    if ( qi == null ) {
      return eventBean;
    }

    eventBean = new EventBean();

    String stemp;
    int itemp;

    stemp = (String) qi.get( 0 ); // ev.event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        eventBean.setEventID( itemp );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // ev.event_name
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      eventBean.setEventName( stemp );
    }

    return eventBean;
  }

}
