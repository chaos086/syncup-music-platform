package com.syncup.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import com.syncup.data.DataManager;

public class RegisterDialogController {
    public static void showRegisterDialog(DataManager dataManager) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Crear Cuenta Nueva");
        dialog.setHeaderText("Completa los datos para registrarte");

        ButtonType crearBtn = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearBtn, ButtonType.CANCEL);

        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField nombre = new TextField();
        TextField email = new TextField();
        username.setPromptText("Usuario");
        password.setPromptText("Contraseña");
        nombre.setPromptText("Nombre completo");
        email.setPromptText("Email");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Usuario"), username);
        grid.addRow(1, new Label("Contraseña"), password);
        grid.addRow(2, new Label("Nombre"), nombre);
        grid.addRow(3, new Label("Email"), email);
        GridPane.setHgrow(username, Priority.ALWAYS);
        GridPane.setHgrow(password, Priority.ALWAYS);
        GridPane.setHgrow(nombre, Priority.ALWAYS);
        GridPane.setHgrow(email, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == crearBtn) {
                boolean ok = dataManager.createUser(username.getText(), password.getText(), nombre.getText(), email.getText());
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "No se pudo crear el usuario. Verifica los campos o si el usuario ya existe.").showAndWait();
                    return false;
                }
                new Alert(Alert.AlertType.INFORMATION, "Usuario creado. Inicia sesión con tus credenciales.").showAndWait();
                return true;
            }
            return false;
        });

        dialog.showAndWait();
    }
}
