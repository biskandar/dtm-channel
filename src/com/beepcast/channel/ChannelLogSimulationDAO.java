package com.beepcast.channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;
import com.beepcast.encrypt.EncryptApp;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelLogSimulationDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelLogSimulationDAO" );

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

  public ChannelLogSimulationDAO() {
    dbLib = DatabaseLibrary.getInstance();
    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Update Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long updateSubscriberCountryProfile( int eventId ,
      String countryPhonePrefix , String countryCode , double countryCreditCost ) {
    long totalRecords = 0;

    if ( eventId < 1 ) {
      return totalRecords;
    }

    String sqlUpdate = "UPDATE channel_log_simulation ";
    String sqlSet = "SET country_code = '" + countryCode + "' ";
    sqlSet += ", country_credit_cost = " + countryCreditCost + " ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    sqlWhere += "AND ( phone LIKE '" + countryPhonePrefix + "%' ) ";

    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );

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

    String sqlUpdate = "UPDATE channel_log_simulation ";
    String sqlSet = "SET marked = 1 , date_marked = NOW() ";
    String sqlWhere = "WHERE ( subscribed = 1 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    DLog.debug( lctx , "Perform " + sql );
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
          + "in the channel log simulation table "
          + ", found blank channel log simulation id" );
      return result;
    }

    messageId = ( messageId == null ) ? "" : messageId;

    String sqlUpdate = "UPDATE channel_log_simulation ";
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

  public boolean updateMessageResult( ChannelLogSimulationBean bean ) {
    boolean result = false;
    if ( bean == null ) {
      return result;
    }

    // read params
    int id = bean.getId();
    String senderId = bean.getSenderId();
    int messageCount = bean.getMessageCount();
    String messageText = bean.getMessageText();

    // clean params
    id = ( id < 1 ) ? 0 : id;
    senderId = ( senderId == null ) ? "" : senderId;
    messageCount = ( messageCount < 1 ) ? 0 : messageCount;
    messageText = ( messageText == null ) ? "" : messageText;

    // compose sql
    String sqlUpdate = "UPDATE channel_log_simulation ";
    String sqlSet = "SET sender_id = '"
        + StringEscapeUtils.escapeSql( senderId ) + "' ";
    sqlSet += ", message_count = " + messageCount + " ";
    sqlSet += ", message_text = '" + StringEscapeUtils.escapeSql( messageText )
        + "' ";
    String sqlWhere = "WHERE ( id = " + id + " ) ";
    String sql = sqlUpdate + sqlSet + sqlWhere;

    // execute sql
    DLog.debug( lctx , "Perform " + sql );
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( ( irslt != null ) && ( irslt.intValue() > 0 ) ) {
      result = true;
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Select Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogSimulationBean selectById( boolean debug , int id ) {
    ChannelLogSimulationBean bean = null;
    if ( id < 1 ) {
      return bean;
    }

    // compose sql
    String sqlSelectFrom = sqlSelectFromChannelLogSimulation();
    String sqlWhere = "WHERE ( id = " + id + " ) ";
    String sql = sqlSelectFrom + sqlWhere;

    // execute sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return bean;
    }

    // fetch record
    Iterator it = qr.iterator();
    if ( it.hasNext() ) {
      bean = populateChannelLogSimulationBean( (QueryItem) it.next() );
    }

    return bean;
  }

  public List getChannelLogSimulationBeans( boolean debug , int eventId ,
      Boolean marked , Boolean useDateSend , int offset , int limit ) {
    List listClsBeans = new ArrayList();

    // compose sql

    String sqlSelectFrom = sqlSelectFromChannelLogSimulation();
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
      return listClsBeans;
    }

    // populate records

    ChannelLogSimulationBean clsBean = null;
    Iterator iter = qr.iterator();
    while ( iter.hasNext() ) {
      QueryItem qi = (QueryItem) iter.next();
      if ( qi == null ) {
        continue;
      }
      clsBean = populateChannelLogSimulationBean( qi );
      if ( clsBean == null ) {
        continue;
      }
      listClsBeans.add( clsBean );
    }

    return listClsBeans;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Report Function
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
    String sqlFrom = "FROM channel_log_simulation ";
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
    String sqlFrom = "FROM channel_log_simulation ";
    String sqlWhere = "WHERE ( marked = 1 ) ";
    sqlWhere += "AND ( event_id =  " + eventId + " ) ";
    String sql = sqlSelect + sqlFrom + sqlWhere;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    result = ChannelLogQueryCommon.getFirstValueLong( sql );
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Store / Clean Subscribers Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long storeSubscribers( boolean debug , int eventId ,
      int subscriberGroupId , boolean allowDuplicated ,
      double defaultCountryCreditCost , String simulationType ,
      long simulationNumbers ) {
    long totalInserted = 0;

    if ( subscriberGroupId < 1 ) {
      return totalInserted;
    }
    if ( eventId < 1 ) {
      return totalInserted;
    }

    String sqlSelectFromClientSubscriber = sqlSelectFromClientSubscriber(
        debug , eventId , subscriberGroupId , allowDuplicated ,
        defaultCountryCreditCost , simulationType , simulationNumbers );
    if ( StringUtils.isBlank( sqlSelectFromClientSubscriber ) ) {
      return totalInserted;
    }

    StringBuffer sbInsert = new StringBuffer();
    sbInsert.append( "INSERT INTO channel_log_simulation " );
    sbInsert.append( "( marked , phone , event_id , subscribed " );
    sbInsert.append( ", client_subscriber_id " );
    sbInsert.append( ", client_subscriber_custom_id " );
    sbInsert.append( ", cust_ref_id , cust_ref_code , description " );
    sbInsert.append( ", country_code , country_credit_cost " );
    sbInsert.append( ", date_tm , date_send , date_subscribed " );
    sbInsert.append( ", date_marked ) " );

    String sql = sbInsert.toString() + sqlSelectFromClientSubscriber;

    DLog.debug( lctx , "Perform " + sql );

    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      totalInserted = irslt.longValue();
    }

    return totalInserted;
  }

  public boolean cleanSubscribers( boolean debug , int eventId ) {
    boolean result = false;
    if ( eventId < 1 ) {
      return result;
    }

    // compose sql
    String sqlDelete = "DELETE FROM channel_log_simulation ";
    String sqlWhere = "WHERE ( event_id = " + eventId + " ) ";
    String sql = sqlDelete + sqlWhere;

    // execute sql
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
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String sqlSelectFromClientSubscriber( boolean debug , int evId ,
      int sgId , boolean allowDuplicated , double defaultCountryCreditCost ,
      String simulationType , long simulationNumbers ) {
    String sql = null;

    if ( evId < 1 ) {
      return sql;
    }
    if ( sgId < 1 ) {
      return sql;
    }

    StringBuffer sbSelect = new StringBuffer();
    sbSelect.append( "SELECT 1 AS \"marked\" " );
    sbSelect.append( ", " + sqlDecryptPhoneNumber( "cs" ) + " " );
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
    sbSelect.append( ", NULL AS \"date_send\" " );
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
      sbWhere.append( "  AND ( cs.active = 1 ) " );

      StringBuffer sbOrder = new StringBuffer();
      sbOrder.append( "ORDER BY " );
      if ( ( simulationType == null ) || ( simulationType.equals( "" ) ) ) {
        sbOrder.append( "cs.id ASC " );
      } else if ( simulationType.equals( ChannelSessionSimulationType.SEQUENCE ) ) {
        sbOrder.append( "cs.id ASC " );
      } else if ( simulationType.equals( ChannelSessionSimulationType.RANDOM ) ) {
        sbOrder.append( "RAND() " );
      } else {
        sbOrder.append( "cs.id ASC " );
      }

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
      sbInner.append( "  AND ( cs1.active = 1 ) " );
      sbInner.append( "GROUP BY cs1.encrypt_phone " );

      StringBuffer sbFrom = new StringBuffer();
      sbFrom.append( "FROM client_subscriber cs " );
      sbFrom.append( "INNER JOIN ( " + sbInner.toString() + " ) cs2 " );
      sbFrom.append( "  ON cs2.id = cs.id " );
      sbFrom.append( "LEFT OUTER JOIN client_subscriber_custom csc " );
      sbFrom.append( "  ON csc.client_subscriber_id = cs.id " );

      StringBuffer sbOrder = new StringBuffer();
      sbOrder.append( "ORDER BY " );
      if ( ( simulationType == null ) || ( simulationType.equals( "" ) ) ) {
        sbOrder.append( "cs.id ASC " );
      } else if ( simulationType.equals( ChannelSessionSimulationType.SEQUENCE ) ) {
        sbOrder.append( "cs.id ASC " );
      } else if ( simulationType.equals( ChannelSessionSimulationType.RANDOM ) ) {
        sbOrder.append( "RAND() " );
      } else {
        sbOrder.append( "cs.id ASC " );
      }

      sqlFromWhereOrder = sbFrom.toString() + sbOrder.toString();

    }

    StringBuffer sbLimit = new StringBuffer();
    sbLimit.append( "LIMIT " );
    sbLimit.append( simulationNumbers );
    sbLimit.append( " " );

    sql = sbSelect.toString() + sqlFromWhereOrder + sbLimit.toString();

    return sql;
  }

  private String sqlSelectFromChannelLogSimulation() {

    String sqlSelect = "SELECT id , marked , phone , event_id , subscribed ";
    sqlSelect += ", client_subscriber_id , client_subscriber_custom_id ";
    sqlSelect += ", cust_ref_id , cust_ref_code , description ";
    sqlSelect += ", country_code , country_credit_cost , date_tm ";
    sqlSelect += ", date_send , date_subscribed , date_marked , date_unmarked ";

    String sqlFrom = "FROM channel_log_simulation ";

    String sqlSelectFrom = sqlSelect + sqlFrom;

    return sqlSelectFrom;
  }

  private ChannelLogSimulationBean populateChannelLogSimulationBean(
      QueryItem queryItem ) {
    ChannelLogSimulationBean bean = null;
    if ( queryItem == null ) {
      return bean;
    }

    String stemp;
    int itemp;
    double dtemp;

    bean = new ChannelLogSimulationBean();

    stemp = (String) queryItem.get( 0 ); // id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 1 ); // marked
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setMarked( itemp );
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
        itemp = Integer.parseInt( stemp );
        bean.setEventId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 4 ); // subscribed
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setSubscribed( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 5 ); // client_subscriber_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setClientSubscriberId( itemp );
      } catch ( NumberFormatException e ) {
      }
    }
    stemp = (String) queryItem.get( 6 ); // client_subscriber_custom_id
    if ( ( stemp != null ) && ( !stemp.equals( "" ) ) ) {
      try {
        itemp = Integer.parseInt( stemp );
        bean.setClientSubscriberCustomId( itemp );
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
        dtemp = Double.parseDouble( stemp );
        bean.setCountryCreditCost( dtemp );
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

  private String sqlDecryptPhoneNumber( String tableAlias ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    sb.append( tableAlias );
    sb.append( ".encrypt_phone,'" );
    sb.append( keyPhoneNumber );
    sb.append( "') AS phone" );
    return sb.toString();
  }

}
