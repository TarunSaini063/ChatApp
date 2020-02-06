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
     Map<String, String> chats = new HashMap<String, String>();
    public Client_info(Socket socket, String userName, String password) {
        setSocket(socket);
        setUserName(userName);
        setPassword(password);
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Socket getSocket() {
        return socket;
    }
    public String getMessage(String chatWith)
    {
        String message="";
        message=chats.get(chatWith);
        return message;
    }
    public void setMessage(String chatWith,String message)
    {
        String existing = chats.get(chatWith);
        chats.put(chatWith, existing == null ? message : existing +"<:>"+message);
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public String toString() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}