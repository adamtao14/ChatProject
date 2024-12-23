// File: client/ChatClientGUI.java
package client;

import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import utils.AuthenticationStatus;
import utils.Friend;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import client.ChatWindow;

public class ChatClientGUI extends Application {

    private ChatClient client;
    boolean isLoggedIn;
    String loggedInUser;
    TabPane tabPane;
    Tab loginTab;
    Tab friendsTab;
    Tab messagesTab;
    Tab friendRequestsTab;
    private final StringProperty windowTitle = new SimpleStringProperty("Chat client");

    public ChatClientGUI() {
        // Initialize the ChatClient with server connection (customize host/port as needed)
        client = new ChatClient("localhost", 1099);
        isLoggedIn = false;
        loggedInUser = "";
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.titleProperty().bindBidirectional(windowTitle);
        primaryStage.getIcons().add(new Image("file:C:/Users/adamt/eclipse-workspace/ChatProject/src/resources/logo.png")); 
        // Create Tabs for the GUI
        tabPane = new TabPane();

        loginTab = createLoginTab();
        friendsTab = createFriendsTab();
        friendRequestsTab = createFriendRequestsTab();

        tabPane.getTabs().addAll(loginTab);

        // Modern styling
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create the scene
        Scene scene = new Scene(tabPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createLoginTab() {
        Tab tab = new Tab("Login/Register");

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // Login Controls
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button logoutButton = new Button("Logout");
        
        logoutButton.setDisable(true);

        Label loginStatus = new Label();

        // Button Actions
        loginButton.setOnAction(e -> {
            AuthenticationStatus result = client.loginUser(usernameField.getText(), passwordField.getText());
            switch(result) {
            case AuthenticationStatus.SUCCESS:
               loginStatus.setText("Login successful");
               isLoggedIn=true;
               loggedInUser = usernameField.getText();
               windowTitle.set(loggedInUser);
               loginButton.setDisable(true);
               registerButton.setDisable(true);
               logoutButton.setDisable(false);
               usernameField.setDisable(true);
           	   passwordField.setDisable(true);
               tabPane.getTabs().addAll(friendsTab, friendRequestsTab);
               break;
            case AuthenticationStatus.EMPTY_CREDENTIALS:
            	loginStatus.setText("Credentials cannot be empty");
            	break;
            default:
            	loginStatus.setText("Login failed"); 
            }
            usernameField.clear();
        	passwordField.clear();
        	
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> loginStatus.setText(""));
            pause.play();
        });

        registerButton.setOnAction(e -> {
        	AuthenticationStatus result = client.registerUser(usernameField.getText(), passwordField.getText());
        	switch(result) {
            case AuthenticationStatus.SUCCESS:
            	loginStatus.setText("Registration successful");
              	break;
            case AuthenticationStatus.EMPTY_CREDENTIALS:
            	loginStatus.setText("Credentials cannot be empty");
            	break;
            case AuthenticationStatus.PASSWORD_TOO_SHORT:
            	loginStatus.setText("Password must be atleast 8 charachters");
            	break;
            case AuthenticationStatus.USERNAME_ALREADY_TAKEN:
            	loginStatus.setText("Username already taken");
            	break;
            case AuthenticationStatus.INVALID_USERNAME:
            	loginStatus.setText("Username cannot contain ,.-;'\"");
              	break;
            default:
            	loginStatus.setText("Registration failed"); 
        	}
        	usernameField.clear();
        	passwordField.clear();
        	PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> loginStatus.setText(""));
            pause.play();
        	
        });
        
        logoutButton.setOnAction(e -> {
        	AuthenticationStatus result = client.logoutUser();
        	switch(result) {
            case AuthenticationStatus.SUCCESS:
            	isLoggedIn=false;
            	loggedInUser = "";
            	windowTitle.set(loggedInUser);
            	tabPane.getTabs().removeAll(friendsTab, messagesTab, friendRequestsTab);
            	loginButton.setDisable(false);
            	registerButton.setDisable(false);
            	logoutButton.setDisable(true);
            	usernameField.setDisable(true);
            	passwordField.setDisable(true);
            	loginStatus.setText("Logout successful");
            	break;
            default:
            	loginStatus.setText("Logout failed"); 
        	}
        	PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(event -> loginStatus.setText(""));
            pause.play();
            
        });

        layout.getChildren().addAll(usernameField, passwordField, loginButton, registerButton, logoutButton, loginStatus);
        tab.setContent(layout);
        return tab;
    }

