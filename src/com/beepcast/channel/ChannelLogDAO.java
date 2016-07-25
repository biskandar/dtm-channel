package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.dbmanager.util.DateTimeFormat;
import com.beepcast.encrypt.EncryptApp;
import com.beepcast.subscriber.common.DateTimeUtils;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelLogDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelLogDAO" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private DatabaseLibrary dbLib;
  private final String keyPhoneNumber;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogDAO() {
    dbLib = DatabaseLibrary.getInstance();

    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean insertChannelLogBean( ChannelLogBean clBean ) {
    boolean result = false;

    if ( clBean == null ) {
      return result;
    }

    int marked = clBean.getMarked();
    String phone = clBean.getPhone();
    int eventId = clBean.getEventId();
    int subscribed = clBean.getSubscribed();
    int clientSubscriberId = clBean.getClientSubscriberId();
    int clientSubscriberCustomId = clBean.getClientSubscriberCustomId();

    Date dateTm = clBean.getDateTm();
    Date dateSubscribed = clBean.getDateSubscribed();
    Date dateMarked = clBean.getDateMarked();
    Date dateUnmarked = clBean.getDateUnmarked();

    String strDateTm = DateTimeFormat.convertToString( dateTm );
    String strDateSubscribed = DateTimeFormat.convertToString( dateSubscribed );
    String strDateMarked = DateTimeFormat.convertToString( dateMarked );
    String strDateUnmarked = DateTimeFormat.convertToString( dateUnmarked );

    phone = ( phone == null ) ? "" : phone.trim();
    strDateTm = ( strDateTm == null ) ? "NULL" : "'" + strDateTm + "'";
    strDateSubscribed = ( strDateSubscribed == null ) ? "NULL" : "'"
        + strDateSubscribed + "'";
    strDateMarked = ( strDateMarked == null ) ? "NULL" : "'" + strDateMarked
        + "'";
    strDateUnmarked = ( strDateUnmarked == null ) ? "NULL" : "'"
        + strDateUnmarked + "'";

    String sqlInsert = "INSERT INTO channel_log ";
    sqlInsert += "( marked , phone , encrypt_phone , event_id , subscribed ";
    sqlInsert += ", client_subscriber_id , client_subscriber_custom_id ";
    sqlInsert += ", date_tm , date_subscribed , date_marked , date_unmarked ) ";

    String sqlValues = "VALUES ( " + marked + " , '' , "
        + sqlEncryptPhoneNumber( phone ) + " , " + eventId + " , " + subscribed
        + " , " + clientSubscriberId + " , " + clientSubscriberCustomId + " , "
        + strDateTm + " , " + strDateSubscribed + " , " + strDateMarked + " , "
        + strDateUnmarked + " ) ";

    String sql = sqlInsert + sqlValues;

    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public long updateSubscriberCountryProfile( int eventId ,
      String countryPhonePrefix , String countryCode , double countryCreditCost ) {
    long totalRecords = 0;

    if ( eventId < 1 ) {
      return totalRecords;
    }

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET country_code = '" + countryCode + "' ";
    sqlSet += ", country_credit_cost = " + countryCreditCost + " ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    sqlWhere += "AND ( " + sqlDecryptPhoneNumber( null , false ) + " LIKE '"
        + countryPhonePrefix + "%' ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.longValue();
    }

    return totalRecords;
  }

  public long updateSubscriberMarked( int eventId ) {
    long result = 0;

    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get total subscriber "
          + ", found eventId = 0 " );
      return result;
    }

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET marked = 1 , date_marked = NOW() ";
    String sqlWhere = "WHERE ( subscribed = 1 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      result = irslt.longValue();
    }

    return result;
  }

  public boolean updateSubscriberUnmarked( int id , String messageId ) {
    boolean result = false;

    if ( id < 1 ) {
      DLog.warning( lctx , "Failed to update unmarked subscriber "
          + "in the channel log table , found blank channel log id" );
      return result;
    }

    messageId = ( messageId == null ) ? "" : messageId;

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET marked = 0 ";
    sqlSet += ", date_unmarked = NOW() ";
    sqlSet += ", message_id = '" + StringEscapeUtils.escapeSql( messageId )
        + "' ";
    String sqlWhere = "WHERE ( id = " + id + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Bean Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogBean getChannelLogBean( boolean debug , int eventId ,
      String phoneNumber ) {
    ChannelLogBean clBean = null;

    // compose sql

    String sqlSelectFrom = sqlSelectFromChannelLog();
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    sqlWhere += "AND ( " + sqlDecryptPhoneNumber( null , false ) + " = '"
        + StringEscapeUtils.escapeSql( phoneNumber ) + "' ) ";
    String sqlOrder = "ORDER BY id DESC ";
    String sqlLimit = "LIMIT 1 ";
    String sql = sqlSelectFrom + sqlWhere + sqlOrder + sqlLimit;

    // execute sql

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return clBean;
    }

    // populate record

    Iterator iter = qr.iterator();
    if ( !iter.hasNext() ) {
      return clBean;
    }
    QueryItem qi = (QueryItem) iter.next();
    if ( qi == null ) {
      return clBean;
    }
    clBean = populateChannelLogBean( qi );

    return clBean;
  }

  public List getSubscriberPhoneNumber( int eventId , int limit ) {
    List result = null;

    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get total subscriber "
          + ", found eventId = 0 " );
      return result;
    }

    String sqlSelect = "SELECT " + sqlDecryptPhoneNumber( null , true ) + " ";
    String sqlFrom = "FROM channel_log ";
    String sqlWhere = "WHERE ( marked = 1 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";
    String sqlLimit = "LIMIT " + limit + " ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlLimit;

    // DLog.debug( lctx , "Perform " + sql );

    QueryResult queryResult = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( queryResult != null ) && ( queryResult.size() > 0 ) ) {
      result = populateSubscriberPhoneNumber( queryResult );
    }

    if ( ( result != null ) && ( result.size() > 0 ) ) {
      DLog.debug( lctx , "Populated " + queryResult.size()
          + " subscriber number(s) in the channel log , ready to process" );
    }

    return result;
  }

  public List getChannelLogBeans( boolean debug , int eventId , Boolean marked ,
      Boolean useDateSend , int offset , int limit ) {
    List listChannelLogBeans = new ArrayList();

    // compose sql

    String sqlSelectFrom = sqlSelectFromChannelLog();
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    if ( marked != null ) {
      if ( marked.booleanValue() ) {
        sqlWhere += "AND ( marked = 1 ) ";
      } else {
        sqlWhere += "AND ( marked = 0 ) ";
      }
    }
    if ( useDateSend != null ) {
      if ( useDateSend.booleanValue() ) {
        sqlWhere += "AND ( date_send < NOW() ) ";
      }
    }
    String sqlLimit = "LIMIT " + offset + " , " + limit + " ";
    String sql = sqlSelectFrom + sqlWhere + sqlLimit;

    // execute sql

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return listChannelLogBeans;
    }

    // populate records

    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      ChannelLogBean channelLogBean = populateChannelLogBean( qi );
      if ( channelLogBean == null ) {
        continue;
      }
      listChannelLogBeans.add( channelLogBean );
    }

    return listChannelLogBeans;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Query Report Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getTotalSubscriber( boolean debug , int eventId ) {
    long result = 0;

    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get total subscriber "
          + ", found zero eventId" );
      return result;
    }

    String sqlSelect = "SELECT COUNT(id) as total ";
    String sqlFrom = "FROM channel_log ";
    String sqlWhere = "WHERE ( subscribed = 1 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    result = ChannelLogQueryCommon.getFirstValueLong( sql );
    return result;
  }

  public long getTotalRemains( boolean debug , int eventId ) {
    long result = 0;

    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to get total remains "
          + ", found zero eventId" );
      return result;
    }

    String sqlSelect = "SELECT COUNT(id) AS total ";
    String sqlFrom = "FROM channel_log ";
    String sqlWhere = "WHERE ( marked = 1 ) ";
    sqlWhere += "AND ( event_id =  " + eventId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    result = ChannelLogQueryCommon.getFirstValueLong( sql );
    return result;
  }

  public double getTotalCountryCreditCostForRemainsSubscriber( boolean debug ,
      int eventId ) {
    double result = 0;

    if ( eventId < 1 ) {
      return result;
    }

    String sqlSelect = "SELECT SUM(country_credit_cost) AS total ";
    String sqlFrom = "FROM channel_log ";
    String sqlWhere = "WHERE ( marked = 1 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    result = ChannelLogQueryCommon.getFirstValueDouble( sql );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Store / Clean Subscribers Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long storeSubscribers( boolean debug , int channelSessionId ,
      int eventId , int subscriberGroupId , boolean allowDuplicated ,
      boolean useDateSend , double defaultCountryCreditCost ) {
    long totalInserted = 0;

    if ( channelSessionId < 1 ) {
      return totalInserted;
    }
    if ( eventId < 1 ) {
      return totalInserted;
    }
    if ( subscriberGroupId < 1 ) {
      return totalInserted;
    }

    String sqlSelectFromClientSubscriber = sqlSelectFromClientSubscriber(
        debug , channelSessionId , eventId , subscriberGroupId ,
        allowDuplicated , useDateSend , defaultCountryCreditCost );
    if ( StringUtils.isBlank( sqlSelectFromClientSubscriber ) ) {
      return totalInserted;
    }

    StringBuffer sbInsert = new StringBuffer();
    sbInsert.append( "INSERT INTO channel_log " );
    sbInsert.append( "( marked , phone , encrypt_phone , channel_session_id " );
    sbInsert.append( ", event_id , subscribed , client_subscriber_id " );
    sbInsert.append( ", client_subscriber_custom_id , cust_ref_id " );
    sbInsert.append( ", cust_ref_code , description , country_code  " );
    sbInsert.append( ", country_credit_cost , date_tm , date_send " );
    sbInsert.append( ", date_subscribed , date_marked ) " );

    String sql = sbInsert.toString() + sqlSelectFromClientSubscriber;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      totalInserted = irslt.longValue();
    }

    return totalInserted;
  }

  public boolean cleanSubscribers( boolean debug , int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      DLog.warning( lctx , "Failed to clean subscriber "
          + ", found zero eventId" );
      return result;
    }

    String sqlDelete = "DELETE FROM channel_log ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    String sql = sqlDelete + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Status Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean updateSubscribedStatus( int eventId , String phoneNumber ,
      Date dateSubscribed ) {
    boolean result = false;

    String strDateSubscribed = DateTimeFormat.convertToString( dateSubscribed );
    strDateSubscribed = ( strDateSubscribed == null ) ? "NULL" : "'"
        + strDateSubscribed + "'";

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET subscribed = 1 ";
    sqlSet += ", date_subscribed = " + strDateSubscribed + " ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    sqlWhere += "AND ( " + sqlDecryptPhoneNumber( null , false ) + " = '"
        + phoneNumber + "' ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );

    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  public boolean updateUnsubscribedStatus( int eventId , String phoneNumber ,
      Date dateUnsubscribed ) {
    boolean result = false;

    String strDateUnsubscribed = DateTimeFormat
        .convertToString( dateUnsubscribed );
    strDateUnsubscribed = ( strDateUnsubscribed == null ) ? "NULL" : "'"
        + strDateUnsubscribed + "'";

    String sqlUpdate = "UPDATE channel_log ";
    String sqlSet = "SET subscribed = 0 ";
    sqlSet += ", date_subscribed = " + strDateUnsubscribed + " ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    sqlWhere += "AND ( " + sqlDecryptPhoneNumber( null , false ) + " = '"
        + phoneNumber + "' ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    // DLog.debug( lctx , "Perform " + sql );

    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlSelectFromClientSubscriber( boolean debug , int csId ,
      int evId , int sgId , boolean allowDuplicated , boolean useDateSend ,
      double defaultCountryCreditCost ) {
    String sql = null;

    if ( csId < 1 ) {
      return sql;
    }
    if ( evId < 1 ) {
      return sql;
    }
    if ( sgId < 1 ) {
      return sql;
    }

    String dateSendMin = DateTimeUtils.sqlDtmDaysBefore( 0 );

    StringBuffer sbSelect = new StringBuffer();
    sbSelect.append( "SELECT 1 AS \"marked\" " );
    sbSelect.append( ", '' AS \"phone\" " );
    sbSelect.append( ", cs.encrypt_phone " );
    sbSelect.append( ", " + csId + " AS \"channel_session_id\" " );
    sbSelect.append( ", " + evId + " AS \"event_id\" " );
    sbSelect.append( ", 1 AS \"subscribed\" " );
    sbSelect.append( ", cs.id AS \"client_subscriber_id\" " );
    sbSelect.append( ", csc.id AS \"client_subscriber_custom_id\" " );
    sbSelect.append( ", cs.cust_ref_id " );
    sbSelect.append( ", cs.cust_ref_code " );
    sbSelect.append( ", cs.description " );
    sbSelect.append( ", NULL AS \"country_code\" " );
    sbSelect.append( ", " + defaultCountryCreditCost
        + " AS \"country_credit_cost\" " );
    sbSelect.append( ", NOW() AS \"date_tm\" " );
    if ( useDateSend ) {
      sbSelect.append( ", cs.date_send " );
    } else {
      sbSelect.append( ", NULL AS \"date_send\" " );
    }
    sbSelect.append( ", NOW() AS \"date_subscribed\" " );
    sbSelect.append( ", NOW() AS \"date_marked\" " );

    String sqlFromWhereOrder = null;
    if ( allowDuplicated ) {

      StringBuffer sbFrom = new StringBuffer();
      sbFrom.append( "FROM client_subscriber cs " );
      sbFrom.append( "LEFT OUTER JOIN client_subscriber_custom csc " );
      sbFrom.append( "  ON csc.client_subscriber_id = cs.id " );

      StringBuffer sbWhere = new StringBuffer();
      sbWhere.append( "WHERE ( cs.subscriber_group_id = " + sgId + " ) " );
      sbWhere.append( "  AND ( cs.subscribed = 1 ) " );
      sbWhere.append( "  AND ( cs.global_subscribed = 1 ) " );
      sbWhere.append( "  AND ( cs.global_invalid = 0 ) " );
      sbWhere.append( "  AND ( cs.global_dnc = 0 ) " );
      if ( useDateSend ) {
        sbWhere.append( "  AND ( cs.date_send IS NOT NULL ) " );
        sbWhere.append( "  AND ( cs.date_send > '" + dateSendMin + "' ) " );
      }
      sbWhere.append( "  AND ( cs.active = 1 ) " );

      StringBuffer sbOrder = new StringBuffer();
      sbOrder.append( "ORDER BY cs.id ASC " );

      sqlFromWhereOrder = sbFrom.toString() + sbWhere.toString()
          + sbOrder.toString();

    } else {

      StringBuffer sbInner = new StringBuffer();
      sbInner.append( "SELECT MAX( cs1.id ) AS id " );
      sbInner.append( "FROM client_subscriber cs1 " );
      sbInner.append( "WHERE ( cs1.subscriber_group_id = " + sgId + " ) " );
      sbInner.append( "  AND ( cs1.subscribed = 1 ) " );
      sbInner.append( "  AND ( cs1.global_subscribed = 1 ) " );
      sbInner.append( "  AND ( cs1.global_invalid = 0 ) " );
      sbInner.append( "  AND ( cs1.global_dnc = 0 ) " );
      if ( useDateSend ) {
        sbInner.append( "  AND ( cs1.date_send IS NOT NULL ) " );
        sbInner.append( "  AND ( cs1.date_send > '" + dateSendMin + "' ) " );
      }
      sbInner.append( "  AND ( cs1.active = 1 ) " );
      sbInner.append( "GROUP BY cs1.encrypt_phone " );

      StringBuffer sbFrom = new StringBuffer();
      sbFrom.append( "FROM client_subscriber cs " );
      sbFrom.append( "INNER JOIN ( " + sbInner.toString() + " ) cs2 " );
      sbFrom.append( "  ON cs2.id = cs.id " );
      sbFrom.append( "LEFT OUTER JOIN client_subscriber_custom csc " );
      sbFrom.append( "  ON csc.client_subscriber_id = cs.id " );

      StringBuffer sbOrder = new StringBuffer();
      sbOrder.append( "ORDER BY cs.id ASC " );

      sqlFromWhereOrder = sbFrom.toString() + sbOrder.toString();

    }

    sql = sbSelect.toString() + sqlFromWhereOrder;

    return sql;
  }

  private String sqlSelectFromChannelLog() {

    String sqlSelect = "SELECT id , marked ";
    sqlSelect += ", " + sqlDecryptPhoneNumber( null , true ) + " ";
    sqlSelect += ", event_id , subscribed , client_subscriber_id ";
    sqlSelect += ", client_subscriber_custom_id , cust_ref_id ";
    sqlSelect += ", cust_ref_code , description , country_code ";
    sqlSelect += ", country_credit_cost , date_tm , date_send ";
    sqlSelect += ", date_subscribed , date_marked , date_unmarked ";

    String sqlFrom = "FROM channel_log ";

    String sqlSelectFrom = sqlSelect + sqlFrom;

    return sqlSelectFrom;
  }

  private List populateSubscriberPhoneNumber( QueryResult queryResult ) {
    List list = new ArrayList();
    String phoneNumber = null;
    Iterator iter = queryResult.getIterator();
    while ( iter.hasNext() ) {
      QueryItem queryItem = (QueryItem) iter.next();
      if ( queryItem == null ) {
        continue;
      }
      phoneNumber = (String) queryItem.getFirstValue();
      if ( ( phoneNumber == null ) || ( phoneNumber.equals( "" ) ) ) {
        continue;
      }
      list.add( phoneNumber );
    }
    return list;
  }

  private ChannelLogBean populateChannelLogBean( QueryItem queryItem ) {
    ChannelLogBean bean = null;
    if ( queryItem == null ) {
      return bean;
    }

    String stemp;

    bean = new ChannelLogBean();

    stemp = (String) queryItem.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 1 ); // marked
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setMarked( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 2 ); // phone
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setPhone( stemp );
    }
    stemp = (String) queryItem.get( 3 ); // event_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setEventId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 4 ); // subscribed
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setSubscribed( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 5 ); // client_subscriber_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setClientSubscriberId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 6 ); // client_subscriber_custom_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setClientSubscriberCustomId( Integer.parseInt( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) queryItem.get( 7 ); // cust_ref_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setCustRefId( stemp );
    }
    stemp = (String) queryItem.get( 8 ); // cust_ref_code
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setCustRefCode( stemp );
    }
    stemp = (String) queryItem.get( 9 ); // description
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDescription( stemp );
    }

    stemp = (String) queryItem.get( 10 ); // country_code
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setCountryCode( stemp );
    }

    stemp = (String) queryItem.get( 11 ); // country_credit_cost
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        bean.setCountryCreditCost( Double.parseDouble( stemp ) );
      } catch ( NumberFormatException e ) {
      }
    }

    stemp = (String) queryItem.get( 12 ); // date_tm
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateTm( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 13 ); // date_send
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSend( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 14 ); // date_subscribed
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateSubscribed( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 15 ); // date_marked
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateMarked( DateTimeFormat.convertToDate( stemp ) );
    }
    stemp = (String) queryItem.get( 16 ); // date_unmarked
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      bean.setDateMarked( DateTimeFormat.convertToDate( stemp ) );
    }

    // validate must be param(s)
    if ( ( bean != null ) && ( bean.getId() < 1 ) ) {
      bean = null;
    }
    if ( ( bean != null ) && ( bean.getEventId() < 1 ) ) {
      bean = null;
    }
    if ( ( bean != null ) && ( StringUtils.isBlank( bean.getPhone() ) ) ) {
      bean = null;
    }

    return bean;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Util Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlEncryptPhoneNumber( String phoneNumber ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_ENCRYPT('" );
    sb.append( phoneNumber );
    sb.append( "','" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    return sb.toString();
  }

  private String sqlDecryptPhoneNumber( String tableAlias , boolean asPhone ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    if ( ( tableAlias != null ) && ( !tableAlias.equals( "" ) ) ) {
      sb.append( tableAlias );
      sb.append( "." );
    }
    sb.append( "encrypt_phone,'" );
    sb.append( keyPhoneNumber );
    sb.append( "') " );
    if ( asPhone ) {
      sb.append( "AS phone" );
    }
    return sb.toString();
  }

}
