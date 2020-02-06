/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author tarun
 */
public class ClientBackendController {

    /**
     * Initializes the controller class.
     */
    private Parent parent;
    private Scene scene;
    private Stage stage;
    static Socket s;
    static ServerSocket ss;
    static DataInputStream dis;
    static DataOutputStream dos;
    ObservableList<String> UsersOnline = FXCollections.observableArrayList();
    @FXML
    private ListView<String> OnlineUsers;
    @FXML
    private TextArea Message;
    @FXML
    private TextField Send;
    public static String userName, Password;
    @FXML
    private Button sendButton;
    String chatWith;

    public ClientBackendController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientBackend.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            scene = new Scene(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void SelectedUser(MouseEvent event) throws IOException {
        chatWith = (String) OnlineUsers.getSelectionModel().getSelectedItem();
        if (chatWith.isEmpty() || chatWith == null) {

        } else if (chatWith.equals(userName)) {
            Alert sameclient = new Alert(AlertType.INFORMATION);
            sameclient.setTitle("Same Client");
            sameclient.setContentText("Cannot chat with yourself");
            sameclient.setHeaderText("Hello " + userName);
            sameclient.show();
            chatWith = null;
        } else {
            dos.writeUTF("ChatWith" + chatWith);
            dos.flush();
            System.out.println("ChatWith" + "<:>" + chatWith + "<:>" + userName);
            Message.setDisable(false);
            sendButton.setDisable(false);
            Send.setDisable(false);
        }

    }

    public void UpdatedUsers(String msg) {
        String[] users = msg.split("<:>");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                UsersOnline.clear();
                UsersOnline.addAll(users);
                OnlineUsers.setItems(UsersOnline);
            }
        });

    }

    @FXML
    void SendMessage(ActionEvent event) throws IOException {
        if (chatWith.equals(userName)) {
            Alert sameclient = new Alert(AlertType.INFORMATION);
            sameclient.setTitle("Same Client");
            sameclient.setContentText("Cannot chat with yourself");
            sameclient.setHeaderText("Hello " + userName);
            sameclient.show();
        } else {
            dos.writeUTF(Send.getText());
            dos.flush();
            System.out.println("send message: " + Send.getText());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Message.appendText(userName + "--->" + Send.getText() + System.lineSeparator());
                    Send.setText("");
                }
            });
        }

    }
    Task Read = new Task<Void>() {
        @Override
        public Void call() throws Exception {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("read message= " + msg);
                if (msg.startsWith("UpdatesUsers<:>")) {
                    System.out.println("new user added");
                    UpdatedUsers(msg.substring(15));
                } else if (msg.startsWith("Refress<:>")) {
                    String[] chats = msg.substring(10).split("<:>");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Message.setText("");

                        }
                    });
                    for (String lines : chats) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Message.appendText(lines + System.lineSeparator());
                            }
                        });

                    }

                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Message.appendText(msg + System.lineSeparator());
                        }
                    });
                }
                //Thread.sleep(100);
            }
        }
    };

    public void initialize(Stage stage, String userName, String Password) {

        stage.setTitle(userName);
        stage.setScene(scene);
        stage.hide();
        stage.show();
        this.userName = userName;
        this.Password = Password;
        try {
            dos.writeUTF("login<:>" + userName + "<:>" + Password);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        Message.setDisable(true);
        Message.setEditable(false);
        sendButton.setDisable(true);
        Send.setDisable(true);
        Thread th = new Thread(Read);
        th.setDaemon(true);
        th.start();
    }
}
