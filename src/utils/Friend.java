package utils;

import javafx.beans.property.SimpleStringProperty;

public class Friend {
    private final SimpleStringProperty username;
    private final boolean status;

    public Friend(String username, Boolean status) {
        this.username = new SimpleStringProperty(username);
        this.status = status;
    }

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getStatus() {
        return this.status ? "Online" : "Offline";
    }

}