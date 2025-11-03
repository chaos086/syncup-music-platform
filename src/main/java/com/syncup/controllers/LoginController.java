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

    Label help = new Label("Mín. 6 caracteres, 1 mayúscula, 1 número");
    help.getStyleClass().add("helper-text");

    VBox box = new VBox(8,
            new Label("Nombre"), name,
            new Label("Username"), user,
            new Label("Email"), email,
            new Label("Contraseña"), pass,
            help);
    pane.setContent(box);

    // Validación en vivo
    name.textProperty().addListener((o,ov,nv)-> toggleError(name, nv.isBlank()));
    user.textProperty().addListener((o,ov,nv)-> toggleError(user, nv.isBlank() || nv.contains(" ") || nv.length()<3));
    email.textProperty().addListener((o,ov,nv)-> toggleError(email, !nv.isBlank() && !nv.contains("@")));
    pass.textProperty().addListener((o,ov,nv)-> {
        boolean ok = isStrong(nv);
        toggleError(pass, !ok);
        help.getStyleClass().remove("error");
        if(!ok) help.getStyleClass().add("error");
    });

    // Deshabilitar OK si hay errores
    final Button okBtn = (Button) pane.lookupButton(ButtonType.OK);
    okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
        boolean err = name.getText().isBlank() || user.getText().isBlank() || user.getText().contains(" ") || user.getText().length()<3
                || (!email.getText().isBlank() && !email.getText().contains("@"))
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

private void toggleError(TextField tf, boolean error){
    if(error){ if(!tf.getStyleClass().contains("input-error")) tf.getStyleClass().add("input-error"); }
    else tf.getStyleClass().remove("input-error");
}

private boolean isStrong(String p){
    if(p==null || p.length()<6) return false;
    boolean hasUpper=false, hasDigit=false;
    for(char c: p.toCharArray()){ if(Character.isUpperCase(c)) hasUpper=true; if(Character.isDigit(c)) hasDigit=true; }
    return hasUpper && hasDigit;
}
