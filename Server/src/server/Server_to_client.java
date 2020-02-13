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

    Server_Info server;  //object of serverinfo class to perform various function lick send,updatechat etc of client
    Socket clientSocket;  //socket of client
    Thread thread;

    DataInputStream fromClient;
    DataOutputStream toClient;
    String text;  //message send by client
    String userName; //username of clinet
    String Password; 
    String chatWith;//username of client with which is client is chating
    String chats; //previous chat between username and chatwith

    Server_to_client(Server_Info server, Socket clientSocket) {
        System.out.println(System.lineSeparator() + "New client arrive");
        this.server = server;            //set socket of this class to client socket
        this.clientSocket = clientSocket;
        try {
            fromClient = new DataInputStream(clientSocket.getInputStream());
            toClient = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();  //start the thread to recevive the message from client(username)
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void run() {
        while (thread != null) {
            try {
                if(fromClient==null)   //if input stream is null reintialize it
                {
                    fromClient=new DataInputStream(clientSocket.getInputStream());
                }
                String text = fromClient.readUTF(); //read message from client(username)
                System.out.println("Message receive " + text);
                if (text.startsWith("exit")) { //if message starts with exit
                    disconnectUser();         //than disconnect the user
                } else if (text.startsWith("login")) { //if message is start with login 
                    String[] arr = text.split("<:>");  //add new user to client list
                    server.addUser(clientSocket, arr[1], arr[2]);
                    this.userName = arr[1];
                    this.Password = arr[2];
                    System.out.println("(in server to client)Username of connected user in server to client " + this.userName);
                    System.out.println(arr[1] + " in thread");
                } else if (text.startsWith("ChatWith")) { //if message is stat with chatwith set the value of chatwith variable with the 
                    chatWith = text.substring(8);          //name of client with which chat is going to start
                    chats = server.getInfoForClient(clientSocket, userName, chatWith);
                    System.out.println("inside server to client " + chats);
                    server.sentPrivateMessage(clientSocket, userName, chats, chatWith);//user server_info class method and send previous chat between these two client
                }  else if (text.startsWith("SEND<:>")) { //it message starts with SEND<:> then client is sending file to chatWiht user
                    System.out.println("Sending to receive function "+chatWith+"<:>"+text.substring(7));
                    server.ReciveFile(clientSocket,chatWith+"<:>"+text.substring(7));//call Server_info class method to handle redirect file transmission between correct users
                }else {
                    System.out.println("Simple message server to client " + chats+" username "+userName+" chatwith "+chatWith);
                    server.sentPrivateMessage(clientSocket, userName, text, chatWith);//if it is a normal message 
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    disconnectUser(); //if any exception occur disconnect the current user
                } catch (IOException ex) {
                    Logger.getLogger(Server_to_client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void disconnectUser() throws IOException {
        server.removeUser(clientSocket, userName); //remove user from Cliet_info list
        stopConnection();  //stop the connection
    }

    public void stopConnection() throws IOException {

        thread = null; //stop the thread
        toClient.close(); //close data stream
        try {
            fromClient.close(); //close datastream
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            clientSocket.close();//close the socket
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
