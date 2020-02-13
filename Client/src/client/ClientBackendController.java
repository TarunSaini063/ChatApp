/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
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
    static Socket s; //socket throught client is connect to server
    static ServerSocket ss; 
    static DataInputStream dis;
    static DataOutputStream dos;
    ObservableList<String> UsersOnline = FXCollections.observableArrayList(); //to update elements of listview
    @FXML
    private ListView<String> OnlineUsers; //listview of current online users
    @FXML
    private TextArea Message; //chat window
    @FXML
    private TextField Send; //text file to send message
    public static String userName, Password;
    @FXML
    private Button sendButton;
    String chatWith;
    @FXML
    private TextField FileName; //file name is shown here whenever file is select to send
    @FXML
    private Button SelectFile; //button to select file (open FileChooser)
    @FXML
    private Button SendFile;  //button to send file
    private File file; //file pointer (file is going to send)

    public ClientBackendController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientBackend.fxml")); //javafx Components are loaded
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load(); //create parent and set scene of javafx layout
            scene = new Scene(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void SelectedUser(MouseEvent event) throws IOException {        //Whenever Client click on User List(Chat With) 
        chatWith = (String) OnlineUsers.getSelectionModel().getSelectedItem(); //Get the username of client which chat is  start in chatWith
        if (chatWith == null) {

        } else if (chatWith.equals(userName)) {      //if Client Click on its Own Username than 
            Alert sameclient = new Alert(AlertType.INFORMATION);  //Alert is shown 
            sameclient.setTitle("Same Client");
            sameclient.setContentText("Cannot chat with yourself");
            sameclient.setHeaderText("Hello " + userName);
            sameclient.show();
            chatWith = null;
            Message.setDisable(true);
            sendButton.setDisable(true);
            Send.setDisable(true);
            FileName.setDisable(true);
            SelectFile.setDisable(true);
        } else {                                        //if clicked user is valid then
            dos.writeUTF("ChatWith" + chatWith);        //we send chatwith with username of person with is chat is going to start to server
            dos.flush();
            System.out.println("ChatWith" + "<:>" + chatWith + "<:>" + userName);
            Message.setDisable(false);
            sendButton.setDisable(false);
            Send.setDisable(false);
            FileName.setDisable(false);
            SelectFile.setDisable(false);
        }

    }

    public void UpdatedUsers(String msg) {   //Whenever new user is connected to server this function update the list
        String[] users = msg.split("<:>");  //of current online user
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
    void SendMessage(ActionEvent event) throws IOException { //whenever user click on send button 
        if (chatWith.equals(userName)) {                    //check if it send message to its self(not allowed
            Alert sameclient = new Alert(AlertType.INFORMATION);
            sameclient.setTitle("Same Client");
            sameclient.setContentText("Cannot chat with yourself");
            sameclient.setHeaderText("Hello " + userName);
            sameclient.show();
        } else {                                    //Send the message
            dos.writeUTF(Send.getText());
            dos.flush();
            System.out.println("send message: " + Send.getText());
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Message.appendText(userName + "--->" + Send.getText() + System.lineSeparator()); //Add this message to current chat window
                    Send.setText("");
                }
            });
        }

    }

    @FXML
    void SendFile(ActionEvent event) throws IOException {       //Whenever user click on send file button
        dos.writeUTF("SEND<:>" + file.getName()+"<:>"+file.length());// first we tell to server that client is going to send file 
        dos.flush();                                                 // along with file details
        SendFile.setDisable(true);
        new Thread(new SendingFile(s, file.getAbsolutePath(), chatWith, userName)).start(); //start new thread of sendfile class which handle background file transmission
    }

    @FXML
    void SelectFile(ActionEvent event) {    //whenever User click on select file 
        FileChooser chooser = new FileChooser();    //it open system file choser
        chooser.setTitle("Open File");
        file = chooser.showOpenDialog(Send.getScene().getWindow());  //get name of selected file
        String file_name;
        if (file != null) {    //if selected file is correct
            file_name = file.getName();
            FileName.setText(file_name);//initialize label with file information
            SendFile.setDisable(false);
        } else {
            FileName.setText("Error in Selecting File"); //otherwise show error
        }
    }

    void ReciveFile(String details) throws FileNotFoundException, IOException {
        new Thread(new ReceivingFile(s,details)).start(); //wherver client is informed by user that file is received by you with file information
    }                                                     //we start thread of class ReciveFile which handle the receiving file data 
    Task Read = new Task<Void>() {
        @Override                                           //this thread receive the data send by server after call various method to handle it
        public Void call() throws Exception {
            while (true) {
                if(dis==null)                   //if DataInputStream is null initialize it again (first time in client class)
                {
                    System.out.println("dis is null ");
                    dis=new DataInputStream(s.getInputStream());
                }
                String msg = dis.readUTF();         //Read the message from server
                System.out.println("read message= " + msg + " mesage for " + userName);
                if (msg.startsWith("UpdatesUsers<:>")) {  //If the message starts which update user then 
                    System.out.println("new user added");
                    UpdatedUsers(msg.substring(15));        //UpdatedUsers method is called and list of all user is send as an argument
                } else if (msg.startsWith("Refress<:>")) {  //if message starts with Refress<:>(its mean complete chat between current user and chatWith
                    String[] chats = msg.substring(10).split("<:>"); //is resend (Previous chat between these users two users
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Message.setText("");  //clear the current message window
                        }
                    });
                    for (String lines : chats) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Message.appendText(lines + System.lineSeparator()); //append new message to message window
                            }
                        });
                    }
                } else if (msg.startsWith("RECIEVE<:>")) {  //if message receive is RECIEVE<:> than server is sending file
                    ReciveFile(msg.substring(10)); //Call ReceiveFile method which start ReceivingFile thread
                } else {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (chatWith != null && msg.startsWith(chatWith)) {    //if message receive and is is send by currently connected user than appe
                                Message.appendText(msg + System.lineSeparator());
                            }
                        }
                    });
                }
            }
        }
    };

    public void initialize(Stage stage, String userName, String Password) { //Intialize is called after constructer
        stage.setTitle(userName);
        stage.setScene(scene);      //set javafx elements property
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
        SelectFile.setDisable(true);
        FileName.setDisable(true);
        SendFile.setDisable(true);
        FileName.setEditable(false);
        Thread th = new Thread(Read); //start the reading thread which handle the message received from server
        th.setDaemon(true);
        th.start();
    }
}
