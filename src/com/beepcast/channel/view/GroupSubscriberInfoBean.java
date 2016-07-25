package com.beepcast.channel.view;

import com.firsthop.common.log.DLogContext;
import com.firsthop.common.log.SimpleContext;

public class GroupSubscriberInfoBean {

  static final DLogContext lctx = new SimpleContext( "GroupSubscriberInfoBean" );

  private int groupSubscriberId;
  private String groupSubscriberName;

  public GroupSubscriberInfoBean() {
  }

  public int getGroupSubscriberId() {
    return groupSubscriberId;
  }

  public void setGroupSubscriberId( int groupSubscriberId ) {
    this.groupSubscriberId = groupSubscriberId;
  }

  public String getGroupSubscriberName() {
    return groupSubscriberName;
  }

  public void setGroupSubscriberName( String groupSubscriberName ) {
    this.groupSubscriberName = groupSubscriberName;
  }

}
