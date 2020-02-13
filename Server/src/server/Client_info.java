/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tarun
 */
public class Client_info {

    private Socket socket;
    private String userName;
    private String password;
    Map<String, String> chats = new HashMap<String, String>(); //store the chat with other clients of current user

    public Client_info(Socket socket, String userName, String password) { //set username,password and Socket for client added to server
        setSocket(socket);
        setUserName(userName);
        setPassword(password);
    }

    public String getUserName() {       //return username of client
        return userName;
    }

    public void setUserName(String userName) {  //set username
        this.userName = userName;
    }

    public Socket getSocket() {  //return the socket
        return socket;
    }

    public String getMessage(String chatWith) {     //return chat between current user and ans chatwith user
        String message = ""; 
        message = chats.get(chatWith);
        System.out.println("get message chat with "+chatWith+" = "+message);
        return message;
    }

    public void setMessage(String chatWith, String message) {  //add the message between current user ad chatWith
        String existing = chats.get(chatWith);
        System.out.println("set message chat with "+chatWith);
        chats.put(chatWith, existing == null ? message : existing + "<:>" + message);
    }

    public void setSocket(Socket socket) {
        this.socket = socket;       //set sicket
    }
    
    public String getPassword() {
        return password;   //return the password
    }

    public void setPassword(String password) {
        this.password = password;   //set password
    }

}
