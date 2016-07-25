package com.beepcast.channel;

public class ChannelSessionSubscriberBean {

  private int id;
  private int channelSessionId;
  private String type;
  private String phone;

  public ChannelSessionSubscriberBean() {

  }

  public int getId() {
    return id;
  }

  public void setId( int id ) {
    this.id = id;
  }

  public int getChannelSessionId() {
    return channelSessionId;
  }

  public void setChannelSessionId( int channelSessionId ) {
    this.channelSessionId = channelSessionId;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone( String phone ) {
    this.phone = phone;
  }

}
