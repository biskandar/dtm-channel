package com.beepcast.channel;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelMonitorBean {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constanta
  //
  // ////////////////////////////////////////////////////////////////////////////

  static final DLogContext lctx = new SimpleContext( "ChannelMonitorBean" );

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Data Member
  //
  // ////////////////////////////////////////////////////////////////////////////

  private final NumberFormat percentageFormatter;

  private int channelSessionId;
  private int eventId;
  private String eventName;
  private int listId;
  private String listName;
  private long totalAllNumber;
  private int initLoad;

  private long totalProcessedNumber1;
  private long totalProcessedNumber2;
  private long totalProcessedNumber3;
  private long totalProcessedNumber4;

  private long dateProcessedNumber1;
  private long dateProcessedNumber2;
  private long dateProcessedNumber3;
  private long dateProcessedNumber4;

  private double percentageProcessed;
  private int secondRemains;

  private long dateAccess;

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelMonitorBean() {
    percentageFormatter = new DecimalFormat( "#0" );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Set / Get Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName( String eventName ) {
    this.eventName = eventName;
  }

  public int getListId() {
    return listId;
  }

  public void setListId( int listId ) {
    this.listId = listId;
  }

  public String getListName() {
    return listName;
  }

  public void setListName( String listName ) {
    this.listName = listName;
  }

  public long getTotalAllNumber() {
    return totalAllNumber;
  }

  public void setTotalAllNumber( long totalAllNumber ) {
    this.totalAllNumber = totalAllNumber;
  }

  public int getInitLoad() {
    return initLoad;
  }

  public void setInitLoad( int initLoad ) {
    this.initLoad = initLoad;
  }

  public double getPercentageProcessed() {
    return percentageProcessed;
  }

  public int getSecondRemains() {
    return secondRemains;
  }

  public String getPercentageProcessedStr() {
    StringBuffer sb = new StringBuffer();
    sb.append( percentageFormatter.format( percentageProcessed ) );
    sb.append( " %" );
    return sb.toString();
  }

  public String getSecondRemainsStr() {
    return ChannelMonitorSupport.readableShowTime( secondRemains );
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public long getAgeInMillis() {
    long now = System.currentTimeMillis();
    long age = now - dateAccess;
    return age;
  }

  public void reset() {
    dateAccess = System.currentTimeMillis();
    totalProcessedNumber1 = totalProcessedNumber2 = totalProcessedNumber3 = totalProcessedNumber4 = -1;
    dateProcessedNumber1 = dateProcessedNumber2 = dateProcessedNumber3 = dateProcessedNumber4 = dateAccess;
    percentageProcessed = 0;
    secondRemains = -1;
  }

  public boolean updateNumber( long totalProcessedNumber ,
      long dateProcessedNumber , boolean debug ) {
    boolean result = false;

    dateAccess = System.currentTimeMillis();

    if ( totalProcessedNumber < 0 ) {
      if ( debug ) {
        DLog.warning( lctx , "Failed to update number "
            + ", found total processed number = " + totalProcessedNumber );
      }
      return result;
    }

    result = true;

    if ( dateProcessedNumber < 1 ) {
      dateProcessedNumber = System.currentTimeMillis();
    }

    totalProcessedNumber4 = totalProcessedNumber3;
    totalProcessedNumber3 = totalProcessedNumber2;
    totalProcessedNumber2 = totalProcessedNumber1;
    totalProcessedNumber1 = totalProcessedNumber;

    dateProcessedNumber4 = dateProcessedNumber3;
    dateProcessedNumber3 = dateProcessedNumber2;
    dateProcessedNumber2 = dateProcessedNumber1;
    dateProcessedNumber1 = dateProcessedNumber;

    return result;
  }

  public double calculatePercentageProcessed() {
    double result = 0;
    if ( totalAllNumber < 1 ) {
      return result;
    }
    percentageProcessed = ( (double) totalProcessedNumber1 / (double) totalAllNumber ) * 100.0;
    result = percentageProcessed;
    return result;
  }

  public int calculateSecondRemains() {

    long deltaTotalNumber = 0 , deltaDateSecond = 0;

    int cntThrottle = 0;
    double avgThrottle = 0.0;

    long totalRemains = totalAllNumber - totalProcessedNumber1;
    if ( totalRemains < 1 ) {
      secondRemains = 0;
      return secondRemains;
    }

    if ( ( totalProcessedNumber1 > -1 ) && ( totalProcessedNumber2 > -1 ) ) {
      deltaTotalNumber = totalProcessedNumber1 - totalProcessedNumber2;
      deltaDateSecond = ( dateProcessedNumber1 - dateProcessedNumber2 ) / 1000;
      if ( deltaDateSecond > 0 ) {
        cntThrottle = cntThrottle + 1;
        avgThrottle += (double) ( (double) deltaTotalNumber / (double) deltaDateSecond );
      }
    }

    if ( ( totalProcessedNumber2 > -1 ) && ( totalProcessedNumber3 > -1 ) ) {
      deltaTotalNumber = totalProcessedNumber2 - totalProcessedNumber3;
      deltaDateSecond = ( dateProcessedNumber2 - dateProcessedNumber3 ) / 1000;
      if ( deltaDateSecond > 0 ) {
        cntThrottle = cntThrottle + 1;
        avgThrottle += (double) ( (double) deltaTotalNumber / (double) deltaDateSecond );
      }
    }

    if ( ( totalProcessedNumber3 > -1 ) && ( totalProcessedNumber4 > -1 ) ) {
      deltaTotalNumber = totalProcessedNumber3 - totalProcessedNumber4;
      deltaDateSecond = ( dateProcessedNumber3 - dateProcessedNumber4 ) / 1000;
      if ( deltaDateSecond > 0 ) {
        cntThrottle = cntThrottle + 1;
        avgThrottle += (double) ( (double) deltaTotalNumber / (double) deltaDateSecond );
      }
    }

    if ( cntThrottle < 1 ) {
      cntThrottle = cntThrottle + 1;
      avgThrottle += initLoad;
    }

    avgThrottle = avgThrottle / (double) cntThrottle;

    secondRemains = (int) ( (double) totalRemains / (double) avgThrottle );

    return secondRemains;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Helper
  //
  // ////////////////////////////////////////////////////////////////////////////

  public String progressToString() {
    StringBuffer sb = new StringBuffer();
    sb.append( getPercentageProcessedStr() );
    if ( ( secondRemains > 0 ) && ( secondRemains < 172800 ) ) {
      sb.append( " - " );
      sb.append( getSecondRemainsStr() );
      sb.append( " remains" );
    }
    return sb.toString();
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append( "ChannelMonitorBean , channelSessionId = " + channelSessionId
        + " , totalAllNumber = " + totalAllNumber
        + " number(s) , lastTotalProcessedNumber = " + totalProcessedNumber1
        + " number(s) , lastDateProcessedNumber = " + dateProcessedNumber1
        + " ms , percentageProcessed = " + getPercentageProcessedStr()
        + " , secondRemains = " + getSecondRemainsStr() );
    return sb.toString();
  }

}
