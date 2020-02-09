/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected StringTokenizer st;
    private final int BUFFER_SIZE = 100;
    private int filesize;
    private String filename;

    ReceivingFile(Socket soc, String details) {
        this.socket = soc;
        filesize = Integer.parseInt(details.split("<;>")[1]);
        filename = details.split("<;>")[0];
    }

    @Override
    public void run() {
        try {

            String path = "/home/tarun/Music/" + filename;
            FileOutputStream fos = new FileOutputStream(path);
            InputStream input = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(input);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count, percent = 0;
            while ((count = bis.read(buffer)) != -1) {
                percent = percent + count;
                int p = (percent / filesize);
                fos.write(buffer, 0, count);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
