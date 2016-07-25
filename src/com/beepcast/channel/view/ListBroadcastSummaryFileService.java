package com.beepcast.channel.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.RandomStringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ListBroadcastSummaryFileService {

  static final DLogContext lctx = new SimpleContext(
      "ListBroadcastsSummaryFileService" );

  private ListBroadcastSummaryFileDAO dao;

  public ListBroadcastSummaryFileService( String strClientIds ,
      String strEventIds , Date dateStarted , Date dateFinished ) {
    dao = new ListBroadcastSummaryFileDAO( strClientIds , strEventIds ,
        dateStarted , dateFinished );
  }

  public ListBroadcastSummaryFileService( int clientId , String strEventIds ,
      Date dateStarted , Date dateFinished ) {
    dao = new ListBroadcastSummaryFileDAO( Integer.toString( clientId ) ,
        strEventIds , dateStarted , dateFinished );
  }

  public String filePathWithoutExtension() {
    String strFileName = null;

    // add date created time
    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
    String dateTimeNowStr = sdf.format( now );

    // random alphanumeric 5 digit
    String randAlphanum5digits = RandomStringUtils.randomAlphanumeric( 5 );

    // compose str file name
    strFileName = "broadcast-summary-report-".concat( dateTimeNowStr )
        .concat( "-" ).concat( randAlphanum5digits );

    return strFileName;
  }

  public long exportToFile( String reportFileNameWithoutExt ) {
    long totalRecords = 0;
    long deltaTime = System.currentTimeMillis();
    totalRecords = dao.exportToFile( reportFileNameWithoutExt );
    deltaTime = System.currentTimeMillis() - deltaTime;
    DLog.debug( lctx , "Successfully export to file , total records = "
        + totalRecords + " , take " + deltaTime + " ms " );
    return totalRecords;
  }

}
