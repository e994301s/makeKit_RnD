package com.androidlec.cheat_app;

public class ChattingBean {

    String sendid;
    String sendContents;

    public ChattingBean(String sendid, String sendContents) {
        this.sendid = sendid;
        this.sendContents = sendContents;
    }

    public String getSendid() {
        return sendid;
    }

    public void setSendid(String sendid) {
        this.sendid = sendid;
    }

    public String getSendContents() {
        return sendContents;
    }

    public void setSendContents(String sendContents) {
        this.sendContents = sendContents;
    }
}
