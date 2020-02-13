/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author tarun
 */
public class ReceivingFile implements Runnable {

    protected Socket socket; //socket throught which file is receiving
    protected DataInputStream dis;
    protected DataOutputStream dos;
    private final int BUFFER_SIZE = 1024; //size of buffer
    private String filename; //file name
    private String ReceiveFrom;  //username of client from which file is receiving

    ReceivingFile(Socket soc, String details) {
        this.socket = soc;                  
        filename = details.split("<:>")[0];    //extract file name from detaisl
        ReceiveFrom = details.split("<:>")[2];  //extract client username form which file is receiving
    }
    // received file is written in folder with the name for client who send this file 
    @Override
    public void run() {
        try {
            File f = new File("/home/tarun/Music/" + ReceiveFrom);  //make file pointer to which file is going to be written in disk
            if (f.exists() && !f.isDirectory() || !f.exists()) {   //check if folder is already exist otherwie create folder
                System.out.println("Creating Directory " + ReceiveFrom);//with tha name of sender of file
                f.mkdir();
            }
            String path = "/home/tarun/Music/" + ReceiveFrom + "/" + filename;
            System.out.println("Start Writing file " + path);
            FileOutputStream fos = new FileOutputStream(path);  //make file output stream to write in disk
            InputStream input = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(input); //buffer stream to read bytes from socket
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = bis.read(buffer)) > 0) { //receive file from stream
                System.out.println("start receiving file .." + filename+" count "+count);
                fos.write(buffer, 0, count);  //write bytes in disk
            }
            System.out.println("Writing file ");
            fos.flush();
            fos.close(); //close file output stream
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
