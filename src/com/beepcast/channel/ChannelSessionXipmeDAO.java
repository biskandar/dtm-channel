package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionXipmeDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelSessionXipmeDAO" );

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

  public ChannelSessionXipmeDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insertBean( ChannelSessionXipmeBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // compose sql

    String sqlInsert = "INSERT INTO channel_session_xipme ";
    sqlInsert += "(channel_session_id,xipme_code,total_members,total_members_members";
    sqlInsert += ",total_customers,total_visitors,total_members_visitors,total_bot_visitors";
    sqlInsert += ",total_members_bot_visitors,total_hits,total_members_hits,total_bot_hits";
    sqlInsert += ",total_members_bot_hits,date_inserted,date_updated) ";
    String sqlValues = "VALUES (" + bean.getChannelSessionId() + ",'"
        + StringEscapeUtils.escapeSql( bean.getXipmeCode() ) + "',"
        + bean.getTotalMembers() + "," + bean.getTotalMembersMembers() + ","
        + bean.getTotalCustomers() + "," + bean.getTotalVisitors() + ","
        + bean.getTotalMembersVisitors() + "," + bean.getTotalBotVisitors()
        + "," + bean.getTotalMembersBotVisitors() + "," + bean.getTotalHits()
        + "," + bean.getTotalMembersHits() + "," + bean.getTotalBotHits() + ","
        + bean.getTotalMembersBotHits() + ",NOW(),NOW()) ";
    String sql = sqlInsert + sqlValues;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( ( irslt == null ) || ( irslt.intValue() < 1 ) ) {
      return result;
    }

    result = true;
    return result;
  }

  public boolean updateTotals( ChannelSessionXipmeBean bean ) {
    boolean result = false;

    if ( bean == null ) {
      return result;
    }

    // compose sql

    String sqlUpdate = "UPDATE channel_session_xipme SET total_members = "
        + bean.getTotalMembers();
    sqlUpdate += " , total_members_members = " + bean.getTotalMembersMembers();
    sqlUpdate += " , total_customers = " + bean.getTotalCustomers();
    sqlUpdate += " , total_visitors = " + bean.getTotalVisitors();
    sqlUpdate += " , total_members_visitors = "
        + bean.getTotalMembersVisitors();
    sqlUpdate += " , total_bot_visitors = " + bean.getTotalBotVisitors();
    sqlUpdate += " , total_members_bot_visitors = "
        + bean.getTotalMembersBotVisitors();
    sqlUpdate += " , total_hits = " + bean.getTotalHits();
    sqlUpdate += " , total_members_hits = " + bean.getTotalMembersHits();
    sqlUpdate += " , total_bot_hits = " + bean.getTotalBotHits();
    sqlUpdate += " , total_members_bot_hits = " + bean.getTotalMembersBotHits();
    sqlUpdate += " , date_updated = NOW() ";
    String sqlWhere = "WHERE ( id = " + bean.getId() + " ) ";
    String sql = sqlUpdate + sqlWhere;

    // execute sql

    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( ( irslt == null ) || ( irslt.intValue() < 1 ) ) {
      return result;
    }

    result = true;
    return result;
  }

  public ChannelSessionXipmeBean selectBean( int channelSessionId ,
      String xipmeCode ) {
    ChannelSessionXipmeBean bean = null;
    if ( channelSessionId < 1 ) {
      return bean;
    }
    if ( xipmeCode == null ) {
      return bean;
    }

    // compose sql
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) AND ( xipme_code = '" + StringEscapeUtils.escapeSql( xipmeCode )
        + "' ) ";
    String sqlOrder = "ORDER BY id ASC ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }

    // populate record
    QueryItem qi = null;
    Iterator it = qr.iterator();
    if ( it.hasNext() ) {
      qi = (QueryItem) it.next();
    }
    if ( qi == null ) {
      return bean;
    }
    bean = populate( qi );

    return bean;
  }

  public List selectBeans( int channelSessionId ) {
    List listBeans = new ArrayList();
    if ( channelSessionId < 1 ) {
      return listBeans;
    }

    // compose sql
    String sqlSelectFrom = sqlSelectFrom();
    String sqlWhere = "WHERE ( channel_session_id = " + channelSessionId
        + " ) ";
    String sqlOrder = "ORDER BY id ASC ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder;

    // execute sql
    QueryResult qr = dbLib.simpleQuery( "profiledb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listBeans;
    }

    // populate records
    Iterator it = qr.iterator();
    while ( it.hasNext() ) {
      QueryItem qi = (QueryItem) it.next();
      if ( qi == null ) {
        continue;
      }
      ChannelSessionXipmeBean bean = populate( qi );
      if ( bean == null ) {
        continue;
      }
      listBeans.add( bean );
    }

    return listBeans;
  }

  private String sqlSelectFrom() {
    String sqlSelect = "SELECT id , channel_session_id , xipme_code ";
    sqlSelect += ", total_members , total_members_members , total_customers , total_visitors ";
    sqlSelect += ", total_members_visitors , total_bot_visitors , total_members_bot_visitors ";
    sqlSelect += ", total_hits , total_members_hits , total_bot_hits , total_members_bot_hits ";
    sqlSelect += ", date_inserted , date_updated ";
    String sqlFrom = "FROM channel_session_xipme ";
    String sqlSelectFrom = sqlSelect + sqlFrom;
    return sqlSelectFrom;
  }

  private ChannelSessionXipmeBean populate( QueryItem qi ) {
    ChannelSessionXipmeBean bean = null;

    if ( qi == null ) {
      return bean;
    }

    bean = new ChannelSessionXipmeBean();

    String stemp;

    stemp = (String) qi.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 1 ); // channel_session_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setChannelSessionId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 2 ); // xipme_code
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setXipmeCode( stemp );
    }

    stemp = (String) qi.get( 3 ); // total_members
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembers( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 4 ); // total_members_members
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembersMembers( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 5 ); // total_customers
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalCustomers( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 6 ); // total_visitors
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalVisitors( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 7 ); // total_members_visitors
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembersVisitors( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 8 ); // total_bot_visitors
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalBotVisitors( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 9 ); // total_members_bot_visitors
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembersBotVisitors( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 10 ); // total_hits
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalHits( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 11 ); // total_members_hits
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembersHits( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 12 ); // total_bot_hits
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalBotHits( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 13 ); // total_members_bot_hits
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setTotalMembersBotHits( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) qi.get( 14 ); // date_inserted
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateInserted( DateTimeFormat.convertToDate( stemp ) );
    }

    stemp = (String) qi.get( 15 ); // date_updated
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateUpdated( DateTimeFormat.convertToDate( stemp ) );
    }

    return bean;
  }

}
