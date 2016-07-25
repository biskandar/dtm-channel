package com.beepcast.channel;

public class ChannelSessionXipmeBeanFactory {

  public static ChannelSessionXipmeBean createChannelSessionXipmeBean(
      int channelSessionId , String xipmeCode , int totalMembers ,
      int totalMembersMembers , int totalCustomers , int totalVisitors ,
      int totalMembersVisitors , int totalBotVisitors ,
      int totalMembersBotVisitors , int totalHits , int totalMembersHits ,
      int totalBotHits , int totalMembersBotHits ) {
    ChannelSessionXipmeBean bean = new ChannelSessionXipmeBean();
    bean.setChannelSessionId( channelSessionId );
    bean.setXipmeCode( xipmeCode );
    bean.setTotalMembers( totalMembers );
    bean.setTotalMembersMembers( totalMembersMembers );
    bean.setTotalCustomers( totalCustomers );
    bean.setTotalVisitors( totalVisitors );
    bean.setTotalMembersVisitors( totalMembersVisitors );
    bean.setTotalBotVisitors( totalBotVisitors );
    bean.setTotalMembersBotVisitors( totalMembersBotVisitors );
    bean.setTotalHits( totalHits );
    bean.setTotalMembersHits( totalMembersHits );
    bean.setTotalBotHits( totalBotHits );
    bean.setTotalMembersBotHits( totalMembersBotHits );
    return bean;
  }

}
