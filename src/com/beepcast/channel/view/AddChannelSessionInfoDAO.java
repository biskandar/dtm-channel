package com.beepcast.channel.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class AddChannelSessionInfoDAO {

  static final DLogContext lctx = new SimpleContext( "AddChannelSessionInfoDAO" );

  private DatabaseLibrary dbLib;

  public AddChannelSessionInfoDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  public List getAvailableChannel( int clientId ) {
    List list = null;

    // compose sql
    String sqlSelect = "SELECT event_id , event_name ";
    String sqlFrom = "FROM event ";
    String sqlWhere = "WHERE ( active = 1 ) AND ( codes != '' ) ";
    sqlWhere += "AND ( channel = 1 ) AND ( display = 1 ) AND ( suspend = 0 ) ";
    sqlWhere += "AND ( client_id = " + clientId + " ) ";
    sqlWhere += "AND ( event_id NOT IN ( SELECT event_id FROM channel_session WHERE active = 1 ) ) ";
    String sqlOrder = "ORDER BY event_id DESC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;

    DLog.debug( lctx , "Perform " + sql );

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( qr != null && ( qr.size() > 0 ) ) {
      DLog.debug( lctx , "Found " + qr.size() + " record(s)" );
      list = populateChannelRecords( qr );
    }

    return list;
  }

  public List getAvailableGroupSubscriber( int clientId ) {
    List list = null;

    // compose sql
    String sqlSelect = "SELECT id , group_name ";
    String sqlFrom = "FROM subscriber_group ";
    String sqlWhere = "WHERE ( active = 1 ) ";
    sqlWhere += "AND ( client_id = " + clientId + " ) ";
    String sqlOrder = "ORDER BY id DESC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;

    DLog.debug( lctx , "Perform " + sql );

    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( qr != null && ( qr.size() > 0 ) ) {
      DLog.debug( lctx , "Found " + qr.size() + " record(s)" );
      list = populateGroupSubscriberRecords( qr );
    }

    return list;
  }

  private List populateChannelRecords( QueryResult qr ) {
    List list = new ArrayList();

    EventInfoBean bean = null;
    boolean valid;
    String stemp;
    int itemp;

    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      valid = true;
      bean = new EventInfoBean();
      stemp = (String) qi.get( 0 ); // eventId
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          if ( itemp < 1 ) {
            DLog.warning( lctx , "Failed to get add channel session info "
                + ", found zero eventId" );
            valid = false;
          } else {
            bean.setEventId( itemp );
          }
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) qi.get( 1 ); // eventName
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setEventName( stemp );
      } else {
        DLog.warning( lctx , "Failed to get add channel session info "
            + ", found empty eventName" );
        valid = false;
      }

      if ( valid ) {
        list.add( bean );
      }
    }

    return list;
  }

  private List populateGroupSubscriberRecords( QueryResult qr ) {
    List list = new ArrayList();

    GroupSubscriberInfoBean bean = null;
    boolean valid;
    String stemp;
    int itemp;

    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      valid = true;
      bean = new GroupSubscriberInfoBean();
      stemp = (String) qi.get( 0 ); // groupSubscriberId
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        try {
          itemp = Integer.parseInt( stemp );
          if ( itemp < 1 ) {
            DLog.warning( lctx , "Failed to get add channel session info "
                + ", found zero groupSubscriberId" );
            valid = false;
          } else {
            bean.setGroupSubscriberId( itemp );
          }
        } catch ( NumberFormatException e ) {
        }
      }
      stemp = (String) qi.get( 1 ); // groupSubscriberName
      if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
        bean.setGroupSubscriberName( stemp );
      } else {
        DLog.warning( lctx , "Failed to get add channel session info "
            + ", found empty groupSubscriberName" );
        valid = false;
      }

      if ( valid ) {
        list.add( bean );
      }
    }

    return list;
  }

}
