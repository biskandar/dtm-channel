package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionBeanFactory {

  public static ChannelSessionBean createChannelSessionBean( int eventId ,
      int groupSubscriberId , boolean allowDuplicated , boolean useDateSend ,
      double expectResponse , long totalNumbers , long totalRemains ,
      double debitUnits , int priority , boolean suspended , boolean started ,
      boolean finished , int newStatus , Date dateStarted , Date dateSuspended ,
      Date dateFinished , Date dateSchedulledStart , Date dateSchedulledFinish ) {
    ChannelSessionBean bean = new ChannelSessionBean();
    bean.setEventId( eventId );
    bean.setGroupSubscriberId( groupSubscriberId );
    bean.setAllowDuplicated( allowDuplicated );
    bean.setUseDateSend( useDateSend );
    bean.setExpectResponse( expectResponse );
    bean.setTotalNumbers( totalNumbers );
    bean.setTotalRemains( totalRemains );
    bean.setDebitUnits( debitUnits );
    bean.setPriority( priority );
    bean.setSuspended( suspended );
    bean.setStarted( started );
    bean.setFinished( finished );
    bean.setNewStatus( newStatus );
    bean.setDateStarted( dateStarted );
    bean.setDateSuspended( dateSuspended );
    bean.setDateFinished( dateFinished );
    bean.setDateSchedulledStart( dateSchedulledStart );
    bean.setDateSchedulledFinish( dateSchedulledFinish );
    bean.setActive( true );
    return bean;
  }

}
