package com.beepcast.channel.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.database.ConnectionWrapper;
import com.beepcast.database.DatabaseLibrary;
import com.beepcast.model.event.ProcessCommon;
import com.beepcast.model.util.DateTimeFormat;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.mysql.jdbc.CommunicationsException;

public class ListBroadcastSummaryFileDAO {

  static final DLogContext lctx = new SimpleContext(
      "InteractionTrafficFileDAO" );

  static final int BUFFER = 2048;

  private DatabaseLibrary dbLib;
  private String sql;

  public ListBroadcastSummaryFileDAO( String strClientIds , String strEventIds ,
      Date dateStarted , Date dateFinished ) {
    dbLib = DatabaseLibrary.getInstance();

    // compose sql
    String sqlSelectFrom = ChannelSessionReportInfoQuery.sqlSelectFrom();
    String sqlWhere = ListBroadcastSummaryQuery.sqlWhere( strClientIds ,
        strEventIds , dateStarted , dateFinished );
    String sqlGroupBy = "GROUP BY cs.id ";
    String sqlOrderBy = "ORDER BY cs.id DESC ";
    sql = sqlSelectFrom + sqlWhere + sqlGroupBy + sqlOrderBy;

  }

  public long exportToFile( String reportFileNameWithoutExt ) {
    long totalRecords = 0;
    DLog.debug( lctx , "Start exporting to file" );
    DataOutputStream dos = openFile( reportFileNameWithoutExt );
    if ( dos == null ) {
      DLog.warning( lctx , "Failed to open report file" );
      return totalRecords;
    }
    if ( !writeHeader( dos ) ) {
      DLog.warning( lctx , "Failed to write header report" );
      return totalRecords;
    }
    totalRecords = writeRecords( dos );
    if ( !closeFile( reportFileNameWithoutExt , dos ) ) {
      DLog.warning( lctx , "Failed to close report file" );
      return totalRecords;
    }
    DLog.debug( lctx , "Successfully exported to file" );
    return totalRecords;
  }

