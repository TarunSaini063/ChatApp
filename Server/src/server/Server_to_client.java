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
    String Password;
    String chatWith;
    String chats;

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
                    this.userName = arr[1];
                    this.Password = arr[2];
                    System.out.println("(in server to client)Username of connected user in server to client "+this.userName);
                    System.out.println(arr[1] + " in thread");
                } else if (text.startsWith("ChatWith")){
                    chatWith=text.substring(8);
                    chats=server.getInfoForClient(clientSocket,userName, chatWith);
                    System.out.println("inside server to client "+ chats);
                    server.sentPrivateMessage(clientSocket, userName,chats,chatWith);
                }else
                {
                    server.sentPrivateMessage(clientSocket, userName,text,chatWith);
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