// File: shared/ChatServerInterface.java
package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.List;

import javafx.scene.text.Text;
import utils.SerializableMessage;

public interface ChatServerInterface extends Remote {
	
	// Check if current chat has symmetric keys
	String[] checkIfChatHasEncryptionKey(String first_user, String second_user) throws RemoteException;
	
	// Add first signer encrypted key
    boolean addChatSymmetricKeyFirstSigner(String first_user, String second_user, String encrypted_symmetric_key) throws RemoteException;
    
    // Add second signer encrypted key
    public boolean addChatSymmetricKeySecondSigner(String first_user, String second_user, String encrypted_symmetric_key) throws RemoteException;
    
    // Register a new user
    boolean registerUser(String username, String passwordHash, String passwordSalt) throws RemoteException;
    
    // Get user's public key
    String getUserPublicKey(String username) throws RemoteException;
    
    // Add user's public key
    boolean addUserPublicKey(String username, String public_key) throws RemoteException;
    
    // Login a user
    boolean loginUser(String username) throws RemoteException;
    
    // Logout a user
    boolean logoutUser(String username) throws RemoteException;

    // Send a friend request
    boolean sendFriendRequest(String senderUsername, String receiverUsername) throws RemoteException;
    
    // Check if friend request is already sent
    boolean checkIfFriendRequestExists(String senderUsername, String receiverUsername) throws RemoteException;
    
    // Add a friend
    boolean addFriend(String user1_username, String user2_username) throws RemoteException;
    
    //Remove a friend
    boolean removeFriend(String user1_username, String user2_username) throws RemoteException;
    
    // Remove a friend request
    boolean removeFriendRequest(String senderUsername, String receiverUsername) throws RemoteException;
    
    // Get user's status
    boolean getUserStatus(String username) throws RemoteException;
    
    // Get user's pending friend requests
    List<String> getUserFriendRequests(String username) throws RemoteException;
    
    // Get a list of friends for a user
    List<String> getFriends(String username) throws RemoteException;

    // Search for users by username
    List<String> lookupUsers(String searchQuery) throws RemoteException;
    
    // Search for users by query that are not the current user or in the user's friends
    List<String> lookUpUsersSearch(String input, String currentUser) throws RemoteException;

    // Send a message to another user
    boolean sendMessage(String sender, String receiver, String messageText, String iv) throws RemoteException;

    // Fetch messages between two users
    List<SerializableMessage> getMessages(String user1, String user2) throws RemoteException;
    
    // Get newest messages form a chat
    List<SerializableMessage> checkForNewMessages(String loggedInUser, String friendName, Timestamp lastMessageTime) throws RemoteException;
    
    // Get the user's hash and salt
    String[] getUserHashAndSalt(String username) throws RemoteException;
}
