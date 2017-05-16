package com.gilliswerrebrouck.messagingapp.model;

/**
 * Created by gillis on 16/05/2017.
 */

public class Notification {
    //region properties
    private String uid;
    private String topic;
    private String sender;
    private String message;
    private String messageKey;
    //endregion

    //region constructor(s)
    public Notification() {
    }

    public Notification(String uid, String topic, String sender, String message, String messageKey) {
        this.uid = uid;
        this.topic = topic;
        this.sender = sender;
        this.message = message;
        this.messageKey = messageKey;
    }
    //endregion

    //region getter(s)
    public String getUid() {
        return uid;
    }

    public String getTopic() {
        return topic;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageKey() {
        return messageKey;
    }
    //endregion

    //region setter(s)
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    //endregion
}
