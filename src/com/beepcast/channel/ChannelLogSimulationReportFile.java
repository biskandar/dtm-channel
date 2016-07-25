package com.beepcast.channel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.beepcast.database.ConnectionWrapper;
import com.beepcast.database.DatabaseLibrary;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelLogSimulationReportFile {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext(
      "ChannelLogSimulationReportFile" );

  static final int BUFFER = 2048;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private ChannelApp channelApp;
  private DatabaseLibrary dbLib;

  private ChannelSessionService csService;
  private ChannelSessionSimulationService cssService;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelLogSimulationReportFile() {

    channelApp = ChannelApp.getInstance();
    dbLib = DatabaseLibrary.getInstance();

    csService = new ChannelSessionService();
    cssService = new ChannelSessionSimulationService();

  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public boolean generateFileReport( String fileReportNameWithoutExt ,
      int channelSessionId ) {
    boolean result = false;

    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to generate file report "
          + ", found zero channel session id" );
      return result;
    }

    // headerLog
    String headerLog = "[ClsReportFile-" + channelSessionId + "] ";

    // read the channel session bean
    ChannelSessionBean csBean = csService.getChannelSession( channelSessionId );
    if ( csBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found invalid channel session id" );
      return result;
    }

    // read the channel session simulation bean
    ChannelSessionSimulationBean cssBean = cssService
        .selectByChannelSessionId( csBean.getId() );
    if ( cssBean == null ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found null matched channel session simulation bean" );
      return result;
    }

    // validate the channel session simulation still running
    if ( ( cssBean.isStarted() ) && ( !cssBean.isFinished() ) ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found channel session simulation still running" );
      return result;
    }

    try {

      // log first
      DLog.debug( lctx , headerLog + "Start to generate file report "
          + "from channel session id = " + channelSessionId );

      // create text file object
      File frpt = generateFileTextObject( fileReportNameWithoutExt );
      if ( frpt == null ) {
        DLog.warning( lctx , headerLog + "Failed generate file report object "
            + ", fileReportNameWithoutExt = " + fileReportNameWithoutExt );
        return result;
      }

      // open text file stream
      DataOutputStream dos = openFileTextStream( frpt );
      if ( dos == null ) {
        DLog.warning( lctx , headerLog + "Failed generate file report stream " );
        return result;
      }

      // extract header and write into file dos
      if ( !extractHeaderStoreToFile( headerLog , dos ) ) {
        DLog.warning( lctx , headerLog + "Failed to write header line "
            + "into file report stream " );
        return result;
      }

      // extract records and write into file dos
      int totalRecords = extractRecordsStoreToFile( channelApp.isDebug() ,
          headerLog , dos , csBean.getEventId() );
      DLog.debug( lctx , headerLog + "Stored total " + totalRecords
          + " record(s) into report file stream" );

      // close text file stream
      closeFileTextStream( dos );
      DLog.debug( lctx , headerLog + "Saved data report as text file" );

      // convert to zip file
      if ( !convertFileTextToZip( headerLog , frpt ) ) {
        DLog.warning( lctx , headerLog + "Failed to convert text file "
            + "report into zip format" );
      }

      // delete text file
      if ( !frpt.delete() ) {
        DLog.warning( lctx , headerLog + "Failed to delete unused "
            + "report text file" );
      }

      // log cleaning
      DLog.debug( lctx , headerLog + "Cleaned unused report text file" );

      // return result as true
      result = true;
    } catch ( FileNotFoundException fnfe ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", can not find file report , " + fnfe );
    } catch ( IOException ioe ) {
      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", can not close file report , " + ioe );
    }

    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private File generateFileTextObject( String fileReportNameWithoutExt ) {
    File frpt = new File( fileReportNameWithoutExt + ".csv" );
    return frpt;
  }

  private DataOutputStream openFileTextStream( File frpt )
      throws FileNotFoundException {
    DataOutputStream dos = null;
    if ( frpt == null ) {
      return dos;
    }
    FileOutputStream fos = new FileOutputStream( frpt );
    dos = new DataOutputStream( fos );
    return dos;
  }

  private boolean closeFileTextStream( DataOutputStream dos )
      throws IOException {
    boolean result = false;
    if ( dos != null ) {
      dos.close();
      result = true;
    }
    return result;
  }

  private boolean writeToFileTextStream( String str , DataOutputStream dos ) {
    boolean result = false;
    if ( str == null ) {
      return result;
    }
    try {
      dos.writeBytes( str );
      result = true;
    } catch ( IOException e ) {
    }
    return result;
  }

  private boolean convertFileTextToZip( String headerLog , File frpt )
      throws FileNotFoundException , IOException {
    boolean result = false;
    if ( frpt == null ) {
      return result;
    }

    // prepare zip file object
    String fnameZip = frpt.getAbsolutePath();
    fnameZip = FilenameUtils.removeExtension( fnameZip );
    fnameZip = fnameZip + ".zip";
    File frptZip = new File( fnameZip );

    // prepare zip file stream
    FileOutputStream dst = new FileOutputStream( frptZip );
    CheckedOutputStream cos = new CheckedOutputStream( dst , new Adler32() );
    ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( cos ) );

    // compressing from txt to zip
    byte data[] = new byte[BUFFER];
    FileInputStream fis = new FileInputStream( frpt );
    BufferedInputStream ori = new BufferedInputStream( fis , BUFFER );
    ZipEntry zipEntry = new ZipEntry( FilenameUtils.getName( frpt
        .getAbsolutePath() ) );
    zos.putNextEntry( zipEntry );
    int count = 0;
    while ( ( count = ori.read( data , 0 , BUFFER ) ) != -1 ) {
      zos.write( data , 0 , count );
    }
    ori.close();

    // save zip file
    zos.close();

    // log it
    DLog.debug( lctx , headerLog + "Compressed report text file "
        + "into zip format : " + frptZip.getAbsolutePath() );

    result = true;
    return result;
  }

  private int extractRecordsStoreToFile( boolean debug , String headerLog ,
      DataOutputStream dos , int eventId ) {
    int totalRecords = 0;

    if ( dos == null ) {
      return totalRecords;
    }

    // prepare database query params
    ConnectionWrapper cwChannelLogSimulation = null;
    Statement stChannelLogSimulation = null;
    ResultSet rsChannelLogSimulation = null;

    try {

      cwChannelLogSimulation = dbLib.getReaderConnection( "transactiondb" );
      if ( cwChannelLogSimulation == null ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db connection pool" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully get reader connection "
          + "for channel log simulation" );

      stChannelLogSimulation = cwChannelLogSimulation.createStatement();
      if ( stChannelLogSimulation == null ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db statement" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully create statement "
          + "for channel log simulation" );

      String sql = generateSql( eventId );
      if ( debug ) {
        DLog.debug( lctx , headerLog
            + "Perform sql from channel log simulation table : " + sql );
      }
      rsChannelLogSimulation = stChannelLogSimulation.executeQuery( sql );

      if ( rsChannelLogSimulation == null ) {
        DLog.warning( lctx , headerLog + "Failed to generate file report "
            + ", failed to generate db record set" );
        return totalRecords;
      }
      DLog.debug( lctx , headerLog + "Successfully create result set "
          + "for channel log simulation" );

      // loop result set channel log simulation
      while ( rsChannelLogSimulation.next() ) {
        try {
          if ( extractRecordStoreToFile( headerLog , rsChannelLogSimulation ,
              dos ) ) {
            totalRecords = totalRecords + 1;
          }
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog
              + "Failed to extract record store into the file , " + e );
        }
      } // end while

    } catch ( SQLException sqle ) {

      DLog.warning( lctx , headerLog + "Failed to generate file report "
          + ", found failed to query the table , " + sqle );

    } finally {

      if ( rsChannelLogSimulation != null ) {
        try {
          rsChannelLogSimulation.close();
          DLog.debug( lctx , headerLog + "Closed channel session "
              + "simulation result set object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close result set "
              + "channel session simulation , " + e );
        }
      }

      if ( stChannelLogSimulation != null ) {
        try {
          stChannelLogSimulation.close();
          DLog.debug( lctx , headerLog + "Closed channel session "
              + "simulation statement object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close statement "
              + "channel session simulation , " + e );
        }
      }

      if ( cwChannelLogSimulation != null ) {
        try {
          cwChannelLogSimulation.disconnect( true );
          DLog.debug( lctx , headerLog + "Disconnected channel session "
              + "simulation reader connection object" );
        } catch ( Exception e ) {
          DLog.warning( lctx , headerLog + "Failed to close connection reader "
              + "channel session simulation , " + e );
        }
      }

    } // end finally

    return totalRecords;
  }

  private String generateSql( int eventId ) {
    String sqlSelect = "SELECT sender_id , phone ";
    sqlSelect += ", message_count , message_text ";
    String sqlFrom = "FROM channel_log_simulation ";
    String sqlWhere = "WHERE ( marked = 0 ) ";
    sqlWhere += "AND ( event_id = " + eventId + " ) ";
    String sqlOrder = "ORDER BY id ASC ";
    String sql = sqlSelect + sqlFrom + sqlWhere + sqlOrder;
    return sql;
  }

  private boolean extractRecordStoreToFile( String headerLog ,
      ResultSet rsChannelLogSimulation , DataOutputStream dos )
      throws IOException , SQLException {
    boolean result = false;

    StringBuffer csvLine = new StringBuffer();
    csvLine.append( "" );
    csvLine.append( StringEscapeUtils.escapeCsv( rsChannelLogSimulation
        .getString( "sender_id" ) ) );
    csvLine.append( "," );
    csvLine.append( StringEscapeUtils.escapeCsv( rsChannelLogSimulation
        .getString( "phone" ) ) );
    csvLine.append( "," );
    csvLine.append( StringEscapeUtils.escapeCsv( rsChannelLogSimulation
        .getString( "message_count" ) ) );
    csvLine.append( "," );
    csvLine.append( StringEscapeUtils.escapeCsv( rsChannelLogSimulation
        .getString( "message_text" ) ) );
    csvLine.append( "\n" );

    result = writeToFileTextStream( csvLine.toString() , dos );
    return result;
  }

  private boolean extractHeaderStoreToFile( String headerLog ,
      DataOutputStream dos ) throws IOException {
    boolean result = false;

    StringBuffer csvLine = new StringBuffer();
    csvLine.append( "SenderId,PhoneNumber,MessageCount,MessageText\n" );

    result = writeToFileTextStream( csvLine.toString() , dos );
    DLog.debug( lctx , headerLog + "Writed a header line into "
        + "the report file stream , result = " + result );

    return result;
  }

}
