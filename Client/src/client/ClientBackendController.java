/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author tarun
 */
public class ClientBackendController {

    /**
     * Initializes the controller class.
     */
    private Parent parent;
    private Scene scene;
    private Stage stage;
    static Socket s;
    static ServerSocket ss;
    static DataInputStream dis;
    static DataOutputStream dos;
    @FXML
    private TextArea Message;
    @FXML
    private TextField Send;
    public static String userName, Password;
    public ClientBackendController() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientBackend.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            scene = new Scene(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    @FXML
    void SendMessage(MouseEvent event) throws IOException {
        dos.writeUTF(userName + "<:>" + Send.getText());
        dos.flush();
        System.out.println("send message: " + Send.getText());
        Send.setText("");
    }
    Task Read = new Task<Void>() {
        @Override
        public Void call() throws Exception {
            while (true) {
                String msg = dis.readUTF();
                System.out.println("read message= " + msg);
                msg = new String(System.lineSeparator() + System.lineSeparator() + msg);
                Message.appendText(msg);
                Thread.sleep(100);
            }
        }
    };

    public void initialize(Stage stage, String userName, String Password) {

        
        stage.setTitle(userName);
        stage.setScene(scene);
        stage.hide();
        stage.show();
        this.userName = userName;
        this.Password = Password;
        try {
            dos.writeUTF("login<:>" + userName + "<:>" + Password);
            dos.flush();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread th = new Thread(Read);
        th.setDaemon(true);
       // th.start();
    }
}
