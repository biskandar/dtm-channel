package com.beepcast.channel;

import java.util.Iterator;

import com.beepcast.database.DatabaseLibrary;
import com.beepcast.database.DatabaseLibrary.QueryItem;
import com.beepcast.database.DatabaseLibrary.QueryResult;

public class ChannelLogQueryCommon {

  private static DatabaseLibrary dbLib = DatabaseLibrary.getInstance();

  public static long getFirstValueLong( String sql ) {
    long longResult = 0;
    String strResult = getFirstValueStr( sql );
    if ( ( strResult == null ) || ( strResult.equals( "" ) ) ) {
      return longResult;
    }
    try {
      longResult = Long.parseLong( strResult );
    } catch ( NumberFormatException e ) {
    }
    return longResult;
  }

  public static double getFirstValueDouble( String sql ) {
    double doubleResult = 0;
    String strResult = getFirstValueStr( sql );
    if ( ( strResult == null ) || ( strResult.equals( "" ) ) ) {
      return doubleResult;
    }
    try {
      doubleResult = Double.parseDouble( strResult );
    } catch ( NumberFormatException e ) {
    }
    return doubleResult;
  }

  public static String getFirstValueStr( String sql ) {
    String strResult = null;
    if ( sql == null ) {
      return strResult;
    }

    QueryResult qr = dbLib.simpleQuery( "transactiondb" , sql );
    if ( ( qr == null ) || ( qr.size() < 1 ) ) {
      return strResult;
    }
    Iterator it = qr.iterator();
    if ( !it.hasNext() ) {
      return strResult;
    }
    QueryItem qi = (QueryItem) it.next();
    if ( qi == null ) {
      return strResult;
    }
    strResult = qi.getFirstValue();
    return strResult;
  }

}
