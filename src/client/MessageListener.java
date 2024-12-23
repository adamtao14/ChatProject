package client;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import utils.MessageEncryption;
import utils.SerializableMessage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.SecretKey;

public class MessageListener {
    private final String friendName;
    private final ChatClient client;
    private final TextFlow messagesArea;
    private Timestamp lastMessageTime;
    private SecretKey symmetricKey;

    public MessageListener( String friendName,ChatClient client, TextFlow messagesArea, SecretKey symmetricKey) {
        this.friendName = friendName;
        this.client = client;
        this.messagesArea = messagesArea;
        this.lastMessageTime = new Timestamp(System.currentTimeMillis());
        this.symmetricKey = symmetricKey;
    }

    public void startListening() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForNewMessages(friendName);
            }
        }, 0, 2000);
    }

    private void checkForNewMessages(String friendName) {
    		List<SerializableMessage> newMessages = client.checkForNewMessages(friendName, lastMessageTime);
    		
    		if(!newMessages.isEmpty()) {
    			for(int i=0; i < newMessages.size(); i++) {
    				int currentIndex = i;
    				if(i == newMessages.size()-1) {
    					String lastTimeTrim = newMessages.get(currentIndex).getTime();
    					int dotIndex = lastTimeTrim.indexOf('.');
    					if (dotIndex != -1) {
    						lastTimeTrim = lastTimeTrim.substring(0, dotIndex);
    					}
    					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    					LocalDateTime localDateTime = LocalDateTime.parse(lastTimeTrim, formatter);
    					lastMessageTime = Timestamp.valueOf(localDateTime);
    				}else {
    					String formattedSender = String.format("[%s-%s] ", newMessages.get(currentIndex).getSender(), newMessages.get(currentIndex).getTime());
    					Text senderText = new Text(formattedSender);
    	                senderText.setFill(Color.valueOf(newMessages.get(currentIndex).getSenderStyle()));
    	                
    	                String messageIv = newMessages.get(currentIndex).getIv();
    	                try {
							String decryptedMessage = MessageEncryption.decryptMessage(newMessages.get(currentIndex).getMessage(), symmetricKey, MessageEncryption.stringToIV(messageIv));
							Text messageText = new Text(decryptedMessage + "\n");
							Platform.runLater(() -> messagesArea.getChildren().addAll(senderText, messageText));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    				}
    			}
    		}

    }
}

