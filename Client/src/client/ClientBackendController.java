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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        String Users = (String) OnlineUsers.getSelectionModel().getSelectedItem();
        if (Users.isEmpty() || Users == null) {

        } else {
            dos.writeUTF("ChatWith" + "<:>" + Users);
            dos.flush();
            System.out.println("ChatWith" + "<:>" + Users + "<:>" + userName);
        }
    }
    public void UpdatedUsers(String msg)
    {
        UsersOnline.removeAll();
        String []users=msg.split("<:>");
        UsersOnline.addAll(users);
        OnlineUsers.getItems().setAll(UsersOnline);
    }
    @FXML
    void SendMessage(MouseEvent event) throws IOException {
        dos.writeUTF(userName + "<:>" + Send.getText());
        dos.flush();
        System.out.println("send message: " + Send.getText());
        Send.setText("");
    }
    Task Read = new Task<Void>() {
        @Override
        public Void call() throws Exception {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("read message= " + msg);
                msg = new String(System.lineSeparator() + System.lineSeparator() + msg);
                if(msg.startsWith("UpdatesUsers"))
                {
                    UpdatedUsers(msg.substring(12));
                }
                else if(msg.startsWith("PreviousChat"))
                {
                    Message.setText(msg.substring(17));
                }
                else Message.appendText(msg);
                Thread.sleep(100);
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
        Thread th = new Thread(Read);
        th.setDaemon(true);
        th.start();
    }
}
