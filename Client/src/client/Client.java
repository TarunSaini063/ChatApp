package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author tarun
 */
public class Client extends Application {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        while (true) { //Waiting for Server
            try {
                ClientBackendController.s = new Socket("localhost", 1234); //Client Socket is Created
                if (ClientBackendController.s != null) {
                    System.out.println("Client Connected");
                    break;
                }
            } catch (IOException e) {
                System.out.println("Waiting for Cleint");
                Thread.sleep(1000);
            }
        }
        ClientBackendController.dis = new DataInputStream(ClientBackendController.s.getInputStream()); //DataInputStream of Current client
        ClientBackendController.dos = new DataOutputStream(ClientBackendController.s.getOutputStream());//DataOutputStream of Current client
        System.out.println("Launching Message Layout in CHATAPP_Client");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        LoginController loginController = new LoginController(); //After Connection is Successfull with server than login page is open
        loginController.initialize(stage);    //initialize javafx elements
    }

}
