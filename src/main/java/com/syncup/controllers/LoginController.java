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
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private final UserRepository userRepo = new UserRepository();
    private DataManager dataManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        // Asegurar admin con rol persistido al inicio
        ensureAdminWithRole();
        if (usernameField != null) usernameField.setOnAction(e -> handleLogin());
        if (passwordField != null) passwordField.setOnAction(e -> handleLogin());
    }
    
    /**
     * Garantiza que el admin tenga el rol de administrador persistido
     */
    private void ensureAdminWithRole() {
        try {
            // Si admin existe pero no tiene rol, actualizar
            if (userRepo.findByUsername("admin").isPresent()) {
                userRepo.updateAdminRole("admin", true);
            }
        } catch (Exception e) {
            System.err.println("Error asegurando rol admin: " + e.getMessage());
        }
    }

    @FXML private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        if (user.isEmpty() || pass.isEmpty()) { showError("Ingresa usuario y contraseña"); return; }

        // SISTEMA MEJORADO: Verificación unificada con roles
        Usuario usuario = authenticateUserWithRole(user, pass);
        if (usuario != null) {
            hideError();
            navigateToMainApp(usuario);
        } else {
            showError("Usuario o contraseña incorrectos");
        }
    }
    
    /**
     * Método unificado de autenticación que resuelve roles correctamente
     */
    private Usuario authenticateUserWithRole(String username, String password) {
        // 1. Verificación de credenciales admin hardcodeadas (prioritario)
        if (userRepo.isAdminCredentials(username, password)) {
            // Buscar admin en persistencia o crear/actualizar
            Optional<Usuario> adminOpt = userRepo.findByUsername("admin");
            if (adminOpt.isPresent()) {
                Usuario admin = adminOpt.get();
                if (!admin.isEsAdmin()) {
                    admin.setEsAdmin(true);
                    userRepo.updateAdminRole("admin", true); // persistir rol
                }
                return admin;
            }
        }
        
        // 2. Autenticación normal contra persistencia
        if (userRepo.authenticate(username, password)) {
            return userRepo.findByUsernameOrEmail(username).orElse(null);
        }
        
        // 3. Fallback a DataManager (usuarios en memoria)
        return dataManager.authenticateUser(username, password);
    }

    @FXML private void handleRegister(ActionEvent e) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear cuenta");
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/css/themes/spotify-theme.css").toExternalForm());
        pane.getStyleClass().add("signup-dialog");
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField name = new TextField(); name.setPromptText("Nombre completo");
        TextField user = new TextField(); user.setPromptText("Username");
        TextField email = new TextField(); email.setPromptText("Email");
        PasswordField pass = new PasswordField(); pass.setPromptText("Contraseña");

        Label helpUser = new Label(""); helpUser.getStyleClass().add("helper-text");
        Label helpEmail = new Label(""); helpEmail.getStyleClass().add("helper-text");
        Label helpPass = new Label("Mín. 6 caracteres, 1 mayúscula, 1 número"); helpPass.getStyleClass().add("helper-text");

        VBox box = new VBox(8,
                new Label("Nombre"), name,
                new Label("Username"), user, helpUser,
                new Label("Email"), email, helpEmail,
                new Label("Contraseña"), pass, helpPass);
        pane.setContent(box);

        name.textProperty().addListener((o,ov,nv)-> toggleError(name, nv.isBlank()));
        user.textProperty().addListener((o,ov,nv)-> {
            boolean formatBad = nv.isBlank() || nv.contains(" ") || nv.length()<3;
            boolean duplicate = userRepo.findByUsername(nv).isPresent();
            toggleError(user, formatBad || duplicate);
            helpUser.setText( duplicate? "Username ya está en uso" : (formatBad? "Mín. 3, sin espacios" : "") );
            setErrorStyle(helpUser, formatBad || duplicate);
        });
        email.textProperty().addListener((o,ov,nv)-> {
            boolean fmt = !nv.isBlank() && !nv.contains("@");
            boolean duplicate = !nv.isBlank() && userRepo.findByEmail(nv).isPresent();
            toggleError(email, fmt || duplicate);
            helpEmail.setText( duplicate? "Email ya está registrado" : (fmt? "Formato inválido" : "") );
            setErrorStyle(helpEmail, fmt || duplicate);
        });
        pass.textProperty().addListener((o,ov,nv)-> {
            boolean ok = isStrong(nv);
            toggleError(pass, !ok);
            helpPass.setText("Mín. 6, 1 mayúscula, 1 número" + (ok? "" : " — no cumple"));
            setErrorStyle(helpPass, !ok);
        });

        final Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            boolean err = name.getText().isBlank()
                    || user.getText().isBlank() || user.getText().contains(" ") || user.getText().length()<3 || userRepo.findByUsername(user.getText()).isPresent()
                    || (!email.getText().isBlank() && (!email.getText().contains("@") || userRepo.findByEmail(email.getText()).isPresent()))
                    || !isStrong(pass.getText());
            if(err){ ev.consume(); }
        });

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    userRepo.create(name.getText(), user.getText(), email.getText(), pass.getText(), false); // usuarios normales
                    showError("Cuenta creada correctamente. Ya puedes iniciar sesión.");
                } catch (IllegalStateException dup) { showError(dup.getMessage()); }
                catch (IllegalArgumentException bad) { showError(bad.getMessage()); }
                catch (Exception ex) { showError("Error creando usuario"); }
            }
        });
    }

    private void toggleError(TextField tf, boolean error){
        if(error){ if(!tf.getStyleClass().contains("input-error")) tf.getStyleClass().add("input-error"); }
        else tf.getStyleClass().remove("input-error");
    }

    private void setErrorStyle(Label l, boolean error){ l.getStyleClass().remove("error"); if(error) l.getStyleClass().add("error"); }

    private boolean isStrong(String p){ if(p==null || p.length()<6) return false; boolean hasUpper=false, hasDigit=false; for(char c: p.toCharArray()){ if(Character.isUpperCase(c)) hasUpper=true; if(Character.isDigit(c)) hasDigit=true; } return hasUpper && hasDigit; }

    private void navigateToMainApp(Usuario usuario) { 
        try { 
            String fxml = usuario.isEsAdmin() ? "/fxml/admin-dashboard.fxml" : "/fxml/user-dashboard.fxml"; 
            System.out.println("🔍 Navegando a: " + fxml + " (esAdmin: " + usuario.isEsAdmin() + ", user: " + usuario.getUsername() + ")");
            
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
            } catch (IOException ex) { 
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

    private Parent createFallback(Usuario usuario) { VBox box = new VBox(10); box.setStyle("-fx-padding: 20; -fx-alignment: center;"); Label titulo = new Label("Bienvenido " + (usuario.getUsername()!=null?usuario.getUsername():"usuario")); titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"); Label info = new Label(usuario.isEsAdmin()?"Panel de Administrador (Fallback)":"Panel de Usuario (Fallback)"); Button logout = new Button("Cerrar Sesión"); logout.setOnAction(e -> { try { FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/login.fxml")); Parent loginRoot = loginLoader.load(); Scene loginScene = new Scene(loginRoot, 1200, 800); StyleManager.applySpotifyTheme(loginScene); Stage stage = (Stage) ((Button)e.getSource()).getScene().getWindow(); stage.setScene(loginScene); stage.setTitle("SyncUp - Login"); } catch (Exception ex) { System.err.println("Error volviendo a login: " + ex); } }); box.getChildren().addAll(titulo, info, logout); return box; }

    private void showError(String m) { if (errorLabel!=null) { errorLabel.setText(m); errorLabel.setStyle("-fx-text-fill:#E22134"); errorLabel.setVisible(true);} }
    private void hideError() { if (errorLabel!=null) errorLabel.setVisible(false); }
}
