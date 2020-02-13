/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author tarun
 */
public class SendingFile implements Runnable {

    protected Socket socket; //socket throught which file is receiving
    protected String file;   //path of file from disk
    protected String receiver; //username of receiver of file
    protected String sender;  //username of sender of file
    private final int BUFFER_SIZE = 1024; //buffer size

    public SendingFile(Socket soc, String file, String receiver, String sender) { //set values of variable
        this.socket = soc;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            File filename = new File(file); //make file pointer of the file from disk location ""
            InputStream input = new FileInputStream(filename); //file input stream to reda dtata from file
            OutputStream output = socket.getOutputStream(); //output stream to send data to receiver
            BufferedInputStream bis = new BufferedInputStream(input); //buffer to store block of data from disk 
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = bis.read(buffer)) > 0) { //check if file data is send completely
                output.write(buffer, 0, count); //write data to ouput stream
                System.out.println("Sending "+filename.getAbsolutePath()+" "+count+" bytes");
            } 
            output.flush();  //flush data from socket stream
        } catch (IOException e) {
            System.out.println("[SendFile]: " + e.getMessage());
        }
    }

}
