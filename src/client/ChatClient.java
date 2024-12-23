// File: client/ChatClient.java
package client;

import shared.ChatServerInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.util.List;

import utils.PasswordUtils;
import utils.SerializableMessage;
import utils.AuthenticationStatus;
import utils.AsymmetricEncryptionUtil;

public class ChatClient {
    private ChatServerInterface server;
    private String loggedInUser;
    

    // Constructor: Connect to the RMI server
    public ChatClient(String serverHost, int serverPort) {
        try {
            Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
            this.server = (ChatServerInterface) registry.lookup("ChatServer");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the server.");
        }
    }
    // Add the encrypted symmetric key of first user
    public boolean addChatSymmetricKeyFirstSigner(String first_signer, String second_signer, String encrypted_symmetric_key) {
    	try {
            return server.addChatSymmetricKeyFirstSigner(first_signer, second_signer, encrypted_symmetric_key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Add the encrypted symmetric key of second user
    public boolean addChatSymmetricKeySecondSigner(String first_signer, String second_signer, String encrypted_symmetric_key) {
    	try {
            return server.addChatSymmetricKeySecondSigner(first_signer, second_signer, encrypted_symmetric_key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // Check if the chat already has an encryption key
    public String[] checkIfChatHasEncryptionKey(String first_signer, String second_signer) {
    	try {
            return server.checkIfChatHasEncryptionKey(first_signer, second_signer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Get user's public key
    public String getUserPublicKey(String username) {
    	try {
            return server.getUserPublicKey(username);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Register user
    public AuthenticationStatus registerUser(String username, String password) {
        try {
            if(password.isEmpty() || username.isEmpty()) {
            	return AuthenticationStatus.EMPTY_CREDENTIALS;
            }else if(password.length() < 8) {
            	return AuthenticationStatus.PASSWORD_TOO_SHORT;
            }else if(username.contains("-") || username.contains(",") || username.contains(".") || username.contains(";") || username.contains("'") || username.contains("\"")) {
            	return AuthenticationStatus.INVALID_USERNAME;
            }
            List<String> username_lookup = server.lookupUsers(username);
            if(username_lookup.size() > 0) {
            	return AuthenticationStatus.USERNAME_ALREADY_TAKEN;
            }
            PasswordUtils.HashResult hashResult = PasswordUtils.hashPassword(password);
            if(server.registerUser(username, hashResult.getHash(), hashResult.getSalt())) {
            	KeyPair keys = AsymmetricEncryptionUtil.generateKeyPair();
            	if(keys != null) {
            		if(server.addUserPublicKey(username, AsymmetricEncryptionUtil.publicKeyToString(keys.getPublic()))) {
            			AsymmetricEncryptionUtil.savePrivateKey(username, keys.getPrivate());
            			return AuthenticationStatus.SUCCESS;
            		}
            	}
            }
            return AuthenticationStatus.FAILED;
        } catch (Exception e) {
            e.printStackTrace();
            return AuthenticationStatus.GENERAL_ERROR;
        }
    }

    // Login user
    public AuthenticationStatus loginUser(String username, String password) {
        try {
        	if(password.isEmpty() || username.isEmpty()) {
            	return AuthenticationStatus.EMPTY_CREDENTIALS;
            }
        	List<String> username_lookup = server.lookupUsers(username);
            if(username_lookup.size() > 0) {
            	String[] user_hash_and_salt = server.getUserHashAndSalt(username);
                if(PasswordUtils.verifyPassword(password, user_hash_and_salt[0], user_hash_and_salt[1])) {
                	if(server.loginUser(username)) {
                		loggedInUser=username;
                		return AuthenticationStatus.SUCCESS;
                	}
                }
            }
            return AuthenticationStatus.FAILED;
  
        } catch (Exception e) {
            e.printStackTrace();
            return AuthenticationStatus.GENERAL_ERROR;
        }
    }
    
    // Logout user
    public AuthenticationStatus logoutUser() {
        try {
        	if(server.logoutUser(loggedInUser)) {
        		loggedInUser="";
        		return AuthenticationStatus.SUCCESS;
        	}
            return AuthenticationStatus.FAILED;
  
        } catch (Exception e) {
            e.printStackTrace();
            return AuthenticationStatus.GENERAL_ERROR;
        }
    }

    // Send a friend request
    public boolean sendFriendRequest(String receiverUsername) {
        try {
            return server.sendFriendRequest(loggedInUser, receiverUsername);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Remove friend request
    public boolean removeFriendRequest(String sender_username) {
        try {
            return server.removeFriendRequest(sender_username, loggedInUser);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
 // Check if friend request already exists
    public boolean checkIfFriendRequestExists(String receiverUsername) {
        try {
            return server.checkIfFriendRequestExists(loggedInUser, receiverUsername);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Accept a friend request
    public boolean addFriend(String user2_username) {
        try {
            return server.addFriend(loggedInUser, user2_username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Remove a friend request
    public boolean removeFriend(String user2_username) {
        try {
            return server.removeFriend(loggedInUser, user2_username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get user's status
    public boolean getUserStatus(String username) {
    	try {
            return server.getUserStatus(username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // Fetch friends list
    public List<String> getFriends() {
        try {
            return server.getFriends(loggedInUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Fetch friends list
    public List<String> getFriendRequests() {
        try {
            return server.getUserFriendRequests(loggedInUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lookup users by search query
    public List<String> lookupUsers(String searchQuery) {
        try {
            return server.lookupUsers(searchQuery);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Lookup users by search query
    public List<String> lookupUsersSearch(String searchQuery) {
        try {
            return server.lookUpUsersSearch(searchQuery,loggedInUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Send a message
    public boolean sendMessage(String receiver, String messageText, String iv) {
        try {
            return server.sendMessage(loggedInUser, receiver, messageText, iv);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch messages between logged-in user and another user
    public List<SerializableMessage> getMessages(String withUser) {
        try {
            return server.getMessages(loggedInUser, withUser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Fetch newer messages between logged-in user and another user
    public List<SerializableMessage> checkForNewMessages(String withUser, Timestamp lastMessageTime) {
        try {
            return server.checkForNewMessages(loggedInUser, withUser, lastMessageTime);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Main function for quick testing (without GUI)
    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost", 1099);
    }
}
