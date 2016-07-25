package com.beepcast.channel.view;

import java.text.DecimalFormat;
import java.util.List;

import com.beepcast.channel.ChannelSessionSchedulesService;
import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionInfoService {

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionInfoService" );

  private ChannelSessionInfoDAO dao;
  private ChannelSessionSchedulesService csssService;

  public ChannelSessionInfoService() {
    dao = new ChannelSessionInfoDAO();
    csssService = new ChannelSessionSchedulesService();
  }

  public ChannelSessionInfoBean getChannelSessionInfo( int channelSessionId ) {
    ChannelSessionInfoBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to get channel session info "
          + ", found zero channelSessionId" );
      return bean;
    }
    bean = dao.getChannelSessionInfo( channelSessionId );
    List listChannelSessionSchedules = csssService.select( channelSessionId );
    if ( listChannelSessionSchedules != null ) {
      bean.getListChannelSessionSchedules().clear();
      bean.getListChannelSessionSchedules()
          .addAll( listChannelSessionSchedules );
    }
    return bean;
  }

  public static String percentageString( double total , double current ) {
    String result = null;
    double percentage = percentageDouble( total , current );
    DecimalFormat format = new DecimalFormat( "#0.00" );
    result = format.format( percentage );
    return result;
  }

  public static double percentageDouble( double total , double current ) {
    double result = 0.0;
    if ( total == 0.0 ) {
      return result;
    }
    result = current * 100.0 / total;
    return result;
  }

}
