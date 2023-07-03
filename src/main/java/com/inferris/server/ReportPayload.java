package com.inferris.server;

import java.util.UUID;

public class ReportPayload {
    private String sender;
    private String reported;
    private String reason;
    private String server;
    public ReportPayload(String sender, String reported, String reason, String server){
        this.sender = sender;
        this.reported = reported;
        this.reason = reason;
        this.server = server;
    }

    public ReportPayload(){

    }

    public String getSender() {
        return sender;
    }

    public String getReported() {
        return reported;
    }

    public String getReason() {
        return reason;
    }

    public String getServer() {
        return server;
    }
}
