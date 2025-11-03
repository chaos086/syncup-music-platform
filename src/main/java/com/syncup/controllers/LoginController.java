@FXML private void handleRegister(ActionEvent e) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Crear cuenta");
    DialogPane pane = dialog.getDialogPane();
    pane.getStylesheets().add(getClass().getResource("/css/spotify-theme.css").toExternalForm());
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

    // Validación en vivo
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

private void setErrorStyle(Label l, boolean error){
    l.getStyleClass().remove("error");
    if(error) l.getStyleClass().add("error");
}
