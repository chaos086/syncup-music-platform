package com.syncup.utils;

import javafx.scene.Scene;

public class StyleManager {
    private static final String MAIN_STYLESHEET = "/css/spotify-theme.css";

    public static void applySpotifyTheme(Scene scene) {
        if (scene == null) return;
        try {
            String url = StyleManager.class.getResource(MAIN_STYLESHEET) != null
                    ? StyleManager.class.getResource(MAIN_STYLESHEET).toExternalForm()
                    : null;
            if (url != null) {
                scene.getStylesheets().add(url);
            } else {
                scene.getRoot().setStyle("-fx-background-color:#191414;-fx-text-fill:white;-fx-font-family:Arial;");
            }
        } catch (Exception e) {
            scene.getRoot().setStyle("-fx-background-color:#191414;-fx-text-fill:white;-fx-font-family:Arial;");
        }
    }
}
