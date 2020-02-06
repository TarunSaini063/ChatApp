/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataOutputStream;
import java.io.IOException;
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
    private TextArea Log;
    @FXML
    private Button StartServer;
    @FXML
    private ListView<String> Users;
    ObservableList<String> UsersOnline = FXCollections.observableArrayList();
    @FXML
    private Button StopServer;
    int serverPort = 1234;
    ServerSocket serverSocket;
    Thread thread;
    Socket socket;
    DataOutputStream dos;
    ArrayList<Client_info> client_info;
    ArrayList<String> roomMessages;

    public static void main(String[] args) {
        launch(args);
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
    public void run() {
        while (thread != null) {
            try {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Log.appendText(System.lineSeparator() + "Thread Started....");
                    }
                });
                socket = serverSocket.accept();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Log.appendText(System.lineSeparator() + "New Client Connected....");
                    }
                });
                new Server_to_client(this, socket);
            } catch (IOException ex) {
                StopServer();
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
    private void StopServer() {        //button
        if (serverSocket != null) {
            if (client_info != null) {
                client_info.stream().forEach(client -> {
                    try {
                        sendMessageToClient(client.getSocket(), "Server Ofline");
                    } catch (IOException ex) {
                        Logger.getLogger(Server_Info.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
            thread = null;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                client_info = null;
                roomMessages = null;
            }
            StartServer.setDisable(false);
        }
    }

    public void sendMessageToClient(Socket clientSocket, String message) throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                dos = new DataOutputStream(clientSocket.getOutputStream());
                dos.writeUTF(message);
            } catch (IOException ex) {
                ex.printStackTrace();
                dos.close();
            }
        }
    }

    String getInfoForClient(Socket clientSocket, String userName, String chatWith) throws IOException {
        String chats = "Refress<:>";
        String flag = null;
        System.out.println("getInfo fro client server Info " + userName);
        for (Client_info client : client_info) {
            if (client.getUserName().equals(userName)) {
                flag = client.getMessage(chatWith);
                break;
            }
        }
        if (flag != null) {
            chats = chats.concat(flag);
        }
        return chats;
    }

    void sentPrivateMessage(Socket clientSocket, String fromClient, String message, String toClient) throws IOException {
        Client_info chatfrom = null, chatto = null;
        if (message.startsWith("Refress<:>")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Log.appendText(message + System.lineSeparator());
                }
            });
            sendMessageToClient(clientSocket, message);
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Log.appendText(System.lineSeparator() + "Private message(from->" + fromClient + " to->" + toClient + ") Message: (" + message + ")" + System.lineSeparator());
                }
            });
            for (Client_info client : client_info) {
                System.out.println("printing in private message for serching to message receiver " + client.getUserName());
                if (client.getUserName().equals(toClient)) {
                    chatto = client;
                    break;
                }
            }
            for (Client_info client : client_info) {
                System.out.println("updating message for sender chats");
                if (client.getUserName().equals(fromClient)) {
                    chatfrom = client;
                    break;
                }
            }
            chatto.setMessage(toClient, fromClient + "--->" + message);
            chatfrom.setMessage(toClient, fromClient + "--->" + message);
            sendMessageToClient(chatto.getSocket(), fromClient + " -->" + message);

        }
    }

    boolean addUser(Socket clientSocket, String userName, String password) throws IOException {
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
        System.out.println("in add users current users = " + msg);
        String[] users = msg.split("<:>");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                UsersOnline.clear();
                UsersOnline.addAll(users);
                Users.getItems().setAll(UsersOnline);
            }
        });
        client_info.add(new Client_info(socket, userName, password));
        for (Client_info client : client_info) {
            sendMessageToClient(client.getSocket(), "UpdatesUsers<:>" + msg);
        }
        return true;
    }

    void removeUser(Socket clientSocket, String userName) throws IOException {
        System.out.println(userName + " removeing in removeUser");
        UsersOnline.clear();
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
            sendMessageToClient(client.getSocket(), "UpdatesUsers" + msg);
        }
    }

    @FXML
    private void StartServer() throws InterruptedException {
        try {
            serverSocket = new ServerSocket(serverPort);
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
        client_info = new ArrayList<Client_info>();
        roomMessages = new ArrayList<String>();
        thread = new Thread(this);
        thread.start();
        StartServer.setDisable(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Log.setText("Waiting for clients to connect...." + System.lineSeparator());
            }
        });

        Thread.sleep(500);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

}
