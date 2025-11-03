package com.syncup.controllers;

import com.syncup.data.DataManager;
import com.syncup.data.UserRepository;
import com.syncup.models.Usuario;
import com.syncup.utils.StyleManager;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML private ImageView logoImageView;
    @FXML private TextField usernameField; // login: username o email
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    // Persistencia JSON de usuarios nuevos
    private final UserRepository userRepo = new UserRepository();

    // Mantengo DataManager para compatibilidad con usuarios existentes
    private DataManager dataManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        if (usernameField != null) usernameField.setOnAction(e -> handleLogin());
        if (passwordField != null) passwordField.setOnAction(e -> handleLogin());
    }

    @FXML private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) { showError("Ingresa usuario y contraseña"); return; }

        // 1) Intentar autenticar por nuevo repositorio (username o email)
        if (userRepo.authenticate(user, pass)) {
            navigateToMainAppSimple();
            return;
        }
        // 2) Compatibilidad: autenticación previa
        Usuario usuario = dataManager.authenticateUser(user, pass);
        if (usuario != null) { hideError(); navigateToMainApp(usuario); }
        else { showError("Usuario o contraseña incorrectos"); }
    }

    @FXML private void handleRegister(ActionEvent e) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear cuenta");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField name = new TextField(); name.setPromptText("Nombre completo");
        TextField user = new TextField(); user.setPromptText("Username");
        TextField email = new TextField(); email.setPromptText("Email");
        PasswordField pass = new PasswordField(); pass.setPromptText("Contraseña");
        VBox box = new VBox(8, new Label("Nombre"), name, new Label("Username"), user, new Label("Email"), email, new Label("Contraseña"), pass);
        pane.setContent(box);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    userRepo.create(name.getText(), user.getText(), email.getText(), pass.getText());
                    showError("Cuenta creada correctamente. Ya puedes iniciar sesión.");
                } catch (IllegalStateException dup) {
                    showError(dup.getMessage());
                } catch (IllegalArgumentException bad) {
                    showError(bad.getMessage());
                } catch (Exception ex) {
                    showError("Error creando usuario");
                }
            }
        });
    }

    private void navigateToMainAppSimple() {
        try {
            String fxml = "/fxml/user-dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            StyleManager.applySpotifyTheme(scene);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Usuario");
            stage.centerOnScreen();
        } catch (Exception ex) {
            System.err.println("Error cargando UI: " + ex);
            showError("Error cargando UI: " + ex.getMessage());
        }
    }

    private void navigateToMainApp(Usuario usuario) {
        try {
            String fxml = usuario.isEsAdmin() ? "/fxml/admin-dashboard.fxml" : "/fxml/user-dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root;
            try { 
                root = loader.load(); 
                Object controller = loader.getController();
                if (controller instanceof UserDashboardController && !usuario.isEsAdmin()) {
                    ((UserDashboardController) controller).setCurrentUser(usuario);
                } else if (controller instanceof AdminDashboardController && usuario.isEsAdmin()) {
                    ((AdminDashboardController) controller).setCurrentUser(usuario);
                }
            }
            catch (IOException ex) { 
                System.err.println("FXML no encontrado: " + fxml + " - usando fallback");
                root = createFallback(usuario); 
            }
            Scene scene = new Scene(root, 1200, 800);
            StyleManager.applySpotifyTheme(scene);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(usuario.isEsAdmin()?"SyncUp - Admin":"SyncUp - Usuario");
            stage.centerOnScreen();
        } catch (Exception e1) { 
            System.err.println("Error completo: " + e1);
            showError("Error cargando UI: "+e1.getMessage()); 
        }
    }

    private Parent createFallback(Usuario usuario) {
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Label titulo = new Label("Bienvenido " + (usuario.getUsername()!=null?usuario.getUsername():"usuario"));
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label info = new Label(usuario.isEsAdmin()?"Panel de Administrador (Fallback)":"Panel de Usuario (Fallback)");
        Button logout = new Button("Cerrar Sesión");
        logout.setOnAction(e -> {
            try {
                FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent loginRoot = loginLoader.load();
                Scene loginScene = new Scene(loginRoot, 1200, 800);
                StyleManager.applySpotifyTheme(loginScene);
                Stage stage = (Stage) ((Button)e.getSource()).getScene().getWindow();
                stage.setScene(loginScene);
                stage.setTitle("SyncUp - Login");
            } catch (Exception ex) { System.err.println("Error volviendo a login: " + ex); }
        });
        box.getChildren().addAll(titulo, info, logout);
        return box;
    }

    private void showError(String m) { if (errorLabel!=null) { errorLabel.setText(m); errorLabel.setStyle("-fx-text-fill:#E22134"); errorLabel.setVisible(true);} }
    private void hideError() { if (errorLabel!=null) errorLabel.setVisible(false); }
}