  private boolean writeRecord( DataOutputStream dos , ResultSet rs ) {
    boolean result = false;
    if ( dos == null ) {
      return result;
    }
    if ( rs == null ) {
      return result;
    }
    try {

      // read all required fields

      int id = rs.getInt( "id" );
      if ( id < 1 ) {
        return result;
      }
      int eventId = rs.getInt( "event_id" );
      if ( eventId < 1 ) {
        return result;
      }

      String clientName = rs.getString( "company_name" );
      clientName = ( clientName == null ) ? "" : clientName.trim();
      String eventName = rs.getString( "event_name" );
      eventName = ( eventName == null ) ? "" : eventName.trim();
      String listName = rs.getString( "group_name" );
      listName = ( listName == null ) ? "" : listName.trim();
      String eventMessage = ProcessCommon.getFirstProcessBeanResponse( eventId );
      eventMessage = ( eventMessage == null ) ? "" : eventMessage.trim();

      Date dateReportCreated = DateTimeFormat.convertToDate( rs
          .getString( "date_updated" ) );
      Date dateBroadcastStarted = DateTimeFormat.convertToDate( rs
          .getString( "date_started" ) );
      Date dateBroadcastFinished = DateTimeFormat.convertToDate( rs
          .getString( "date_finished" ) );

      long totalNumbers = rs.getLong( "total_numbers" );
      long totalRemains = rs.getLong( "total_remains" );
      long totalSent = rs.getLong( "total_sent" );
      long totalRetry = rs.getLong( "total_retry" );
      long totalSubmitted = totalSent - totalRetry;
      long totalDelivered = rs.getLong( "total_dn_delivered" );
      long totalInvalid = rs.getLong( "total_dn_invalid_number" );
      long totalPending = rs.getLong( "total_dn_empty" );
      long totalFailed = totalSubmitted
          - ( totalDelivered + totalInvalid + totalPending );
      long totalUnsubscribed = rs.getLong( "total_unsubs" );
      long totalVisitors = rs.getLong( "total_visitors" );
      long totalHits = rs.getLong( "total_hits" );

      // prepare the csv format

      StringBuffer csvLines = new StringBuffer();
      csvLines.append( ( dateReportCreated == null ) ? "" : DateTimeFormat
          .convertToString( dateReportCreated ) );
      csvLines.append( "," );
      csvLines.append( ( dateBroadcastStarted == null ) ? "" : DateTimeFormat
          .convertToString( dateBroadcastStarted ) );
      csvLines.append( "," );
      csvLines.append( ( dateBroadcastFinished == null ) ? "" : DateTimeFormat
          .convertToString( dateBroadcastFinished ) );
      csvLines.append( "," );
      csvLines.append( StringEscapeUtils.escapeCsv( eventName ) );
      csvLines.append( "," );
      csvLines.append( StringEscapeUtils.escapeCsv( eventMessage ) );
      csvLines.append( "," );
      csvLines.append( StringEscapeUtils.escapeCsv( listName ) );
      csvLines.append( "," );
      csvLines.append( totalNumbers );
      csvLines.append( "," );
      csvLines.append( totalSubmitted );
      csvLines.append( "," );
      csvLines.append( totalRetry );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalSubmitted , totalRetry ) );
      csvLines.append( "," );
      csvLines.append( totalDelivered );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalSubmitted , totalDelivered ) );
      csvLines.append( "," );
      csvLines.append( totalInvalid );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalSubmitted , totalInvalid ) );
      csvLines.append( "," );
      csvLines.append( totalFailed );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalSubmitted , totalFailed ) );
      csvLines.append( "," );
      csvLines.append( totalPending );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalSubmitted , totalPending ) );
      csvLines.append( "," );
      csvLines.append( totalUnsubscribed );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalDelivered , totalUnsubscribed ) );
      csvLines.append( "," );
      csvLines.append( totalVisitors );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalDelivered , totalVisitors ) );
      csvLines.append( "," );
      csvLines.append( totalHits );
      csvLines.append( "," );
      csvLines.append( ChannelSessionInfoService.percentageString(
          totalDelivered , totalHits ) );
      csvLines.append( "\n" );

      // write records into file
      dos.writeBytes( csvLines.toString() );

      // result as true
      result = true;
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to write record from trx , " + e );
    }
    return result;
  }

  private long writeRecords( DataOutputStream dos ) {
    long totalRecords = 0;
    if ( dos == null ) {
      return totalRecords;
    }

    String databaseName = "transactiondb";
    ConnectionWrapper conn = dbLib.getReaderConnection( databaseName );
    if ( ( conn != null ) && ( conn.isConnected() ) ) {
      Statement statement = null;
      ResultSet resultSet = null;
      try {
        statement = conn.createStatement();
        if ( statement != null ) {
          DLog.debug( lctx , "Perform " + sql );
          resultSet = statement.executeQuery( sql );
        }
        if ( resultSet != null ) {
          while ( resultSet.next() ) {
            if ( writeRecord( dos , resultSet ) ) {
              totalRecords = totalRecords + 1;
            }
          }
        }
      } catch ( CommunicationsException communicationsException ) {
        DLog.error( lctx , "[" + databaseName + "] Database query failed , "
            + communicationsException );
      } catch ( SQLException sqlException ) {
        DLog.error( lctx , "[" + databaseName + "] Database query failed , "
            + sqlException );
      }
      try {
        if ( resultSet != null ) {
          resultSet.close();
          resultSet = null;
        }
      } catch ( SQLException e ) {
        DLog.error( lctx , "[" + databaseName
            + "] Failed to close the ResultSet object , " + e );
      }
      try {
        if ( statement != null ) {
          statement.close();
          statement = null;
        }
      } catch ( SQLException e ) {
        DLog.error( lctx , "[" + databaseName
            + "] Failed to close the Statement object , " + e );
      }
      conn.disconnect( true );
    }

    return totalRecords;
  }

  private boolean writeHeader( DataOutputStream dos ) {
    boolean result = false;
    if ( dos == null ) {
      return result;
    }
    try {
      dos.writeBytes( "ReportCreated,BroadcastStarted,BroadcastFinished" );
      dos.writeBytes( ",EventName,EventMessage,ListName,ListSize" );
      dos.writeBytes( ",SubmittedNumbers,RetryNumbers,RetryPercentage" );
      dos.writeBytes( ",DeliveredNumbers,DeliveredPercentage,InvalidNumbers" );
      dos.writeBytes( ",InvalidPercentage,FailedNumbers,FailedPercentage" );
      dos.writeBytes( ",PendingNumbers,PendingPercentage" );
      dos.writeBytes( ",UnsubscribedNumbers,UnsubscribedPercentage" );
      dos.writeBytes( ",XipMeVisitors,XipMeVisitorsPercentage" );
      dos.writeBytes( ",XipMeClicks,XipMeClicksPercentage" );
      dos.writeBytes( "\n" );
      result = true;
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to write file , " + e );
    }
    return result;
  }

  private DataOutputStream openFile( String reportFileNameWithoutExt ) {
    DataOutputStream dos = null;
    try {

      // put txt extension
      String txtFileName = reportFileNameWithoutExt + ".csv";
      DLog.debug( lctx , "Create text csv file = " + txtFileName );

      // prepare txt file object
      File file = new File( txtFileName );

      // open and return txt stream file
      FileOutputStream fos = new FileOutputStream( file );
      dos = new DataOutputStream( fos );

    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to open file , " + e );
    }
    return dos;
  }

  private boolean closeFile( String reportFileNameWithoutExt ,
      DataOutputStream dos ) {
    boolean result = false;
    if ( dos == null ) {
      return result;
    }
    try {

      // put txt extension
      String txtFileName = reportFileNameWithoutExt + ".csv";

      // save txt file
      DLog.debug( lctx , "Saved text file = " + txtFileName );
      dos.close();

      // prepare text file object
      File file = new File( txtFileName );

      // generate to zip file
      String zipFilename = "";
      zipFilename += FilenameUtils.removeExtension( file.getAbsolutePath() );
      zipFilename += ".zip";

      DLog.debug( lctx , "Convert text file to zip file = " + zipFilename );

      FileOutputStream dst = new FileOutputStream( zipFilename );
      CheckedOutputStream cos = new CheckedOutputStream( dst , new Adler32() );
      ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( cos ) );

      byte data[] = new byte[BUFFER];
      FileInputStream fis = new FileInputStream( file );
      BufferedInputStream ori = new BufferedInputStream( fis , BUFFER );
      ZipEntry zipEntry = new ZipEntry( FilenameUtils.getName( file
          .getAbsolutePath() ) );
      zos.putNextEntry( zipEntry );
      int count = 0;
      while ( ( count = ori.read( data , 0 , BUFFER ) ) != -1 ) {
        zos.write( data , 0 , count );
      }
      ori.close();

      // save zip file
      DLog.debug( lctx , "Saved zip file = " + zipFilename );
      zos.close();

      // delete text file
      DLog.debug( lctx , "Delete text file = " + txtFileName );
      file.delete();

      result = true;
    } catch ( Exception e ) {
      DLog.warning( lctx , "Failed to open file , " + e );
    }
    return result;
  }

}
