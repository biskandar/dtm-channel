package com.beepcast.channel.view;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class ChannelSessionReportInfoService {

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionReportInfoService" );

  private ChannelSessionReportInfoDAO dao;

  public ChannelSessionReportInfoService() {
    dao = new ChannelSessionReportInfoDAO();
  }

  public ChannelSessionReportInfoBean getChannelSessionReportInfo(
      int channelSessionId ) {
    ChannelSessionReportInfoBean bean = null;
    if ( channelSessionId < 1 ) {
      DLog.warning( lctx , "Failed to get channel session report info "
          + ", found zero channelSessionId" );
      return bean;
    }
    bean = dao.getChannelSessionReportInfo( channelSessionId );
    return bean;
  }

}
