package com.beepcast.channel.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FilesToZip {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private String zipFileName;
  private List listFileZips;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public FilesToZip( String zipFileName ) {
    this.zipFileName = zipFileName;
    listFileZips = new ArrayList();
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public Iterator iterator() {
    return listFileZips.iterator();
  }

  public void addFile( FileZip fileZip ) {
    if ( fileZip != null ) {
      listFileZips.add( fileZip );
    }
  }

  public void addFiles( String srcPathFolder , String zipBasePath ) {
    generateListFiles( srcPathFolder.length() , new File( srcPathFolder ) ,
        zipBasePath , listFileZips );
  }

  public int zip() throws Exception {
    int totalZipFiles = 0;

    FileOutputStream fos = null;
    CheckedOutputStream cos = null;
    BufferedOutputStream bos = null;
    ZipOutputStream zos = null;
    try {

      fos = new FileOutputStream( zipFileName );
      cos = new CheckedOutputStream( fos , new Adler32() );
      bos = new BufferedOutputStream( cos );
      zos = new ZipOutputStream( bos );

      Iterator iter = iterator();
      while ( iter.hasNext() ) {
        FileZip fileZip = (FileZip) iter.next();
        if ( fileZip == null ) {
          continue;
        }
        if ( zip( fileZip , zos ) ) {
          totalZipFiles = totalZipFiles + 1;
        }
      } // while ( iter.hasNext() ) {

      zos.closeEntry();

    } catch ( Exception e ) {

      throw e;

    } finally {

      if ( zos != null ) {
        try {
          zos.close();
        } catch ( Exception e ) {
        }
      }

      if ( bos != null ) {
        try {
          bos.close();
        } catch ( Exception e ) {
        }
      }

      if ( cos != null ) {
        try {
          cos.close();
        } catch ( Exception e ) {
        }
      }

      if ( fos != null ) {
        try {
          fos.close();
        } catch ( Exception e ) {
        }
      }

    }

    return totalZipFiles;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Core Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  private void generateListFiles( int pathFolderLength , File node ,
      String zipBasePath , List listFileZips ) {
    if ( node.isFile() ) {
      if ( ( zipBasePath == null ) || ( zipBasePath.equals( "" ) ) ) {
        zipBasePath = ".";
      }
      String zipPathFileName = zipBasePath.concat( "/" );
      zipPathFileName = zipPathFileName.concat( node.getName() );
      listFileZips.add( FileZipFactory.createFileZip( node.getAbsolutePath() ,
          zipPathFileName ) );
    }
    if ( node.isDirectory() ) {
      String[] subNodes = node.list();
      for ( String filename : subNodes ) {
        generateListFiles( pathFolderLength , new File( node , filename ) ,
            zipBasePath , listFileZips );
      }
    }
  }

  private boolean zip( FileZip fileZip , ZipOutputStream zos ) {
    boolean result = false;

    if ( fileZip == null ) {
      return result;
    }

    int length = 0;
    int bufferSize = 1024;
    byte[] bufferData = new byte[bufferSize];

    FileInputStream fis = null;
    BufferedInputStream bis = null;

    try {

      fis = new FileInputStream( fileZip.getSrcPathFileName() );
      bis = new BufferedInputStream( fis , bufferSize );

      ZipEntry zipEntry = new ZipEntry( fileZip.getZipPathFileName() );
      zos.putNextEntry( zipEntry );

      while ( ( length = bis.read( bufferData , 0 , bufferSize ) ) > -1 ) {
        zos.write( bufferData , 0 , length );
      }

      // result as true
      result = true;

    } catch ( Exception e ) {

    } finally {

      if ( bis != null ) {
        try {
          bis.close();
        } catch ( Exception e ) {
        }
      }

      if ( fis != null ) {
        try {
          fis.close();
        } catch ( Exception e ) {
        }
      }

    }

    return result;
  }

}
