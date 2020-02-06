/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author tarun
 */
public class LoginController {

    private Parent parent;
    private Scene scene;
    private Stage stage;
    private ClientBackendController clientBackendController = null;
    @FXML
    private TextField username;
    @FXML
    private TextField password;

    public LoginController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Login.fxml"));
        fxmlLoader.setController(this);
        try {
            parent = (Parent) fxmlLoader.load();
            scene = new Scene(parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void login(ActionEvent event) throws IOException {
        if (username.getText().trim().length() > 0 && password.getText().trim().length() > 0) {
            clientBackendController = new ClientBackendController();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
            clientBackendController.initialize(stage, username.getText().trim(), password.getText().trim());
        }
    }

    public void initialize(Stage stage) {

        this.stage = stage;
        stage.setTitle("User Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.hide();
        stage.show();
    }

}