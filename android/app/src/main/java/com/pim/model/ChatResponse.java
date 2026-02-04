package com.pim.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response payload from the backend containing the AI-generated reply.
 */
public class ChatResponse {

    @SerializedName("reply")
    private String reply;

    @SerializedName("sender")
    private String sender;

    @SerializedName("originalMessage")
    private String originalMessage;

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }
}
