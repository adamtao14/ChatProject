// File: server/ChatServer.java
package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;
// RMI Interface Definition
import shared.ChatServerInterface;
import utils.SerializableMessage;

public class ChatServer extends UnicastRemoteObject implements ChatServerInterface {

    private static final long serialVersionUID = 1L;
    private Connection dbConnection;

    // Constructor to set up database connection
    protected ChatServer() throws RemoteException, SQLException {
        super();
        this.dbConnection = DatabaseUtils.getConnection();
    }
    
    // Get user's public key
    @Override
    public String getUserPublicKey(String username) throws RemoteException{
    	String query = "SELECT public_key FROM public_keys WHERE username=?";
    	String pub_key = "";
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            	pub_key = rs.getString("public_key");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return pub_key;
    }
    
    // Add user's public key
    @Override
    public boolean addUserPublicKey(String username, String public_key) throws RemoteException {
    	String query = "INSERT INTO public_keys (username,public_key) VALUES (?, ?)";
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, public_key);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Check if current chat has symmetric keys
    @Override
    public String[] checkIfChatHasEncryptionKey(String first_user, String second_user) throws RemoteException{
    	String query = "SELECT * FROM chat_keys WHERE first_signer=? AND second_signer=? OR first_signer=? AND second_signer=?";
    	String[] values = null;
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, first_user);
            stmt.setString(2, second_user);
            stmt.setString(3, second_user);
            stmt.setString(4, first_user);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
            	values = new String[5];
            	values[0] = rs.getString("first_signer");
            	values[1] = rs.getString("second_signer");
            	values[2] = rs.getString("encrypted_key_first_signer");
            	values[3] = rs.getString("encrypted_key_second_signer");
            	values[4] = rs.getString("first_creator");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
    	return values;
    }
    
    // Add first signer encrypted key
    @Override
    public boolean addChatSymmetricKeyFirstSigner(String first_user, String second_user, String encrypted_symmetric_key) throws RemoteException{
    	String query = "INSERT INTO chat_keys (first_signer, second_signer, first_creator, encrypted_key_first_signer) VALUES (?, ?, ? ,?) ";
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, first_user);
            stmt.setString(2, second_user);
            stmt.setString(3, first_user);
            stmt.setString(4, encrypted_symmetric_key);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Add second signer encrypted key
    @Override
    public boolean addChatSymmetricKeySecondSigner(String first_user, String second_user, String encrypted_symmetric_key) throws RemoteException{
    	String query = "UPDATE chat_keys SET encrypted_key_second_signer = ? WHERE first_signer = ? AND second_signer = ?";
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
    		stmt.setString(1, encrypted_symmetric_key);
    		stmt.setString(2, first_user);
            stmt.setString(3, second_user);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // User registration
    @Override
    public boolean registerUser(String username, String passwordHash, String passwordSalt) throws RemoteException {
        String query = "INSERT INTO users (username, password_hash, password_salt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, passwordSalt);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // User login
    @Override
    public boolean loginUser(String username) throws RemoteException {
        String query = "UPDATE users SET is_online=? WHERE username=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
        	stmt.setBoolean(1, true);
        	stmt.setString(2, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // User logout
    @Override
    public boolean logoutUser(String username) throws RemoteException {
        String query = "UPDATE users SET is_online=? WHERE username=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
        	stmt.setBoolean(1, false);
        	stmt.setString(2, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get user's password hash and salt
    @Override
    public String[] getUserHashAndSalt(String username) throws RemoteException {
    	String query = "SELECT password_hash, password_salt FROM users WHERE username = ?";
    	String[] values = new String[2];
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            	values[0] = rs.getString("password_hash");
            	values[1] = rs.getString("password_salt");
            }
    	} catch (SQLException e) {
            e.printStackTrace();
        }
    	return values;
    }

    // Send a friend request
    @Override
    public boolean sendFriendRequest(String sender, String receiver) throws RemoteException {
        String query = "INSERT INTO friend_requests (sender_username, receiver_username) VALUES (?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Send a friend request
    @Override
    public boolean checkIfFriendRequestExists(String sender, String receiver) throws RemoteException {
        String query = "SELECT COUNT(*) FROM friend_requests WHERE sender_username = ? AND receiver_username = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Accept a friend request
    @Override
    public boolean addFriend(String user1_username, String user2_username) throws RemoteException {
        String query = "INSERT INTO friends (user1_username, user2_username) VALUES (?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, user1_username);
            stmt.setString(2, user2_username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Remove friend
    @Override
    public boolean removeFriend(String user1_username, String user2_username) throws RemoteException {
        String query = "DELETE FROM friends WHERE user1_username=? AND user2_username=? OR user1_username=? AND user2_username=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, user1_username);
            stmt.setString(2, user2_username);
            stmt.setString(3, user2_username);
            stmt.setString(4, user1_username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reject a friend request
    @Override
    public boolean removeFriendRequest(String sender_username, String receiver_username) throws RemoteException {
        String query = "DELETE FROM friend_requests WHERE sender_username=? AND receiver_username=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
        	stmt.setString(1, sender_username);
            stmt.setString(2, receiver_username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get user's pending friend requests
    @Override
    public List<String> getUserFriendRequests(String username) throws RemoteException {
    	List<String> friendRequests = new ArrayList<>();
        String query = "SELECT sender_username FROM friend_requests WHERE receiver_username=?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
        	stmt.setString(1, username);
        	ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String user1 = rs.getString("sender_username");
                friendRequests.add(user1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    // Fetch user's friends list
    @Override
    public List<String> getFriends(String username) throws RemoteException {
        List<String> friends = new ArrayList<>();
        String query = "SELECT user1_username, user2_username FROM friends WHERE user1_username = ? OR user2_username = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String user1 = rs.getString("user1_username");
                String user2 = rs.getString("user2_username");
                friends.add(user1.equals(username) ? user2 : user1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }
    
 // Fetch user's status
    @Override
    public boolean getUserStatus(String username) throws RemoteException {
        String query = "SELECT is_online FROM users WHERE username = ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getBoolean("is_online");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lookup users by username
    @Override
    public List<String> lookupUsers(String searchQuery) throws RemoteException {
        List<String> users = new ArrayList<>();
        String query = "SELECT username FROM users WHERE username LIKE ?";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Send a message
    @Override
    public boolean sendMessage(String sender, String receiver, String messageText, String iv) throws RemoteException {
        String query = "INSERT INTO messages (sender_username, receiver_username, message_text, iv) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, messageText);
            stmt.setString(4, iv);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch messages between two users
    @Override
    public List<SerializableMessage> getMessages(String user1, String user2) throws RemoteException {
        List<SerializableMessage> messages = new ArrayList<SerializableMessage>();
        String query = "SELECT message_text, sender_username, sent_at, iv FROM messages WHERE (sender_username = ? AND receiver_username = ?) OR (sender_username = ? AND receiver_username = ?) ORDER BY sent_at ASC";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	String sender = rs.getString("sender_username");
                String message = rs.getString("message_text");
                String time = rs.getTimestamp("sent_at").toString();
                String senderStyle = (sender.equals(user1)) ? "BLUE" : "GREEN"; // Set style based on sender
                String iv = rs.getString("iv");
                messages.add(new SerializableMessage(sender, message, time, senderStyle, iv));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    @Override
    public List<SerializableMessage> checkForNewMessages(String loggedInUser, String friendName, Timestamp lastMessageTime) throws RemoteException {
        List<SerializableMessage> messages = new ArrayList<SerializableMessage>();
        String lastTimestamp = "";
        String query = "SELECT sender_username, message_text, sent_at, iv FROM messages " +
                "WHERE receiver_username = ? AND sender_username = ? AND sent_at > ? " +
                "ORDER BY sent_at ASC";
        try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
        	stmt.setString(1, loggedInUser);
        	stmt.setString(2, friendName);
        	stmt.setTimestamp(3, lastMessageTime);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	String sender = rs.getString("sender_username");
                String message = rs.getString("message_text");
                String time = rs.getTimestamp("sent_at").toString();
                String senderStyle = (sender.equals(loggedInUser)) ? "BLUE" : "GREEN"; // Set style based on sender
                String iv = rs.getString("iv");
                messages.add(new SerializableMessage(sender, message, time, senderStyle, iv));
                if(rs.isLast()) {
            		lastTimestamp = time; 
            	}
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(lastTimestamp != "") { 
        	// Needed to keep track of time of last read message
        	messages.add(new SerializableMessage("","",lastTimestamp,"", ""));
        }
        return messages;
    }
    
    public List<String> lookUpUsersSearch(String input, String currentUser) throws RemoteException{
    	List<String> users = new ArrayList<>();
    	String query = "SELECT u.username\r\n"
    			+ "FROM users u\r\n"
    			+ "WHERE u.username LIKE CONCAT('%', ? , '%')"
    			+ "  AND u.username != ?"
    			+ "  AND u.username NOT IN (\r\n"
    			+ "    SELECT CASE \r\n"
    			+ "              WHEN f.user1_username = ? THEN f.user2_username\r\n"
    			+ "              ELSE f.user1_username\r\n"
    			+ "           END AS friend_username\r\n"
    			+ "    FROM friends f\r\n"
    			+ "    WHERE f.user1_username = ? OR f.user2_username = ?\r\n"
    			+ "  );";
    	try (PreparedStatement stmt = dbConnection.prepareStatement(query)) {
            stmt.setString(1, input);
            stmt.setString(2, currentUser);
            stmt.setString(3, currentUser);
            stmt.setString(4, currentUser);
            stmt.setString(5, currentUser);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    	return users;
    }
    
    public static void main(String[] args) {
        try {
            // Create and export the ChatServer instance
            ChatServer server = new ChatServer();

            // Bind the server to the RMI registry with a name
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            java.rmi.Naming.rebind("ChatServer", server);

            System.out.println("ChatServer is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
