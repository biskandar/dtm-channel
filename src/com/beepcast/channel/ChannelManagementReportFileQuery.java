package com.beepcast.channel;

import com.beepcast.encrypt.EncryptApp;

public class ChannelManagementReportFileQuery {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String keyPhoneNumber;
  private String keyMessage;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportFileQuery() {

    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );
    keyMessage = encryptApp.getKeyValue( EncryptApp.KEYNAME_MESSAGE );

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String generateSqlGateway( int channelSessionId ) {
    String sqlSelect = "SELECT " + sqlDecryptPhoneNumber( "gl" , true )
        + " , gl.`status` , gl.message_id , gl.message_count , "
        + sqlDecryptMessage( "gl" , true )
        + " , gl.retry , gl.date_tm , gl.status_date_tm , gl.debit_amount "
        + " , gx.xipme_code , gxh.date_hit ";
    String sqlFrom = "FROM gateway_log gl ";
    sqlFrom += "LEFT OUTER JOIN gateway_xipme gx ON "
        + "gx.gateway_xipme_id = gl.gateway_xipme_id ";
    sqlFrom += "LEFT OUTER JOIN gateway_xipme_hit gxh ON "
        + "( gxh.gateway_xipme_id = gx.gateway_xipme_id ) "
        + "AND ( gxh.xipme_code = gx.xipme_code ) ";
    String sqlWhere = "WHERE ( gl.channel_session_id = " + channelSessionId
        + " ) ";
    String sqlOrder = "ORDER BY " + sqlDecryptPhoneNumber( "gl" , false )
        + " ASC , gl.log_id ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;
    return sql;
  }

  public String generateSqlTransaction( int channelSessionId ) {
    String sqlSelect = "SELECT "
        + sqlDecryptDestinationAddress( "tn" , true )
        + " , tn.internal_status_code , tn.internal_message_id , tn.message_count , "
        + sqlDecryptMessageContent( "tn" , true )
        + " , tn.retry_count , tn.date_submitted , tn.date_received_status "
        + ", tn.debit_amount ";
    String sqlFrom = "FROM `transaction` tn ";
    String sqlWhere = "WHERE ( tn.channel_session_id = " + channelSessionId
        + " ) ";
    String sqlOrder = "ORDER BY " + sqlDecryptDestinationAddress( "tn" , false )
        + " ASC , tn.log_id ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;
    return sql;
  }

  public String generateSqlSubscriber( int groupSubscriberId ) {
    String sqlSelect = "SELECT " + sqlDecryptPhoneNumber( "cs" , true )
        + " , cs.subscribed , cs.subscribed_event_id , cs.date_subscribed "
        + ", cs.global_subscribed , cs.date_global_subscribed "
        + ", cs.cust_ref_id , cs.cust_ref_code , cs.description ";
    String sqlFrom = "FROM client_subscriber cs ";
    String sqlWhere = "WHERE ( cs.subscriber_group_id = " + groupSubscriberId
        + " ) ";
    String sqlOrder = "ORDER BY " + sqlDecryptPhoneNumber( "cs" , false )
        + " ASC , cs.id ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;
    return sql;
  }

  public String generateSqlChannelSessionSubscriber( int channelSessionId ) {
    String sqlSelect = "SELECT " + sqlDecryptPhoneNumber( "css" , true ) + " ";
    String sqlFrom = "FROM channel_session_subscriber css ";
    String sqlWhere = "WHERE ( css.channel_session_id = " + channelSessionId
        + " ) AND ( css.active = 1 ) ";
    String sqlOrder = "ORDER BY css.id ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;
    return sql;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Util Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String sqlDecryptPhoneNumber( String tblAlias , boolean withAs ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    if ( tblAlias != null ) {
      sb.append( tblAlias );
      sb.append( "." );
    }
    sb.append( "encrypt_phone,'" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    if ( withAs ) {
      sb.append( " AS phone" );
    }
    return sb.toString();
  }

  private String sqlDecryptMessage( String tblAlias , boolean withAs ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    if ( tblAlias != null ) {
      sb.append( tblAlias );
      sb.append( "." );
    }
    sb.append( "encrypt_message,'" );
    sb.append( keyMessage );
    sb.append( "')" );
    if ( withAs ) {
      sb.append( " AS message" );
    }
    return sb.toString();
  }

  private String sqlDecryptDestinationAddress( String tblAlias , boolean withAs ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    if ( tblAlias != null ) {
      sb.append( tblAlias );
      sb.append( "." );
    }
    sb.append( "encrypt_destination_address,'" );
    sb.append( keyPhoneNumber );
    sb.append( "')" );
    if ( withAs ) {
      sb.append( " AS destination_address" );
    }
    return sb.toString();
  }

  private String sqlDecryptMessageContent( String tblAlias , boolean withAs ) {
    StringBuffer sb = new StringBuffer();
    sb.append( "AES_DECRYPT(" );
    if ( tblAlias != null ) {
      sb.append( tblAlias );
      sb.append( "." );
    }
    sb.append( "encrypt_message_content,'" );
    sb.append( keyMessage );
    sb.append( "')" );
    if ( withAs ) {
      sb.append( " AS message_content" );
    }
    return sb.toString();
  }

}
