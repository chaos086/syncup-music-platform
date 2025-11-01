package com.syncup;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import com.syncup.data.DataManager;
import com.syncup.utils.StyleManager;

/**
 * Clase principal de la aplicación SyncUp - Motor de Recomendaciones Musicales.
 * Esta clase inicia la aplicación JavaFX y configura la ventana principal.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class Main extends Application {
    
    /** Título de la aplicación */
    private static final String APP_TITLE = "SyncUp - Motor de Recomendaciones Musicales";
    
    /** Ancho inicial de la ventana */
    private static final int WINDOW_WIDTH = 1200;
    
    /** Alto inicial de la ventana */
    private static final int WINDOW_HEIGHT = 800;
    
    /** Ancho mínimo de la ventana */
    private static final int MIN_WINDOW_WIDTH = 800;
    
    /** Alto mínimo de la ventana */
    private static final int MIN_WINDOW_HEIGHT = 600;

    /**
     * Método start que se ejecuta al iniciar la aplicación JavaFX.
     * Configura la ventana principal y carga la pantalla de login.
     * 
     * @param primaryStage Escenario principal de la aplicación
     * @throws Exception Si ocurre un error al cargar la interfaz
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Inicializar el gestor de datos
            DataManager.getInstance().initialize();
            
            // Cargar la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            // Crear la escena principal
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            
            // Aplicar estilos CSS
            StyleManager.applySpotifyTheme(scene);
            
            // Configurar la ventana principal
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
            primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
            
            // Configurar icono de la aplicación
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/logo/syncup-icon.ico"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("No se pudo cargar el icono de la aplicación: " + e.getMessage());
            }
            
            // Configurar evento de cierre
            primaryStage.setOnCloseRequest(event -> {
                // Guardar datos antes de cerrar
                DataManager.getInstance().saveAllData();
                System.out.println("Aplicación cerrada correctamente.");
            });
            
            // Mostrar la ventana
            primaryStage.show();
            
            System.out.println("=== SyncUp Music Platform iniciado correctamente ===");
            System.out.println("Ventana principal: " + WINDOW_WIDTH + "x" + WINDOW_HEIGHT);
            
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Método principal que lanza la aplicación JavaFX.
     * Este es el punto de entrada de la aplicación.
     * 
     * @param args Argumentos de línea de comandos
     */
    public static void main(String[] args) {
        System.out.println("=== Iniciando SyncUp Music Platform ===");
        System.out.println("Versión: 1.0.0");
        System.out.println("Desarrollado por: Alejandro Marín Hernández");
        System.out.println("Universidad del Quindío - Estructura de Datos");
        System.out.println("================================================");
        
        // Lanzar la aplicación JavaFX
        launch(args);
    }
}