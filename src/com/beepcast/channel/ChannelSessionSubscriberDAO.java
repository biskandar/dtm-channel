package com.beepcast.channel;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.encrypt.EncryptApp;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionSubscriberDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionSubscriberDAO" );

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

  public ChannelSessionSubscriberDAO() {
    dbLib = DatabaseLibrary.getInstance();
    EncryptApp encryptApp = EncryptApp.getInstance();
    keyPhoneNumber = encryptApp.getKeyValue( EncryptApp.KEYNAME_PHONENUMBER );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int insertUnprocessedNumbers( boolean debug , int channelSessionId ,
      int eventId ) {
    int totalRecords = 0;

    if ( channelSessionId < 1 ) {
      return totalRecords;
    }
    if ( eventId < 1 ) {
      return totalRecords;
    }

    // compose sql

    String sqlInsert = "INSERT INTO channel_session_subscriber ";
    sqlInsert += "(channel_session_id,type,phone,encrypt_phone";
    sqlInsert += ",active,date_inserted,date_updated) ";

    String sqlInnerSelect = "SELECT " + channelSessionId + " , '"
        + ChannelSessionSubscriberType.Unprocessed + "' , "
        + sqlDecryptPhoneNumber( "cl" )
        + " , cl.encrypt_phone , 1 , NOW() , NOW() ";
    String sqlInnerFrom = "FROM channel_log cl ";
    String sqlInnerWhere = "WHERE ( cl.event_id = " + eventId
        + " ) AND ( cl.marked = 1 ) ";
    String sqlInnerOrder = "ORDER BY cl.id ASC ";
    String sqlInner = sqlInnerSelect + sqlInnerFrom + sqlInnerWhere
        + sqlInnerOrder;

    String sql = sqlInsert + sqlInner;

    // execute sql
    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }
    Integer irslt = dbLib.executeQuery( "transactiondb" , sql );
    if ( irslt != null ) {
      totalRecords = irslt.intValue();
    }

    return totalRecords;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  // ...

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
