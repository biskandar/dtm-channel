package com.beepcast.channel.zip;

public class FileZip {

  private String srcPathFileName;
  private String zipPathFileName;

  public FileZip() {
  }

  public String getSrcPathFileName() {
    return srcPathFileName;
  }

  public void setSrcPathFileName( String srcPathFileName ) {
    this.srcPathFileName = srcPathFileName;
  }

  public String getZipPathFileName() {
    return zipPathFileName;
  }

  public void setZipPathFileName( String zipPathFileName ) {
    this.zipPathFileName = zipPathFileName;
  }

  public String toString() {
    return "FileZip [srcPathFileName=" + srcPathFileName + ", zipPathFileName="
        + zipPathFileName + "]";
  }

}
