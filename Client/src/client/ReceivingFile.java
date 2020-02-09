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
import java.text.DecimalFormat;
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
    protected DecimalFormat df = new DecimalFormat("##,#00");
    private final int BUFFER_SIZE = 100;

    ReceivingFile(Socket soc) {
        this.socket = soc;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String data = dis.readUTF();
                st = new StringTokenizer(data);
                String CMD = st.nextToken();
                switch (CMD) {
                    case "CMD_SENDFILE":
                        String consignee = null;
                        try {
                            String filename = st.nextToken();
                            int filesize = Integer.parseInt(st.nextToken());
                            consignee = st.nextToken();
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
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ReceivingFileThread]: " + e.getMessage());
        }
    }

}