    private Tab createFriendsTab() {
        Tab tab = new Tab("Friends");

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // TableView setup
        TableView<Friend> friendsTable = new TableView<>();
        friendsTable.setPrefHeight(400);

        // Username column
        TableColumn<Friend, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setMinWidth(200);

        // Status column
        TableColumn<Friend, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setMinWidth(100);

        friendsTable.getColumns().addAll(usernameColumn, statusColumn);

        Button refreshFriendsButton = new Button("Refresh Friends");

        refreshFriendsButton.setOnAction(e -> {
            friendsTable.getItems().clear();
            client.getFriends().forEach(friend -> 
                friendsTable.getItems().add(new Friend(friend, client.getUserStatus(friend)))
            );
        });

        refreshFriendsButton.fire();

        friendsTable.setRowFactory(tv -> {
            TableRow<Friend> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                Friend selectedFriend = row.getItem();
                if (selectedFriend != null) {
                    if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                        // Left-click: Open chat window
                        ChatWindow chat = new ChatWindow(client, selectedFriend.getUsername(), loggedInUser);
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        // Right-click: Prompt to remove friend
                        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmationAlert.setTitle("Remove Friend");
                        confirmationAlert.setHeaderText("Are you sure you want to remove " + selectedFriend.getUsername() + " as a friend?");
                        confirmationAlert.setContentText("This action cannot be undone.");

                        confirmationAlert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                boolean removed = client.removeFriend(selectedFriend.getUsername());
                                if (removed) {
                                    friendsTable.getItems().remove(selectedFriend);
                                } else {
                                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                    errorAlert.setTitle("Error");
                                    errorAlert.setHeaderText("Failed to Remove Friend");
                                    errorAlert.setContentText("An error occurred while trying to remove " + selectedFriend.getUsername() + ".");
                                    errorAlert.show();
                                }
                            }
                        });
                    }
                }
            });
            return row;
        });

        layout.getChildren().addAll(friendsTable, refreshFriendsButton);
        tab.setContent(layout);
        return tab;
    }

    



    private Tab createFriendRequestsTab() {
        Tab tab = new Tab("Friend Requests");

        // Main Layout
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));

        // ====================== TOP SECTION: SEARCH USERS =======================
        Label searchLabel = new Label("Search for Users:");
        searchLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Search bar and button
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Enter username...");
        Button searchButton = new Button("Search");
        Button sendRequestButton = new Button("Send Friend Request");
        sendRequestButton.setDisable(true);

        searchBar.getChildren().addAll(searchField, searchButton, sendRequestButton);

        // ListView for search results
        ListView<String> searchResultsListView = new ListView<>();
        searchResultsListView.setPrefHeight(200);
        
        searchResultsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            sendRequestButton.setDisable(newSelection == null);
        });
        
        sendRequestButton.setOnAction(e -> {
            String selectedUser = searchResultsListView.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
            	if(client.checkIfFriendRequestExists(selectedUser)) {
            		showAlert(Alert.AlertType.WARNING,"Friend request", "Friend Request already sent");
            	}else {
            		boolean success = client.sendFriendRequest(selectedUser);
                    showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, "Friend Request",
                            success ? "Friend Request Sent!" : "Failed to Send Friend Request!");
            	}
            	
                
            }
        });
        
        // Search button action
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
            	searchResultsListView.getItems().clear();
                // Fetch search results (replace fetchSearchResults with your actual method)
            	List<String> results = client.lookupUsersSearch(query);
            	if(!results.isEmpty()) {            		
            		// In case there is the current user in the list
            		results.remove(loggedInUser);
            		ObservableList<String> observableList = FXCollections.observableArrayList(results);
            		searchResultsListView.setItems(observableList);
            		
            	}
               
            } else {
                showAlert(Alert.AlertType.WARNING, "Search", "Please enter a username to search.");
            }
        });

        VBox searchSection = new VBox(10, searchLabel, searchBar, searchResultsListView);

        // ================== BOTTOM SECTION: FRIEND REQUESTS ====================
        Label requestsLabel = new Label("Pending Friend Requests:");
        requestsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // ListView for friend requests
        ListView<String> requestsList = new ListView<>();
        requestsList.setPrefHeight(200);

        // Accept and Reject buttons
        Button acceptButton = new Button("Accept");
        Button rejectButton = new Button("Reject");
        Button refreshRequestsButton = new Button("Refresh");
        
        HBox requestButtons = new HBox(10);
        requestButtons.setAlignment(Pos.CENTER);
        requestButtons.getChildren().addAll(acceptButton, rejectButton, refreshRequestsButton);
        

        // Refresh friend requests button action
        refreshRequestsButton.setOnAction(e -> {
            requestsList.getItems().clear();
            requestsList.getItems().addAll(client.getFriendRequests());
        });
        refreshRequestsButton.fire();
        // Accept button action
        acceptButton.setOnAction(e -> {
            String selectedRequest = requestsList.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {                
            	if(client.addFriend(selectedRequest)) {
            		if(client.removeFriendRequest(selectedRequest)) {
            			showAlert(Alert.AlertType.INFORMATION, "Friend request", "Friend request accepted");
            		}else {
            			showAlert(Alert.AlertType.ERROR, "Friend Request", "An error occurred while accepting the request!");
            		}
            	}else {
            		showAlert(Alert.AlertType.ERROR, "Friend Request", "Failed to Accept Request!");
            	}
                refreshRequestsButton.fire();
            } else {
                showAlert(Alert.AlertType.WARNING, "Friend Request", "Please select a request to accept.");
            }
        });

        // Reject button action
        rejectButton.setOnAction(e -> {
            String selectedRequest = requestsList.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                if(client.removeFriendRequest(selectedRequest)) {
                	showAlert(Alert.AlertType.INFORMATION, "Friend request", "Friend request rejected");
                }else {
                	showAlert(Alert.AlertType.ERROR, "Friend request", "Failed to reject friend request");
                }
                refreshRequestsButton.fire();
            } else {
                showAlert(Alert.AlertType.WARNING, "Friend Request", "Please select a request to reject.");
            }
        });

        VBox requestsSection = new VBox(10, requestsLabel, requestsList, requestButtons);

        // ======================= Combine Both Sections =========================
        mainLayout.getChildren().addAll(searchSection, requestsSection);
        tab.setContent(mainLayout);
        
        return tab;
    }


    // Show an alert dialog
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
