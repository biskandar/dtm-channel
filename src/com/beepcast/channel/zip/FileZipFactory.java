package com.beepcast.channel.zip;

public class FileZipFactory {

  public static FileZip createFileZip( String srcPathFileName ,
      String zipPathFileName ) {
    FileZip fileZip = new FileZip();
    fileZip.setSrcPathFileName( srcPathFileName );
    fileZip.setZipPathFileName( zipPathFileName );
    return fileZip;
  }

}
