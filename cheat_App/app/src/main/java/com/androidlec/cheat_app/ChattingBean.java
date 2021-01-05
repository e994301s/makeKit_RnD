package com.androidlec.cheat_app;

public class ChattingBean {

    String sendid;
    String sendContents;
    int chattingNo;

    public ChattingBean(String sendid, String sendContents, int chattingNo) {
        this.sendid = sendid;
        this.sendContents = sendContents;
        this.chattingNo = chattingNo;
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

    public int getChattingNo() {
        return chattingNo;
    }

    public void setChattingNo(int chattingNo) {
        this.chattingNo = chattingNo;
    }
}
