package com.syncup.utils;

import javafx.scene.Scene;

/**
 * Gestor de estilos CSS para la aplicación SyncUp.
 * Aplica el tema inspirado en Spotify con colores y estilos característicos.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class StyleManager {
    
    /** Ruta al archivo CSS principal del tema Spotify */
    private static final String SPOTIFY_THEME_CSS = "/css/spotify-theme.css";
    
    /** Ruta al archivo CSS de componentes */
    private static final String COMPONENTS_CSS = "/css/components.css";
    
    /** Ruta al archivo CSS de animaciones */
    private static final String ANIMATIONS_CSS = "/css/animations.css";
    
    /**
     * Aplica el tema Spotify completo a una escena.
     * 
     * @param scene Escena a la que aplicar el tema
     */
    public static void applySpotifyTheme(Scene scene) {
        if (scene == null) {
            System.err.println("Scene es null, no se puede aplicar el tema");
            return;
        }
        
        try {
            // Limpiar estilos existentes
            scene.getStylesheets().clear();
            
            // Aplicar hojas de estilo en orden
            applyStylesheet(scene, SPOTIFY_THEME_CSS);
            applyStylesheet(scene, COMPONENTS_CSS);
            applyStylesheet(scene, ANIMATIONS_CSS);
            
            System.out.println("Tema Spotify aplicado exitosamente");
            
        } catch (Exception e) {
            System.err.println("Error aplicando tema Spotify: " + e.getMessage());
            // Aplicar estilos por defecto en caso de error
            applyDefaultStyles(scene);
        }
    }
    
    /**
     * Aplica una hoja de estilos específica a la escena.
     * 
     * @param scene Escena objetivo
     * @param stylesheetPath Ruta de la hoja de estilos
     */
    private static void applyStylesheet(Scene scene, String stylesheetPath) {
        try {
            String styleURL = StyleManager.class.getResource(stylesheetPath)?.toExternalForm();
            if (styleURL != null) {
                scene.getStylesheets().add(styleURL);
                System.out.println("Aplicado: " + stylesheetPath);
            } else {
                System.out.println("No se encontró: " + stylesheetPath + " (se usarán estilos por defecto)");
            }
        } catch (Exception e) {
            System.err.println("Error cargando " + stylesheetPath + ": " + e.getMessage());
        }
    }
    
    /**
     * Aplica estilos por defecto cuando no se pueden cargar los archivos CSS.
     * 
     * @param scene Escena a la que aplicar estilos por defecto
     */
    private static void applyDefaultStyles(Scene scene) {
        String defaultStyles = generateDefaultSpotifyCSS();
        
        // Crear un archivo CSS temporal en memoria
        try {
            scene.getRoot().setStyle(
                "-fx-background-color: #191414;" +
                "-fx-text-fill: white;"
            );
            System.out.println("Estilos por defecto aplicados");
        } catch (Exception e) {
            System.err.println("Error aplicando estilos por defecto: " + e.getMessage());
        }
    }
    
    /**
     * Genera CSS por defecto con el tema Spotify.
     * 
     * @return String con CSS por defecto
     */
    private static String generateDefaultSpotifyCSS() {
        return """
            /* Colores principales de Spotify */
            .root {
                -fx-base: #191414;
                -fx-background: #191414;
                -fx-control-inner-background: #282828;
                -fx-control-inner-background-alt: #3E3E3E;
                -fx-accent: #1DB954;
                -fx-default-button: #1DB954;
                -fx-focus-color: #1DB954;
                -fx-faint-focus-color: #1DB95422;
            }
            
            /* Texto */
            .label {
                -fx-text-fill: #FFFFFF;
            }
            
            .label:disabled {
                -fx-text-fill: #B3B3B3;
            }
            
            /* Botones */
            .button {
                -fx-background-color: transparent;
                -fx-text-fill: #FFFFFF;
                -fx-border-color: #1DB954;
                -fx-border-width: 1px;
                -fx-border-radius: 20px;
                -fx-background-radius: 20px;
                -fx-padding: 8 16 8 16;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
            }
            
            .button:hover {
                -fx-background-color: #1DB954;
                -fx-scale-x: 1.05;
                -fx-scale-y: 1.05;
            }
            
            .button:pressed {
                -fx-background-color: #1ED760;
            }
            
            /* Campos de texto */
            .text-field {
                -fx-background-color: #3E3E3E;
                -fx-text-fill: #FFFFFF;
                -fx-prompt-text-fill: #B3B3B3;
                -fx-border-color: transparent;
                -fx-border-radius: 4px;
                -fx-background-radius: 4px;
                -fx-padding: 8;
            }
            
            .text-field:focused {
                -fx-border-color: #1DB954;
                -fx-border-width: 2px;
            }
            
            /* ListView */
            .list-view {
                -fx-background-color: transparent;
                -fx-border-color: transparent;
            }
            
            .list-cell {
                -fx-background-color: transparent;
                -fx-text-fill: #FFFFFF;
                -fx-padding: 8;
            }
            
            .list-cell:selected {
                -fx-background-color: #1DB954;
                -fx-text-fill: #000000;
            }
            
            .list-cell:hover {
                -fx-background-color: #282828;
            }
            """;
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
    
    /**
     * Obtiene el color gris de Spotify.
     * 
     * @return Color gris de Spotify (#282828)
     */
    public static String getSpotifyGray() {
        return "#282828";
    }
    
    /**
     * Obtiene el color de texto principal.
     * 
     * @return Color blanco (#FFFFFF)
     */
    public static String getSpotifyWhite() {
        return "#FFFFFF";
    }
    
    /**
     * Obtiene el color de texto secundario.
     * 
     * @return Color gris claro (#B3B3B3)
     */
    public static String getSpotifyLightGray() {
        return "#B3B3B3";
    }
}
""";
    }
}