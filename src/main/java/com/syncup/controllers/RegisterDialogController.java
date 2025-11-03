package com.syncup.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import com.syncup.data.DataManager;

import java.util.regex.Pattern;

public class RegisterDialogController {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    public static void showRegisterDialog(DataManager dataManager) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("🎵 Crear Cuenta Nueva");
        dialog.setHeaderText("Completa todos los campos para registrarte en SyncUp");

        ButtonType crearBtn = new ButtonType("✓ Crear Cuenta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearBtn, ButtonType.CANCEL);

        // Campos con validación visual
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField nombre = new TextField();
        TextField email = new TextField();
        
        // Configurar campos
        username.setPromptText("👤 Usuario (sin espacios)");
        password.setPromptText("🔒 Contraseña (mínimo 4 caracteres)");
        nombre.setPromptText("📝 Nombre completo");
        email.setPromptText("📧 Email válido");
        
        // Tooltips
        username.setTooltip(new Tooltip("Nombre de usuario único, sin espacios ni caracteres especiales"));
        password.setTooltip(new Tooltip("Contraseña de al menos 4 caracteres"));
        nombre.setTooltip(new Tooltip("Tu nombre completo como aparecerá en el perfil"));
        email.setTooltip(new Tooltip("Dirección de email válida para contacto"));
        
        // Labels de error
        Label usernameError = new Label();
        Label passwordError = new Label();
        Label nombreError = new Label();
        Label emailError = new Label();
        
        // Estilo de errores
        String errorStyle = "-fx-text-fill: #FF6B6B; -fx-font-size: 11px;";
        usernameError.setStyle(errorStyle);
        passwordError.setStyle(errorStyle);
        nombreError.setStyle(errorStyle);
        emailError.setStyle(errorStyle);
        
        // Grid layout
        GridPane grid = new GridPane();
        grid.setHgap(10); 
        grid.setVgap(8);
        grid.addRow(0, new Label("Usuario:"), username, usernameError);
        grid.addRow(1, new Label("Contraseña:"), password, passwordError);
        grid.addRow(2, new Label("Nombre:"), nombre, nombreError);
        grid.addRow(3, new Label("Email:"), email, emailError);
        
        GridPane.setHgrow(username, Priority.ALWAYS);
        GridPane.setHgrow(password, Priority.ALWAYS);
        GridPane.setHgrow(nombre, Priority.ALWAYS);
        GridPane.setHgrow(email, Priority.ALWAYS);
        
        dialog.getDialogPane().setContent(grid);
        
        // Botón crear inicialmente deshabilitado
        Button crearButton = (Button) dialog.getDialogPane().lookupButton(crearBtn);
        crearButton.setDisable(true);
        
        // Validación en tiempo real
        Runnable validarFormulario = () -> {
            boolean valid = true;
            
            // Validar usuario
            String userText = username.getText().trim();
            if (userText.isEmpty()) {
                username.setStyle("-fx-border-color: #FF6B6B;");
                usernameError.setText("⚠ Requerido");
                valid = false;
            } else if (userText.contains(" ")) {
                username.setStyle("-fx-border-color: #FF6B6B;");
                usernameError.setText("⚠ Sin espacios");
                valid = false;
            } else {
                username.setStyle("-fx-border-color: #1DB954;");
                usernameError.setText("✓");
                usernameError.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px;");
            }
            
            // Validar contraseña
            String passText = password.getText();
            if (passText.length() < 4) {
                password.setStyle("-fx-border-color: #FF6B6B;");
                passwordError.setText("⚠ Mínimo 4 caracteres");
                valid = false;
            } else {
                password.setStyle("-fx-border-color: #1DB954;");
                passwordError.setText("✓");
                passwordError.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px;");
            }
            
            // Validar nombre
            String nameText = nombre.getText().trim();
            if (nameText.isEmpty()) {
                nombre.setStyle("-fx-border-color: #FF6B6B;");
                nombreError.setText("⚠ Requerido");
                valid = false;
            } else {
                nombre.setStyle("-fx-border-color: #1DB954;");
                nombreError.setText("✓");
                nombreError.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px;");
            }
            
            // Validar email
            String emailText = email.getText().trim();
            if (emailText.isEmpty()) {
                email.setStyle("-fx-border-color: #FF6B6B;");
                emailError.setText("⚠ Requerido");
                valid = false;
            } else if (!EMAIL_PATTERN.matcher(emailText).matches()) {
                email.setStyle("-fx-border-color: #FF6B6B;");
                emailError.setText("⚠ Email inválido");
                valid = false;
            } else {
                email.setStyle("-fx-border-color: #1DB954;");
                emailError.setText("✓");
                emailError.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px;");
            }
            
            crearButton.setDisable(!valid);
        };
        
        // Listeners para validación en tiempo real
        username.textProperty().addListener((obs, old, newVal) -> validarFormulario.run());
        password.textProperty().addListener((obs, old, newVal) -> validarFormulario.run());
        nombre.textProperty().addListener((obs, old, newVal) -> validarFormulario.run());
        email.textProperty().addListener((obs, old, newVal) -> validarFormulario.run());
        
        dialog.setResultConverter(bt -> {
            if (bt == crearBtn) {
                boolean ok = dataManager.createUser(
                    username.getText().trim(), 
                    password.getText().trim(), 
                    nombre.getText().trim(), 
                    email.getText().trim()
                );
                if (!ok) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error de Registro");
                    errorAlert.setHeaderText("No se pudo crear el usuario");
                    errorAlert.setContentText("El nombre de usuario ya existe o hay un error en los datos. Inténtalo con un usuario diferente.");
                    errorAlert.showAndWait();
                    return false;
                }
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("✓ Registro Exitoso");
                successAlert.setHeaderText("¡Cuenta creada correctamente!");
                successAlert.setContentText("Ya puedes iniciar sesión con tu usuario \"" + username.getText() + "\" y tu contraseña.");
                successAlert.showAndWait();
                return true;
            }
            return false;
        });

        dialog.showAndWait();
    }
}
