package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionScheduleBeanFactory {

  public static ChannelSessionScheduleBean createChannelSessionScheduleBean(
      int channelSessionId , Date dateStarted , Date dateStopped , String marked ) {
    ChannelSessionScheduleBean bean = new ChannelSessionScheduleBean();
    bean.setChannelSessionId( channelSessionId );
    bean.setDateStarted( dateStarted );
    bean.setDateStopped( dateStopped );
    bean.setMarked( marked );
    bean.setActive( true );
    return bean;
  }

}
