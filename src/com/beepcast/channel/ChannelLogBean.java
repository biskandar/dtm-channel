package com.beepcast.channel;

import java.util.Date;

public class ChannelLogBean {

  private int id;
  private int marked;
  private String messageId;
  private String phone;
  private int eventId;
  private int subscribed;
  private int clientSubscriberId;
  private int clientSubscriberCustomId;
  private String custRefId;
  private String custRefCode;
  private String description;
  private String countryCode;
  private double countryCreditCost;
  private Date dateTm;
  private Date dateSend;
  private Date dateSubscribed;
  private Date dateMarked;
  private Date dateUnmarked;

  public ChannelLogBean() {

  }

  public int getId() {
    return id;
  }

  public void setId( int id ) {
    this.id = id;
  }

  public int getMarked() {
    return marked;
  }

  public void setMarked( int marked ) {
    this.marked = marked;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId( String messageId ) {
    this.messageId = messageId;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone( String phone ) {
    this.phone = phone;
  }

  public int getEventId() {
    return eventId;
  }

  public void setEventId( int eventId ) {
    this.eventId = eventId;
  }

  public int getSubscribed() {
    return subscribed;
  }

  public void setSubscribed( int subscribed ) {
    this.subscribed = subscribed;
  }

  public int getClientSubscriberId() {
    return clientSubscriberId;
  }

  public void setClientSubscriberId( int clientSubscriberId ) {
    this.clientSubscriberId = clientSubscriberId;
  }

  public int getClientSubscriberCustomId() {
    return clientSubscriberCustomId;
  }

  public void setClientSubscriberCustomId( int clientSubscriberCustomId ) {
    this.clientSubscriberCustomId = clientSubscriberCustomId;
  }

  public String getCustRefId() {
    return custRefId;
  }

  public void setCustRefId( String custRefId ) {
    this.custRefId = custRefId;
  }

  public String getCustRefCode() {
    return custRefCode;
  }

  public void setCustRefCode( String custRefCode ) {
    this.custRefCode = custRefCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode( String countryCode ) {
    this.countryCode = countryCode;
  }

  public double getCountryCreditCost() {
    return countryCreditCost;
  }

  public void setCountryCreditCost( double countryCreditCost ) {
    this.countryCreditCost = countryCreditCost;
  }

  public Date getDateTm() {
    return dateTm;
  }

  public void setDateTm( Date dateTm ) {
    this.dateTm = dateTm;
  }

  public Date getDateSend() {
    return dateSend;
  }

  public void setDateSend( Date dateSend ) {
    this.dateSend = dateSend;
  }

  public Date getDateSubscribed() {
    return dateSubscribed;
  }

  public void setDateSubscribed( Date dateSubscribed ) {
    this.dateSubscribed = dateSubscribed;
  }

  public Date getDateMarked() {
    return dateMarked;
  }

  public void setDateMarked( Date dateMarked ) {
    this.dateMarked = dateMarked;
  }

  public Date getDateUnmarked() {
    return dateUnmarked;
  }

  public void setDateUnmarked( Date dateUnmarked ) {
    this.dateUnmarked = dateUnmarked;
  }

  public String toString() {
    return "ChannelLogBean [id=" + id + ", marked=" + marked + ", messageId="
        + messageId + ", phone=" + phone + ", eventId=" + eventId
        + ", subscribed=" + subscribed + ", clientSubscriberId="
        + clientSubscriberId + ", clientSubscriberCustomId="
        + clientSubscriberCustomId + ", countryCode=" + countryCode
        + ", countryCreditCost=" + countryCreditCost + "]";
  }

}
