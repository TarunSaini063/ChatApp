/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author tarun
 */
public class Server_Info extends Application implements Initializable, Runnable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextArea Log;  //log area in server show the logs 
    @FXML
    private Button StartServer;
    @FXML
    private ListView<String> Users;  //listview of currently connected user to srver
    ObservableList<String> UsersOnline = FXCollections.observableArrayList();
    @FXML
    private Button StopServer;
    int serverPort = 1234; //port on which server sending and receiving data
    ServerSocket serverSocket;
    Thread thread;
    Socket socket;  //socket to which new client is going to connect every time
    DataOutputStream dos;
    ArrayList<Client_info> client_info; //contain the object of users connected to client
    private final int BUFFER_SIZE = 1024; //size of buffer to redirect file transmission
    public static void main(String[] args) {
        launch(args);   //launch initializer to initialize javafx elements
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.resizableProperty().setValue(Boolean.FALSE);
        Parent root = FXMLLoader.load(getClass().getResource("Server_Info.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void UpdateUsers(MouseEvent event) {

    }

    @Override
    public void run() {                 //this thread take care of when client connects
        while (thread != null) {        // with server
            try {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Log.appendText(System.lineSeparator() + "Thread Started....");
                    }
                });
                socket = serverSocket.accept();   //waiting for client to connect 
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Log.appendText(System.lineSeparator() + "New Client Connected....");
                    }
                });
                new Server_to_client(this, socket);// if connection is successful than create new object pass this class object and socket of current client socket
            } catch (IOException ex) {
                StopServer();       //if any exception occur Stop server
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Log.setText("Server has stopped!"); 
            }
        });
    }

    @FXML
    private void StopServer() {        //when StopServer button is pressed stop the server
        if (serverSocket != null) {
            if (client_info != null) {
                client_info.stream().forEach(client -> {
                    try {
                        sendMessageToClient(client.getSocket(), "Server Ofline");  //tell all client that server is stoped
                    } catch (IOException ex) {
                        Logger.getLogger(Server_Info.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
            thread = null;
            try {
                serverSocket.close(); //close the serverSocket
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client_info = null;
            }
            StartServer.setDisable(false);
        }
    }

    public void sendMessageToClient(Socket clientSocket, String message) throws IOException { //send message to client with socket clientSocket
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                dos = new DataOutputStream(clientSocket.getOutputStream()); //create dataoutput stream
                dos.writeUTF(message); //send message 
                dos.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                dos.close();  //close the stream
            }
        }
    }

    String getInfoForClient(Socket clientSocket, String userName, String chatWith) throws IOException { //this message send the previous chat
        String chats = "Refress<:>";                                                            //between username ans chatwith
        String flag = null;
        System.out.println("getInfo fro client server Info " + userName);
        for (Client_info client : client_info) {                    //search for the client with which pervious chat is required
            if (client.getUserName().equals(userName)) {
                flag = client.getMessage(chatWith);
                break;
            }
        }
        if (flag != null) {
            chats = chats.concat(flag);
        }
        return chats;                       //return previous chat
    }

    void sentPrivateMessage(Socket clientSocket, String fromClient, String message, String toClient) throws IOException {//handle the chat receive by client and call various method 
        if (message.startsWith("Refress<:>")) {    //if message starts with Refress<:> than it ask from previous chat
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Log.appendText(message + System.lineSeparator());
                }
            });
            sendMessageToClient(clientSocket, message);
        } else {                                    //else normal chat is happens between fromClient and toClient
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Log.appendText(System.lineSeparator() + "Message(from-> " + fromClient + " to->" + toClient + ") Message: (" + message + ")" + System.lineSeparator());
                }
            });
            for (Client_info client : client_info) {        //search from toClient in Client_info object list
                if (client.getUserName().equals(toClient)) {
                    System.out.println("updating message for receiver chats " + client.getUserName());
                    client.setMessage(fromClient, fromClient + "--->" + message);   //update the chat
                    sendMessageToClient(client.getSocket(), fromClient + " -->" + message); //send message to corresponding client
                    break;
                }
            }
            for (Client_info client : client_info) {        //search from fromClient in Client_info object list

                if (client.getUserName().equals(fromClient)) {
                    client.setMessage(toClient, fromClient + "--->" + message);  //update the chat
                    System.out.println("updating message for sender chats " + client.getUserName());
                    break;
                }
            }
        }
    }

    boolean addUser(Socket clientSocket, String userName, String password) throws IOException {  //whenever new user is connected with server
        System.out.println(userName + " Adden in add user");
        String msg = "";
        for (Client_info client : client_info) {
            if (msg == "") {
                msg = new String(client.getUserName() + "<:>");
            } else {
                msg = msg.concat(client.getUserName() + "<:>");
            }
        }
        if (msg == "") {
            msg = new String(userName);
        } else {
            msg = msg.concat(userName);
        }
        System.out.println("in add users current users = " + msg); //msg store the list of all connect user with the server
        String[] users = msg.split("<:>");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {     //uupdate the list of connected user with the Server
                UsersOnline.clear();
                UsersOnline.addAll(users);
                Users.getItems().setAll(UsersOnline);
            }
        });
        client_info.add(new Client_info(socket, userName, password));  //add this new client in client_info object list
        for (Client_info client : client_info) {
            sendMessageToClient(client.getSocket(), "UpdatesUsers<:>" + msg);//notify other users about newly connected user
        }
        return true;
    }

    void removeUser(Socket clientSocket, String userName) throws IOException { //when ever a user remove this function safely disconnect that user form server
        System.out.println(userName + " removeing in removeUser");
        UsersOnline.clear();         //clear the list of current client than update it 
        String msg = "";
        for (Client_info client : client_info) {
            if (!client.getUserName().equals(userName)) {
                if (msg == "") {
                    msg = new String(client.getUserName() + "<:>");
                } else {
                    msg = msg.concat(client.getUserName() + "<:>");
                }
            } else {
                client_info.remove(client);
            }
        }
        System.out.println(msg);
        String[] users = msg.split("<:>");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Users.getItems().clear();
                Users.getItems().setAll(UsersOnline);
            }
        });
        for (Client_info client : client_info) {
            sendMessageToClient(client.getSocket(), "UpdatesUsers" + msg); //inform other users about disconnected user
        }
    }

    @FXML
    private void StartServer() throws InterruptedException {  //whenever start server button is pressed than this method start ther server
        try {
            serverSocket = new ServerSocket(serverPort);  //create a socketserver
            System.out.println(serverSocket.getInetAddress().getCanonicalHostName());
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Log.setText(e.getMessage());
                }
            });

            return;
        }
        client_info = new ArrayList<Client_info>(); //intialize the list that contains object of client_info class
       
        thread = new Thread(this);
        thread.start();   //start the run method
        StartServer.setDisable(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Log.setText("Waiting for clients to connect...." + System.lineSeparator());
            }
        });

        Thread.sleep(500);//wait for the some time to fully intiallize the list of users
    }

    void ReciveFile(Socket sock, String senderInfo) throws FileNotFoundException, IOException { //this method handle
        String sendto;                                                  //the transmission of file between two users
        String[] details = senderInfo.split("<:>");                     //Find details about the user sending file and file info
        sendto = details[0];
        Socket sendtosocket = null;
        for (Client_info client : client_info) {        //search for the client to which file is send
            if (client.getUserName().equals(sendto)) {
                sendtosocket = client.getSocket();
                break;
            }
        }
        if (sendtosocket != null) {  //if receiver client is find successfully than redirect the file stream to that client
            sendMessageToClient(sendtosocket, "RECIEVE<:>"+details[1]+"<:>"+details[2]+"<:>"+details[0]);
            InputStream input = sock.getInputStream(); 
            OutputStream sendFile = sendtosocket.getOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int cnt;
            while ((cnt = input.read(buffer)) > 0) {
                sendFile.write(buffer, 0, cnt);
                System.out.println("sending file... "+cnt);
            }
            sendFile.flush();  
            System.out.println("Closing OutputStream");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

}
