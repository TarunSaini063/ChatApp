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

    protected Socket socket;
    protected DataInputStream dis;
    protected DataOutputStream dos;
    protected StringTokenizer st;
    private final int BUFFER_SIZE = 1024;
    private String filename;
    private String ReceiveFrom;

    ReceivingFile(Socket soc, String details) {
        this.socket = soc;
        filename = details.split("<:>")[0];
        ReceiveFrom = details.split("<:>")[2];
    }

    @Override
    public void run() {
        try {
            File f = new File("/home/tarun/Music/" + ReceiveFrom);
            if (f.exists() && !f.isDirectory() || !f.exists()) {
                System.out.println("Creating Directory " + ReceiveFrom);
                f.mkdir();
            }
            String path = "/home/tarun/Music/" + ReceiveFrom + "/" + filename;
            System.out.println("Start Writing file " + path);
            FileOutputStream fos = new FileOutputStream(path);
            InputStream input = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(input);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = bis.read(buffer)) > 0) {
                System.out.println("start receiving file .." + filename+" count "+count);
                fos.write(buffer, 0, count);
            }
            System.out.println("Writing file ");
            fos.flush();
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
