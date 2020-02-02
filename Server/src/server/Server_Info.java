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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
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
    private Button StopServer;
    @FXML
    public TextArea Users;
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

    @Override
    public void run() {
        while (thread != null) {
            try {
                Log.appendText(System.lineSeparator() + "Thread Started....");
                socket = serverSocket.accept();
                Log.appendText(System.lineSeparator() + "New Client Connected1....");
                new Server_to_client(this, socket);
                Log.appendText(System.lineSeparator() + "New Client Connected2....");
            } catch (IOException ex) {
                StopServer();
            }
        }
        Log.setText("Server has stopped!");
    }

    @FXML
    private void StopServer() {        //button
        if (serverSocket != null) {
            if (client_info != null) {
                client_info.stream().forEach(client -> {
                    try {
                        sendMessageToClient(client.getSocket(), "DISC");
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

    String getInfoForClient(String userName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void sentPrivateMessage(Socket clientSocket, String fromClient, String message, String toClient) throws IOException {
        Log.appendText(System.lineSeparator() + "Private message(from->" + fromClient + " to->" + toClient + ") Message: (" + message + ")");
        /////// Notify all the Clients about the Message //////
        for (Client_info client : client_info) {
            System.out.println("printing in private message for sender "+client.getUserName());
            if (client.getUserName().equals(toClient)) {
                sendMessageToClient(client.getSocket(), fromClient+" -->" +message );
                break;
            }
        }
    }

    boolean addUser(Socket clientSocket, String userName, String password) {
        System.out.println(userName+" Adden in add user");
        Users.appendText(System.lineSeparator() + userName);
        client_info.add(new Client_info(socket, userName, password));
        return true;

    }

    void removeUser(Socket clientSocket, String userName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @FXML
    private void StartServer() throws InterruptedException {

        ////////// Initialize the Server Socket ///////
        try {

            serverSocket = new ServerSocket(serverPort);
            System.out.println(serverSocket.getInetAddress().getCanonicalHostName());
        } catch (IOException e) {
            e.printStackTrace();
            Log.setText(e.getMessage());
            return;
        }

        ////////// Initialize the ArrayLists //////////
        client_info = new ArrayList<Client_info>();
        roomMessages = new ArrayList<String>();

        ////////// Initialize the thread //////////
        thread = new Thread(this);
        thread.start();

        ////////// Configure the Buttons //////////
        StartServer.setDisable(true);
        Log.setText("Waiting for clients to connect....");
        Thread.sleep(500);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
