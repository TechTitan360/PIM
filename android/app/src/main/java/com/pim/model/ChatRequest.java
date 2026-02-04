package com.pim.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request payload sent to the backend.
 */
public class ChatRequest {

    @SerializedName("sender")
    private String sender;

    @SerializedName("message")
    private String message;

    public ChatRequest(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
