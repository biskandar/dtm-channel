package com.beepcast.channel;

import com.beepcast.database.DatabaseLibrary;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementTaskDAO {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelManagementTaskDAO" );

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

  public ChannelManagementTaskDAO() {
    dbLib = DatabaseLibrary.getInstance();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int deleteExpiredRecords( boolean simulation , int expiryDays ,
      int limitRecords , boolean debug ) {
    int result = 0;

    if ( expiryDays < 1 ) {
      return result;
    }

    if ( limitRecords < 1 ) {
      return result;
    }

    // compose sql
    String sqlTable = "channel_log".concat( simulation ? "_simulation" : "" );
    String sqlInterval = "INTERVAL " + expiryDays + " DAY ";
    String sqlDateThreshold = "DATE_SUB( CURDATE() , " + sqlInterval + " ) ";

    String sqlDelete = "DELETE FROM " + sqlTable + " ";
    String sqlWhere = "WHERE ( date_tm < " + sqlDateThreshold + " ) ";
    String sqlLimit = "LIMIT " + limitRecords + " ";

    String sql = sqlDelete + sqlWhere + sqlLimit;

    if ( debug ) {
      DLog.debug( lctx , "Perform " + sql );
    }

    Integer irslt = dbLib.executeQuery( "profiledb" , sql );
    if ( irslt != null ) {
      result = irslt.intValue();
    }

    return result;
  }

}
