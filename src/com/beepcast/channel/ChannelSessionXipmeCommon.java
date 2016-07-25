package com.beepcast.channel;

import org.apache.commons.lang.StringUtils;

import com.firsthop.common.log.DLog;
import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;
import com.xipme.api.SummaryMapReport;
import com.xipme.api.commands.CommandResponseSummaryMapReport;

public class ChannelSessionXipmeCommon {

  static final DLogContext lctx = new SimpleContext(
      "ChannelSessionXipmeCommon" );

  public static ChannelSessionXipmeBean convert( int channelSessionId ,
      CommandResponseSummaryMapReport cmdResp ) {
    ChannelSessionXipmeBean bean = null;

    if ( cmdResp == null ) {
      DLog.warning( lctx , "Failed to convert , found null command response" );
      return bean;
    }

    String xipmeCode = cmdResp.getMapId();
    if ( StringUtils.isBlank( xipmeCode ) ) {
      DLog.warning( lctx , "Failed to convert , found blank xipme code" );
      return bean;
    }

    SummaryMapReport mapReport = cmdResp.getMapReport();
    if ( mapReport == null ) {
      DLog.warning( lctx , "Failed to convert , found null summary map report" );
      return bean;
    }

    bean = ChannelSessionXipmeBeanFactory.createChannelSessionXipmeBean(
        channelSessionId , xipmeCode , mapReport.getTotalMembers() ,
        mapReport.getTotalMembersMembers() , mapReport.getTotalCustomers() ,
        mapReport.getTotalVisitors() , mapReport.getTotalMembersVisitors() ,
        mapReport.getTotalBotVisitors() ,
        mapReport.getTotalMembersBotVisitors() , mapReport.getTotalHits() ,
        mapReport.getTotalMembersHits() , mapReport.getTotalBotHits() ,
        mapReport.getTotalMembersBotHits() );

    return bean;
  }

}
