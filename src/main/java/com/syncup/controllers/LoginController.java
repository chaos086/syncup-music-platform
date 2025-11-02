package com.syncup.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.syncup.data.DataManager;
import com.syncup.models.Usuario;
import com.syncup.models.Admin;
import com.syncup.utils.StyleManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private ImageView logoImageView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button demoUserButton;
    @FXML private Button adminUserButton;
    @FXML private Label errorLabel;

    private DataManager dataManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        if (usernameField != null) usernameField.setOnAction(e -> handleLogin());
        if (passwordField != null) passwordField.setOnAction(e -> handleLogin());
    }

    @FXML private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) { showError("Ingresa usuario y contraseña"); return; }
        Usuario usuario = dataManager.authenticateUser(username, password);
        if (usuario != null) { hideError(); navigateToMainApp(usuario); }
        else { showError("Usuario o contraseña incorrectos"); }
    }

    // Nuevo: para que el FXML no falle si existe botón de registro
    @FXML private void handleRegister(ActionEvent e) {
        showError("Registro no implementado aún. Usa demo_user/demo123 o admin/admin123");
    }

    @FXML private void handleDemoLogin() { usernameField.setText("demo_user"); passwordField.setText("demo123"); handleLogin(); }
    @FXML private void handleAdminLogin() { usernameField.setText("admin"); passwordField.setText("admin123"); handleLogin(); }

    private void navigateToMainApp(Usuario usuario) {
        try {
            String fxml = usuario.isEsAdmin() ? "/fxml/admin-dashboard.fxml" : "/fxml/user-dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root;
            try { root = loader.load(); }
            catch (IOException ex) { root = createFallback(usuario); }
            Object controller = loader.getController();
            if (controller instanceof UserDashboardController && !usuario.isEsAdmin()) {
                ((UserDashboardController) controller).setCurrentUser(usuario);
            } else if (controller instanceof AdminDashboardController && usuario.isEsAdmin()) {
                ((AdminDashboardController) controller).setCurrentAdmin((Admin) usuario);
            }
            Scene scene = new Scene(root, 1200, 800);
            StyleManager.applySpotifyTheme(scene);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(usuario.isEsAdmin()?"SyncUp - Admin":"SyncUp");
            stage.centerOnScreen();
        } catch (Exception e) { showError("Error cargando UI: "+e.getMessage()); }
    }

    private Parent createFallback(Usuario usuario) {
        VBox box = new VBox();
        Label l = new Label("Bienvenido " + usuario.getUsername());
        box.getChildren().add(l);
        return box;
    }

    private void showError(String m) { if (errorLabel!=null) { errorLabel.setText(m); errorLabel.setStyle("-fx-text-fill:#E22134"); errorLabel.setVisible(true);} }
    private void hideError() { if (errorLabel!=null) errorLabel.setVisible(false); }
}
