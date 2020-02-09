/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
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

    protected Socket socket;
    private DataOutputStream dos;
    protected String file;
    protected String receiver;
    protected String sender;
    private final int BUFFER_SIZE = 100;

    public SendingFile(Socket soc, String file, String receiver, String sender) {
        this.socket = soc;
        this.file = file;
        this.receiver = receiver;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            File filename = new File(file);
            int len = (int) filename.length();
            int filesize = (int) Math.ceil(len / BUFFER_SIZE);
            InputStream input = new FileInputStream(filename);
            OutputStream output = socket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(input);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count, percent = 0;
            while ((count = bis.read(buffer)) > 0) {
                percent = percent + count;
                int p = (percent / filesize);
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
        } catch (IOException e) {
            System.out.println("[SendFile]: " + e.getMessage());
        }
    }

}
