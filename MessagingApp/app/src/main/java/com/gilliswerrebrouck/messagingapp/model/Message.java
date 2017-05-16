package com.gilliswerrebrouck.messagingapp.model;

/**
 * Created by gillis on 8/05/2017.
 */

public class Message {
    //region properties
    private String uid;
    private String message;
    private String time;
    //endregion

    //region constructor(s)
    public Message(String uid, String message, String time) {
        this.uid = uid;
        this.message = message;
        this.time = time;
    }
    //endregion

    //region getter(s)
    public String getUid() {
        return this.uid;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTime() {
        return this.time;
    }
    //endregion

    //region setter(s)
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTime(String time) {
        this.time = time;
    }
    //endregion
}
