package com.beepcast.channel;

public class ChannelSessionNewStatus {

  public static final int ST_EMPTY = 0;
  public static final int ST_SCHEDULLED = 1;
  public static final int ST_STARTED = 2;
  public static final int ST_SUSPENDED = 3;
  public static final int ST_FINISHED = 4;
  public static final int ST_DELETED = 5;

  public static String toString( int status ) {
    String result = null;
    switch ( status ) {
    case 0 :
      result = "ST_EMPTY";
      break;
    case 1 :
      result = "ST_SCHEDULLED";
      break;
    case 2 :
      result = "ST_STARTED";
      break;
    case 3 :
      result = "ST_SUSPENDED";
      break;
    case 4 :
      result = "ST_FINISHED";
      break;
    case 5 :
      result = "ST_DELETED";
      break;
    }
    return result;
  }

}
