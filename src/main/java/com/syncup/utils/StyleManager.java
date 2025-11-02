package com.syncup.utils;

import javafx.scene.Scene;

/**
 * Gestor de estilos CSS para la aplicación SyncUp.
 * Aplica temas visuales consistentes en toda la aplicación.
 * RF-028: Interface JavaFX moderna con tema tipo Spotify
 * COMPATIBLE CON JAVA 11 - Sin text blocks
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class StyleManager {
    
    /** Ruta del archivo CSS principal */
    private static final String MAIN_STYLESHEET = "/css/spotify-theme.css";
    
    /** CSS por defecto embebido (JAVA 11 COMPATIBLE) */
    private static final String DEFAULT_CSS = 
        ".root { " +
        "-fx-background-color: #191414; " +
        "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
        "} " +
        ".app-title { " +
        "-fx-font-size: 28px; " +
        "-fx-font-weight: bold; " +
        "-fx-text-fill: white; " +
        "} " +
        ".app-subtitle { " +
        "-fx-font-size: 14px; " +
        "-fx-text-fill: #B3B3B3; " +
        "} " +
        ".login-container { " +
        "-fx-background-color: linear-gradient(135deg, #1DB954 0%, #1ed760 100%); " +
        "-fx-padding: 40px; " +
        "} " +
        ".login-box { " +
        "-fx-background-color: #000000; " +
        "-fx-background-radius: 8px; " +
        "-fx-padding: 40px; " +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0); " +
        "} " +
        ".login-field { " +
        "-fx-background-color: #404040; " +
        "-fx-text-fill: white; " +
        "-fx-prompt-text-fill: #B3B3B3; " +
        "-fx-background-radius: 4px; " +
        "-fx-border-color: transparent; " +
        "-fx-padding: 12px; " +
        "-fx-font-size: 14px; " +
        "} " +
        ".login-field:focused { " +
        "-fx-border-color: #1DB954; " +
        "-fx-border-width: 2px; " +
        "} " +
        ".primary-button { " +
        "-fx-background-color: #1DB954; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 14px; " +
        "-fx-background-radius: 50px; " +
        "-fx-padding: 12px 32px; " +
        "-fx-border-color: transparent; " +
        "-fx-cursor: hand; " +
        "} " +
        ".primary-button:hover { " +
        "-fx-background-color: #1ed760; " +
        "-fx-scale-x: 1.04; " +
        "-fx-scale-y: 1.04; " +
        "} " +
        ".secondary-button { " +
        "-fx-background-color: transparent; " +
        "-fx-text-fill: white; " +
        "-fx-border-color: #404040; " +
        "-fx-border-width: 1px; " +
        "-fx-border-radius: 4px; " +
        "-fx-background-radius: 4px; " +
        "-fx-padding: 8px 16px; " +
        "} " +
        ".secondary-button:hover { " +
        "-fx-background-color: #404040; " +
        "} " +
        ".danger-button { " +
        "-fx-background-color: #E22134; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-background-radius: 4px; " +
        "-fx-padding: 8px 16px; " +
        "} " +
        ".content-panel { " +
        "-fx-background-color: #191414; " +
        "-fx-padding: 20px; " +
        "} " +
        ".section-title { " +
        "-fx-font-size: 20px; " +
        "-fx-font-weight: bold; " +
        "-fx-text-fill: white; " +
        "} " +
        ".section-subtitle { " +
        "-fx-font-size: 14px; " +
        "-fx-text-fill: #B3B3B3; " +
        "} " +
        ".field-label { " +
        "-fx-text-fill: white; " +
        "-fx-font-size: 12px; " +
        "} " +
        ".success-message { " +
        "-fx-text-fill: #1DB954; " +
        "-fx-font-size: 12px; " +
        "} " +
        ".metric-card { " +
        "-fx-background-color: #282828; " +
        "-fx-background-radius: 8px; " +
        "-fx-padding: 20px; " +
        "-fx-min-width: 120px; " +
        "} " +
        ".metric-number { " +
        "-fx-font-size: 32px; " +
        "-fx-font-weight: bold; " +
        "-fx-text-fill: #1DB954; " +
        "} " +
        ".metric-label { " +
        "-fx-font-size: 12px; " +
        "-fx-text-fill: #B3B3B3; " +
        "} " +
        ".table-view { " +
        "-fx-background-color: #191414; " +
        "} " +
        ".table-view .column-header-background { " +
        "-fx-background-color: #282828; " +
        "} " +
        ".table-view .column-header { " +
        "-fx-background-color: transparent; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "} " +
        ".table-view .table-cell { " +
        "-fx-background-color: #191414; " +
        "-fx-text-fill: #B3B3B3; " +
        "-fx-border-color: transparent; " +
        "} " +
        ".table-row-cell:selected { " +
        "-fx-background-color: #1DB954; " +
        "} " +
        ".tab-pane { " +
        "-fx-tab-min-height: 40px; " +
        "} " +
        ".tab { " +
        "-fx-background-color: #282828; " +
        "-fx-text-fill: #B3B3B3; " +
        "-fx-font-weight: bold; " +
        "} " +
        ".tab:selected { " +
        "-fx-background-color: #1DB954; " +
        "-fx-text-fill: white; " +
        "}";
    
    /**
     * Aplica el tema principal de Spotify a una escena.
     * 
     * @param scene Escena a la que aplicar el tema
     */
    public static void applySpotifyTheme(Scene scene) {
        if (scene == null) {
            System.err.println("Error: Scene es null, no se pueden aplicar estilos");
            return;
        }
        
        try {
            // Intentar cargar CSS desde archivo
            String styleURL = StyleManager.class.getResource(MAIN_STYLESHEET) != null ? 
                            StyleManager.class.getResource(MAIN_STYLESHEET).toExternalForm() : null;
            if (styleURL != null) {
                scene.getStylesheets().add(styleURL);
                System.out.println("Aplicado: " + MAIN_STYLESHEET);
            } else {
                // Usar CSS embebido como fallback
                applyEmbeddedCSS(scene);
                System.out.println("No se encontró " + MAIN_STYLESHEET + " - usando estilos por defecto");
            }
        } catch (Exception e) {
            // En caso de error, aplicar CSS embebido
            applyEmbeddedCSS(scene);
            System.err.println("Error cargando " + MAIN_STYLESHEET + ": " + e.getMessage());
        }
    }
    
    /**
     * Aplica CSS embebido directamente a la escena.
     * 
     * @param scene Escena objetivo
     */
    private static void applyEmbeddedCSS(Scene scene) {
        try {
            scene.getRoot().setStyle(DEFAULT_CSS);
        } catch (Exception e) {
            System.err.println("Error aplicando CSS embebido: " + e.getMessage());
        }
    }
    
    /**
     * Aplica estilos por defecto cuando no se pueden cargar los archivos CSS.
     * 
     * @param scene Escena objetivo  
     */
    public static void applyDefaultStyles(Scene scene) {
        if (scene == null) return;
        
        // Estilo mínimo para que la aplicación se vea decente
        String minimalCSS = 
            "-fx-background-color: #191414; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Arial';";
            
        try {
            scene.getRoot().setStyle(minimalCSS);
            System.out.println("Estilos por defecto aplicados");
        } catch (Exception e) {
            System.err.println("Error aplicando estilos por defecto: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene el color principal de Spotify como string hex.
     * 
     * @return Color verde de Spotify (#1DB954)
     */
    public static String getSpotifyGreen() {
        return "#1DB954";
    }
    
    /**
     * Obtiene el color de fondo principal de Spotify.
     * 
     * @return Color negro de Spotify (#191414)
     */
    public static String getSpotifyBlack() {
        return "#191414";
    }
}