package utils;

import java.io.Serializable;

public class SerializableMessage implements Serializable {
    private String sender;
    private String message;
    private String time;
    private String senderStyle;  // e.g., "BLUE" or "GREEN"
    private String iv;

    public SerializableMessage(String sender, String message, String time, String senderStyle, String iv) {
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.senderStyle = senderStyle;
        this.iv = iv;
    }

    // Getters and setters
    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getSenderStyle() {
        return senderStyle;
    }
    
    public String getIv() {
        return iv;
    }
}
