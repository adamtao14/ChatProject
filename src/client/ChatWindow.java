package client;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import utils.AsymmetricEncryptionUtil;
import utils.KeyExchange;
import utils.MessageEncryption;
import utils.SerializableMessage;

public class ChatWindow {
	ChatClient client;
	String friend;
	String currentUser;
	PrivateKey privateKey;
	PublicKey friendPublicKey;
	SecretKey symmetricKey;
	boolean canSendMessage;
	public ChatWindow(ChatClient client,String friendName, String currentUser) {
		this.client = client;
		this.friend = friendName;
		this.currentUser = currentUser;
		this.canSendMessage = false;
		
		try {
			this.privateKey = AsymmetricEncryptionUtil.readPrivateKey(currentUser);
			this.friendPublicKey = AsymmetricEncryptionUtil.stringToPublicKey(this.client.getUserPublicKey(friendName));
			String[] values = this.client.checkIfChatHasEncryptionKey(this.currentUser, this.friend);
			if(values != null) {
				if(values[3] == null) {
					if(!values[4].equals(this.currentUser)) {						
						// Second user of chat has yet to add his encryption of the symmetric key with first user's pub key
						SecretKey decryptedSymmetricKey = KeyExchange.decryptSymmetricKey(values[2], privateKey);
						String encryptedSymmetricKey = KeyExchange.encryptSymmetricKey(decryptedSymmetricKey, friendPublicKey);
						this.client.addChatSymmetricKeySecondSigner(friendName, currentUser, encryptedSymmetricKey);
					}else {
						showAlert(Alert.AlertType.INFORMATION, "Chat", "You can start sending messages only after "+ friendName + " confirms the encryption key!");
						return;
					}
				}else {
					this.canSendMessage = true;
					if(values[4].equals(currentUser)) {
						//read second key
						symmetricKey = KeyExchange.decryptSymmetricKey(values[3], privateKey);
					}else {
						//read first key
						symmetricKey = KeyExchange.decryptSymmetricKey(values[2], privateKey);
					}
				}
			}else {
				// create key as first signer
				this.symmetricKey = KeyExchange.generateSymmetricKey();
				String encryptedSymmetricKey = KeyExchange.encryptSymmetricKey(symmetricKey, friendPublicKey);
				if(this.client.addChatSymmetricKeyFirstSigner(this.currentUser, friend, encryptedSymmetricKey)) {
					showAlert(Alert.AlertType.INFORMATION, "Chat", "You can start sending messages only after "+ friendName + "confirms the encryption key!");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
        Stage chatStage = new Stage();
        chatStage.setTitle("Chat with " + friendName);
        chatStage.getIcons().add(new Image("file:C:/Users/adamt/eclipse-workspace/ChatProject/src/resources/logo.png"));
        BorderPane root = new BorderPane();

        // Top: Messages Area
        TextFlow messagesArea = new TextFlow();
        messagesArea.setPrefHeight(400);
        loadLastMessages(friendName, messagesArea);
        root.setCenter(messagesArea);

        // Bottom: Input and Send Button
        HBox inputBox = new HBox(10);
        inputBox.setPadding(new Insets(10));
        inputBox.setAlignment(Pos.CENTER);

        TextField messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setPrefWidth(600);
        messageField.setMaxWidth(2000);
        
        Button sendButton = new Button("Send");
        
        Runnable sendMessageAction = () -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
            	if(message.length() < 2000) {            		
            		try {
            			IvParameterSpec iv = MessageEncryption.generateIv();
            			String encryptedMessage = MessageEncryption.encryptMessage(message, symmetricKey, iv);
            			if (client.sendMessage(friendName, encryptedMessage, MessageEncryption.ivToString(iv))) {
            				String formattedSender = String.format("[%s-%s] ", this.currentUser, getCurrentTime());
            				Text senderText = new Text(formattedSender);
            				senderText.setFill(Color.BLUE);
            				Text messageText = new Text(message + "\n");
            				messagesArea.getChildren().addAll(senderText, messageText);
            			} else {
            				showAlert(Alert.AlertType.ERROR, "Message", "Failed to send the message");
            			}
            			messageField.clear();
            		} catch (Exception e) {
            			// TODO Auto-generated catch block
            			e.printStackTrace();
            		}
            	}else {
            		showAlert(Alert.AlertType.ERROR, "Message", "Message cannot be more than 2000 characters");
            		messageField.clear();
            	}
            }
        };
        if(canSendMessage) {        	
        	messageField.setOnKeyPressed(event -> {
        		if (event.getCode() == KeyCode.ENTER) {
        			sendMessageAction.run();
        			event.consume();
        		}
        	});
        	
        	sendButton.setOnAction(e -> sendMessageAction.run());
        }

        inputBox.getChildren().addAll(messageField, sendButton);
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 800, 600);
        chatStage.setScene(scene);
        chatStage.show();
        
        MessageListener messageListener = new MessageListener(friendName, client, messagesArea, symmetricKey);
        messageListener.startListening();
    }

    private void loadLastMessages(String friendName, TextFlow messagesArea) {

        List<SerializableMessage> messages = client.getMessages(friend);
        if (messages != null && !messages.isEmpty()) {
            for (SerializableMessage message : messages) {
            	String formattedSender = String.format("[%s-%s] ", message.getSender(), message.getTime());
                Text senderText = new Text(formattedSender);
                senderText.setFill(Color.valueOf(message.getSenderStyle()));
                try {
                	String decryptedMessage = MessageEncryption.decryptMessage(message.getMessage(), symmetricKey, MessageEncryption.stringToIV(message.getIv()));
                	Text messageText = new Text(decryptedMessage + "\n");                	
                	messagesArea.getChildren().addAll(senderText, messageText);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        
    }
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
