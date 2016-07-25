package com.beepcast.channel;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import com.beepcast.channel.zip.FileZipFactory;
import com.beepcast.channel.zip.FilesToZip;
import com.beepcast.util.properties.GlobalEnvironment;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelManagementReportFileCommon {

  static final DLogContext lctx = new SimpleContext(
      "ChannelManagementReportFileCommon" );

  public static File generateFileTextObject( String fileReportNameWithoutExt ) {
    File frpt = new File( fileReportNameWithoutExt + ".csv" );
    return frpt;
  }

  public static DataOutputStream openFileTextStream( File frpt )
      throws FileNotFoundException {
    DataOutputStream dos = null;
    if ( frpt == null ) {
      return dos;
    }
    FileOutputStream fos = new FileOutputStream( frpt );
    dos = new DataOutputStream( fos );
    return dos;
  }

  public static boolean closeFileTextStream( DataOutputStream dos )
      throws IOException {
    boolean result = false;
    if ( dos != null ) {
      dos.close();
      result = true;
    }
    return result;
  }

  public static boolean writeToFileTextStream( String str , DataOutputStream dos ) {
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

  public static File getFileAttachment( String headerLog , int eventId ) {
    File fileAttachment = null;
    try {

      GlobalEnvironment globalEnv = GlobalEnvironment.getInstance();

      String pathMessageAttachments = globalEnv
          .getProperty( "platform.dir.message_attachments" );

      if ( ( pathMessageAttachments == null )
          || ( pathMessageAttachments.equals( "" ) ) ) {
        return fileAttachment;
      }

      if ( pathMessageAttachments.endsWith( "/" ) ) {
        pathMessageAttachments = pathMessageAttachments.substring( 0 ,
            pathMessageAttachments.length() - 1 );
      }

      fileAttachment = new File( pathMessageAttachments.concat( "/event-" )
          .concat( Integer.toString( eventId ) ) );

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to get file attachment" );
    }
    return fileAttachment;
  }

  public static boolean convertFileTextToZip( String headerLog , File fileText ,
      File fileAttachment ) {
    boolean result = false;

    if ( fileText == null ) {
      return result;
    }

    try {

      // prepare zip file name parameters
      String srcPathFileName = fileText.getAbsolutePath();
      String zipPathFileName = fileText.getName();
      String srcPathFileNameZip = FilenameUtils
          .removeExtension( srcPathFileName );
      srcPathFileNameZip = srcPathFileNameZip + ".zip";

      // prepare the files to zip engine
      FilesToZip filesToZip = new FilesToZip( srcPathFileNameZip );

      // add the file
      filesToZip.addFile( FileZipFactory.createFileZip( srcPathFileName ,
          zipPathFileName ) );

      // add attachment files
      if ( fileAttachment != null ) {
        filesToZip.addFiles( fileAttachment.getAbsolutePath() , "./attachment" );
      }

      // execute
      if ( filesToZip.zip() > 0 ) {
        result = true;
      }

    } catch ( Exception e ) {
      DLog.warning( lctx , headerLog + "Failed to convert file text to zip , "
          + e );
    }

    return result;
  }

}
