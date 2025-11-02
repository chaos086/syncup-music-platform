package com.syncup.controllers;

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

/**
 * Controlador para la pantalla de login del sistema SyncUp.
 * Maneja la autenticación de usuarios y navegación a las pantallas principales.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class LoginController implements Initializable {
    
    @FXML private ImageView logoImageView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button demoUserButton;
    @FXML private Button adminUserButton;
    @FXML private Label errorLabel;
    
    /** Gestor de datos del sistema */
    private DataManager dataManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        
        // Configurar eventos de teclado
        setupKeyboardEvents();
        
        // Configurar tooltips
        setupTooltips();
        
        System.out.println("LoginController inicializado correctamente");
    }
    
    /**
     * Configura eventos de teclado para mejor UX.
     */
    private void setupKeyboardEvents() {
        // Enter en cualquier campo ejecuta login
        if (usernameField != null) usernameField.setOnAction(e -> handleLogin());
        if (passwordField != null) passwordField.setOnAction(e -> handleLogin());
    }
    
    /**
     * Configura tooltips para los botones.
     */
    private void setupTooltips() {
        if (demoUserButton != null) {
            demoUserButton.setTooltip(new Tooltip("Usuario: demo_user\nContraseña: demo123"));
        }
        if (adminUserButton != null) {
            adminUserButton.setTooltip(new Tooltip("Usuario: admin\nContraseña: admin123"));
        }
    }
    
    /**
     * Maneja el evento de login principal.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validar campos
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, ingresa usuario y contraseña.");
            return;
        }
        
        // Intentar autenticar
        try {
            Usuario usuario = dataManager.authenticateUser(username, password);
            
            if (usuario != null) {
                // Login exitoso
                hideError();
                navigateToMainApp(usuario);
            } else {
                showError("Usuario o contraseña incorrectos.");
                passwordField.clear();
                usernameField.requestFocus();
            }
        } catch (Exception e) {
            showError("Error al iniciar sesión: " + e.getMessage());
            System.err.println("Error en login: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el evento de registro de nuevo usuario.
     */
    @FXML
    private void handleRegister() {
        try {
            showRegisterDialog();
        } catch (Exception e) {
            showError("Error al abrir registro: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el login del usuario demo.
     */
    @FXML
    private void handleDemoLogin() {
        usernameField.setText("demo_user");
        passwordField.setText("demo123");
        handleLogin();
    }
    
    /**
     * Maneja el login del usuario administrador.
     */
    @FXML
    private void handleAdminLogin() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        handleLogin();
    }
    
    /**
     * Navega a la aplicación principal después del login exitoso.
     */
    private void navigateToMainApp(Usuario usuario) {
        try {
            String fxmlFile;
            String windowTitle;
            
            // Determinar qué pantalla cargar según el tipo de usuario
            if (usuario.isEsAdmin()) {
                fxmlFile = "/fxml/admin-dashboard.fxml";
                windowTitle = "SyncUp - Panel de Administrador";
            } else {
                fxmlFile = "/fxml/user-dashboard.fxml";
                windowTitle = "SyncUp - " + usuario.getNombreCompleto();
            }
            
            // Cargar la nueva pantalla
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root;
            
            try {
                root = loader.load();
                
                // Pasar el usuario al controlador de la nueva pantalla
                Object controller = loader.getController();
                if (controller instanceof UserDashboardController && !usuario.isEsAdmin()) {
                    ((UserDashboardController) controller).setCurrentUser(usuario);
                } else if (controller instanceof AdminDashboardController && usuario.isEsAdmin()) {
                    ((AdminDashboardController) controller).setCurrentAdmin((Admin) usuario);
                }
                
            } catch (IOException e) {
                // Si no se encuentra el FXML específico, cargar pantalla placeholder
                System.out.println("FXML no encontrado (" + fxmlFile + "), cargando dashboard simple");
                root = createSimpleDashboard(usuario);
                windowTitle = "SyncUp - Dashboard Simple";
            }
            
            // Crear nueva escena
            Scene scene = new Scene(root, 1200, 800);
            
            // Aplicar tema Spotify
            StyleManager.applySpotifyTheme(scene);
            
            // Obtener ventana actual
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Configurar nueva escena
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
            
            System.out.println("Navegación exitosa para usuario: " + usuario.getUsername());
            
        } catch (Exception e) {
            showError("Error al cargar la aplicación principal: " + e.getMessage());
            System.err.println("Error en navegación: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crea un dashboard simple cuando no se encuentran los archivos FXML.
     */
    private Parent createSimpleDashboard(Usuario usuario) {
        VBox dashboard = new VBox(30);
        dashboard.setAlignment(javafx.geometry.Pos.CENTER);
        dashboard.setStyle("-fx-background-color: #191414; -fx-padding: 50;");
        
        Label welcomeLabel = new Label("¡Bienvenido a SyncUp, " + usuario.getUsername() + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label typeLabel = new Label(usuario.isEsAdmin() ? "Perfil: Administrador" : "Perfil: Usuario Estándar");
        typeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1DB954;");
        
        Label infoLabel = new Label(
            "Dashboard completamente funcional disponible cuando:\n" +
            "1. Ejecutes desde IntelliJ IDEA\n" +
            "2. Los archivos FXML estén correctamente ubicados\n" +
            "3. Las dependencias JavaFX estén configuradas\n\n" +
            "El sistema backend está completamente operativo.\n" +
            "Datos cargados: " + dataManager.getAllCanciones().size() + " canciones, " + 
            dataManager.getAllUsuarios().size() + " usuarios"
        );
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #B3B3B3; -fx-text-alignment: center;");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(600);
        
        // Botones de acción básica
        Button testSystemButton = new Button("Probar Sistema");
        testSystemButton.setStyle("-fx-background-color: #1DB954; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        testSystemButton.setOnAction(e -> {
            // Mostrar algunas estadísticas del sistema
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Estado del Sistema");
            alert.setHeaderText("Sistema SyncUp - Estado Operativo");
            alert.setContentText(dataManager.getSystemStats());
            alert.showAndWait();
        });
        
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setStyle("-fx-background-color: #E22134; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        logoutButton.setOnAction(e -> {
            try {
                // Volver al login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 1200, 800);
                StyleManager.applySpotifyTheme(scene);
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("SyncUp - Iniciar Sesión");
            } catch (IOException ex) {
                System.err.println("Error volviendo al login: " + ex.getMessage());
            }
        });
        
        dashboard.getChildren().addAll(welcomeLabel, typeLabel, infoLabel, testSystemButton, logoutButton);
        
        return dashboard;
    }
    
    /**
     * Muestra un diálogo de registro de usuario.
     */
    private void showRegisterDialog() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Crear Nueva Cuenta");
        dialog.setHeaderText("Registro de Usuario en SyncUp");
        
        ButtonType registerButtonType = new ButtonType("Registrarse", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Usuario");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Contraseña");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Nombre Completo");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(newUsernameField, 1, 0);
        grid.add(new Label("Contraseña:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Nombre Completo:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Converter para crear usuario
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                try {
                    String username = newUsernameField.getText().trim();
                    String password = newPasswordField.getText();
                    String fullName = fullNameField.getText().trim();
                    String email = emailField.getText().trim();
                    
                    if (username.isEmpty() || password.isEmpty()) {
                        showError("Usuario y contraseña son obligatorios.");
                        return null;
                    }
                    
                    // Verificar que el usuario no exista
                    if (dataManager.getUsuarioByUsername(username) != null) {
                        showError("El usuario ya existe.");
                        return null;
                    }
                    
                    // Crear nuevo usuario
                    Usuario newUser = new Usuario(username, password);
                    newUser.setNombreCompleto(fullName.isEmpty() ? username : fullName);
                    newUser.setEmail(email);
                    
                    return newUser;
                } catch (Exception e) {
                    showError("Error creando usuario: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Mostrar diálogo y procesar resultado
        dialog.showAndWait().ifPresent(usuario -> {
            if (dataManager.addUsuario(usuario)) {
                showInfo("Usuario creado exitosamente. Puedes iniciar sesión ahora.");
                usernameField.setText(usuario.getUsername());
                passwordField.requestFocus();
            } else {
                showError("No se pudo crear el usuario.");
            }
        });
    }
    
    /**
     * Muestra un mensaje de error.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setStyle("-fx-text-fill: #FF6B6B;");
        }
    }
    
    /**
     * Muestra un mensaje de información.
     */
    private void showInfo(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setStyle("-fx-text-fill: #1DB954;");
        }
    }
    
    /**
     * Oculta el mensaje de error.
     */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }
}