/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author tarun
 */
public class Server_to_client implements Runnable {

    Server_Info server;
    Socket clientSocket;
    Thread thread;

    DataInputStream fromClient;
    DataOutputStream toClient;
    String text;
    String userName;

    Server_to_client(Server_Info server, Socket clientSocket) {
        System.out.println(System.lineSeparator() + "New client arrive");
        this.server = server;
        this.clientSocket = clientSocket;
        try {
            fromClient = new DataInputStream(clientSocket.getInputStream());
            toClient = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void run() {
        while (thread != null) {
            try {
                String text = fromClient.readUTF();
                System.out.println("Message receive " + text);
                if (text.startsWith("exit")) {
                    disconnectUser();
                } else if (text.startsWith("login")) {
                    String[] arr = text.split("<:>");
                    server.addUser(clientSocket, arr[1], arr[2]);
                    System.out.println(arr[1] + " in thread");
                    //server.sentPrivateMessage(clientSocket, arr[0], "Login", arr[1]);
                } else {
                    String[] arr = text.split("<:>");
                    server.sentPrivateMessage(clientSocket, arr[0], arr[2], arr[1]);
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    disconnectUser();
                } catch (IOException ex) {
                    Logger.getLogger(Server_to_client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void disconnectUser() throws IOException {
        server.removeUser(clientSocket, userName);
        stopConnection();
    }

    public void stopConnection() throws IOException {

        thread = null;
        toClient.close();
        try {
            fromClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
