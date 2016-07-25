package com.beepcast.channel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChannelManagementReportFileClass {

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public ChannelManagementReportFileClass() {
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Support Function
  //
  // ////////////////////////////////////////////////////////////////////////////

  public XipmeLink newXipmeLink( String code ) {
    return new XipmeLink( code );
  }

  public RecordGateway newRecordGateway() {
    return new RecordGateway();
  }

  public RecordSubscriber newRecordSubscriber() {
    return new RecordSubscriber();
  }

  public RecordChannelSessionSubscriber newRecordChannelSessionSubscriber() {
    return new RecordChannelSessionSubscriber();
  }

  public RecordGateway cloneRecordGateway( RecordGateway recordGateway ) {
    RecordGateway result = null;
    if ( recordGateway == null ) {
      return result;
    }
    try {
      result = (RecordGateway) recordGateway.clone();
    } catch ( CloneNotSupportedException e ) {
    }
    return result;
  }

  public RecordSubscriber cloneRecordSubscriber(
      RecordSubscriber recordSubscriber ) {
    RecordSubscriber result = null;
    if ( recordSubscriber == null ) {
      return result;
    }
    try {
      result = (RecordSubscriber) recordSubscriber.clone();
    } catch ( CloneNotSupportedException e ) {
    }
    return result;
  }

  public RecordChannelSessionSubscriber cloneRecordChannelSessionSubscriber(
      RecordChannelSessionSubscriber recordChannelSessionSubscriber ) {
    RecordChannelSessionSubscriber result = null;
    if ( recordChannelSessionSubscriber == null ) {
      return result;
    }
    try {
      result = (RecordChannelSessionSubscriber) recordChannelSessionSubscriber
          .clone();
    } catch ( CloneNotSupportedException e ) {
    }
    return result;
  }

  // ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Class
  //
  // ////////////////////////////////////////////////////////////////////////////

  public abstract class RecordBase {

    private String phone;

    public RecordBase() {
    }

    public String getPhone() {
      return phone;
    }

    public void setPhone( String phone ) {
      this.phone = phone;
    }

  }

  public class XipmeLink {

    private String code;
    private int hit;

    public XipmeLink( String code ) {
      this.code = code;
    }

    public String getCode() {
      return code;
    }

    public int getHit() {
      return hit;
    }

    public void setHit( int hit ) {
      this.hit = hit;
    }

  }

  public class RecordGateway extends RecordBase implements Cloneable {

    private String status;
    private String messageId;
    private int retry;
    private Date submitDate;
    private Date statusDate;
    private double debitAmount;
    private int messageCount;
    private String messageContent;

    private Map mapXipmeLinks;

    public RecordGateway() {
      super();
      mapXipmeLinks = new HashMap();
    }

    public String getStatus() {
      return status;
    }

    public void setStatus( String status ) {
      this.status = status;
    }

    public String getMessageId() {
      return messageId;
    }

    public void setMessageId( String messageId ) {
      this.messageId = messageId;
    }

    public int getRetry() {
      return retry;
    }

    public void setRetry( int retry ) {
      this.retry = retry;
    }

    public Date getSubmitDate() {
      return submitDate;
    }

    public void setSubmitDate( Date submitDate ) {
      this.submitDate = submitDate;
    }

    public Date getStatusDate() {
      return statusDate;
    }

    public void setStatusDate( Date statusDate ) {
      this.statusDate = statusDate;
    }

    public double getDebitAmount() {
      return debitAmount;
    }

    public void setDebitAmount( double debitAmount ) {
      this.debitAmount = debitAmount;
    }

    public int getMessageCount() {
      return messageCount;
    }

    public void setMessageCount( int messageCount ) {
      this.messageCount = messageCount;
    }

    public String getMessageContent() {
      return messageContent;
    }

    public void setMessageContent( String messageContent ) {
      this.messageContent = messageContent;
    }

    public Map mapXipmeLinks() {
      return mapXipmeLinks;
    }

    public Object clone() throws CloneNotSupportedException {
      RecordGateway recordGateway = (RecordGateway) super.clone();
      return recordGateway;
    }

  }

  public class RecordSubscriber extends RecordBase implements Cloneable {

    private boolean subscribed;
    private int subscribedEventId;
    private Date subscribedDate;
    private boolean globalSubscribed;
    private Date globalSubscribedDate;
    private String custRefId;
    private String custRefCode;
    private String description;

    public RecordSubscriber() {
      super();
    }

    public boolean isSubscribed() {
      return subscribed;
    }

    public void setSubscribed( boolean subscribed ) {
      this.subscribed = subscribed;
    }

    public int getSubscribedEventId() {
      return subscribedEventId;
    }

    public void setSubscribedEventId( int subscribedEventId ) {
      this.subscribedEventId = subscribedEventId;
    }

    public Date getSubscribedDate() {
      return subscribedDate;
    }

    public void setSubscribedDate( Date subscribedDate ) {
      this.subscribedDate = subscribedDate;
    }

    public boolean isGlobalSubscribed() {
      return globalSubscribed;
    }

    public void setGlobalSubscribed( boolean globalSubscribed ) {
      this.globalSubscribed = globalSubscribed;
    }

    public Date getGlobalSubscribedDate() {
      return globalSubscribedDate;
    }

    public void setGlobalSubscribedDate( Date globalSubscribedDate ) {
      this.globalSubscribedDate = globalSubscribedDate;
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

    public Object clone() throws CloneNotSupportedException {
      RecordSubscriber recordSubscriber = (RecordSubscriber) super.clone();
      return recordSubscriber;
    }

  }

  public class RecordChannelSessionSubscriber extends RecordBase implements
      Cloneable {

    public RecordChannelSessionSubscriber() {
      super();
    }

    public Object clone() throws CloneNotSupportedException {
      RecordChannelSessionSubscriber recordChannelSessionSubscriber = (RecordChannelSessionSubscriber) super
          .clone();
      return recordChannelSessionSubscriber;
    }

  }

}
