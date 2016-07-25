package com.beepcast.channel;

import java.util.Date;

public class ChannelSessionXipmeBean {

  private int id;
  private int channelSessionId;
  private String xipmeCode;
  private int totalMembers;
  private int totalMembersMembers;
  private int totalCustomers;
  private int totalVisitors;
  private int totalMembersVisitors;
  private int totalBotVisitors;
  private int totalMembersBotVisitors;
  private int totalHits;
  private int totalMembersHits;
  private int totalBotHits;
  private int totalMembersBotHits;

  private Date dateInserted;
  private Date dateUpdated;

  public ChannelSessionXipmeBean() {

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

  public String getXipmeCode() {
    return xipmeCode;
  }

  public void setXipmeCode( String xipmeCode ) {
    this.xipmeCode = xipmeCode;
  }

  public int getTotalMembers() {
    return totalMembers;
  }

  public void setTotalMembers( int totalMembers ) {
    this.totalMembers = totalMembers;
  }

  public int getTotalMembersMembers() {
    return totalMembersMembers;
  }

  public void setTotalMembersMembers( int totalMembersMembers ) {
    this.totalMembersMembers = totalMembersMembers;
  }

  public int getTotalCustomers() {
    return totalCustomers;
  }

  public void setTotalCustomers( int totalCustomers ) {
    this.totalCustomers = totalCustomers;
  }

  public int getTotalVisitors() {
    return totalVisitors;
  }

  public void setTotalVisitors( int totalVisitors ) {
    this.totalVisitors = totalVisitors;
  }

  public int getTotalMembersVisitors() {
    return totalMembersVisitors;
  }

  public void setTotalMembersVisitors( int totalMembersVisitors ) {
    this.totalMembersVisitors = totalMembersVisitors;
  }

  public int getTotalBotVisitors() {
    return totalBotVisitors;
  }

  public void setTotalBotVisitors( int totalBotVisitors ) {
    this.totalBotVisitors = totalBotVisitors;
  }

  public int getTotalMembersBotVisitors() {
    return totalMembersBotVisitors;
  }

  public void setTotalMembersBotVisitors( int totalMembersBotVisitors ) {
    this.totalMembersBotVisitors = totalMembersBotVisitors;
  }

  public int getTotalHits() {
    return totalHits;
  }

  public void setTotalHits( int totalHits ) {
    this.totalHits = totalHits;
  }

  public int getTotalMembersHits() {
    return totalMembersHits;
  }

  public void setTotalMembersHits( int totalMembersHits ) {
    this.totalMembersHits = totalMembersHits;
  }

  public int getTotalBotHits() {
    return totalBotHits;
  }

  public void setTotalBotHits( int totalBotHits ) {
    this.totalBotHits = totalBotHits;
  }

  public int getTotalMembersBotHits() {
    return totalMembersBotHits;
  }

  public void setTotalMembersBotHits( int totalMembersBotHits ) {
    this.totalMembersBotHits = totalMembersBotHits;
  }

  public Date getDateInserted() {
    return dateInserted;
  }

  public void setDateInserted( Date dateInserted ) {
    this.dateInserted = dateInserted;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated( Date dateUpdated ) {
    this.dateUpdated = dateUpdated;
  }

  public String toString() {
    final String TAB = " ";
    String retValue = "";
    retValue = "ChannelSessionXipmeBean ( " + "id = " + this.id + TAB
        + "channelSessionId = " + this.channelSessionId + TAB + "xipmeCode = "
        + this.xipmeCode + TAB + "totalMembers = " + this.totalMembers + TAB
        + "totalMembersMembers = " + this.totalMembersMembers + TAB
        + "totalCustomers = " + this.totalCustomers + TAB + "totalVisitors = "
        + this.totalVisitors + TAB + "totalMembersVisitors = "
        + this.totalMembersVisitors + TAB + "totalBotVisitors = "
        + this.totalBotVisitors + TAB + "totalMembersBotVisitors = "
        + this.totalMembersBotVisitors + TAB + "totalHits = " + this.totalHits
        + TAB + "totalMembersHits = " + this.totalMembersHits + TAB
        + "totalBotHits = " + this.totalBotHits + TAB
        + "totalMembersBotHits = " + this.totalMembersBotHits + TAB + " )";
    return retValue;
  }

}
