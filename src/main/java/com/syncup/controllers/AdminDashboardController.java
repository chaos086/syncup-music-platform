package com.syncup.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.models.Admin;
import com.syncup.data.DataManager;
import com.syncup.services.ReportService;
import com.syncup.services.BulkDataLoader;
import com.syncup.algorithms.RecommendationEngine;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

/**
 * Controlador para el dashboard de administradores.
 * RF-010: Gestión completa del catálogo
 * RF-011: Administración de usuarios
 * RF-012: Carga masiva de canciones
 * RF-013: Panel de métricas del sistema
 * RF-014: Gráficos interactivos con JavaFX Charts
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class AdminDashboardController implements Initializable {
    
    // Componentes del perfil admin
    @FXML private Label adminWelcomeLabel;
    @FXML private Label adminStatsLabel;
    
    // Componentes de gestión de catálogo (RF-010)
    @FXML private TableView<Cancion> catalogTable;
    @FXML private TableColumn<Cancion, String> catalogTitleColumn;
    @FXML private TableColumn<Cancion, String> catalogArtistColumn;
    @FXML private TableColumn<Cancion, String> catalogGenreColumn;
    @FXML private TableColumn<Cancion, Integer> catalogYearColumn;
    @FXML private Button addSongButton;
    @FXML private Button deleteSongButton;
    @FXML private TextField newSongTitleField;
    @FXML private TextField newSongArtistField;
    @FXML private TextField newSongGenreField;
    @FXML private TextField newSongYearField;
    
    // Componentes de carga masiva (RF-012)
    @FXML private Label bulkLoadStatusLabel;
    @FXML private Button selectFileButton;
    @FXML private Button validateFileButton;
    @FXML private Button bulkLoadButton;
    @FXML private TextArea bulkLoadResultsArea;
    @FXML private ProgressIndicator bulkLoadProgress;
    
    // Componentes de métricas (RF-013)
    @FXML private Label totalUsersLabel;
    @FXML private Label totalSongsLabel;
    @FXML private TextArea systemStatsArea;
    @FXML private Button refreshMetricsButton;
    
    // Componentes de gráficos (RF-014)
    @FXML private PieChart genreDistributionChart;
    @FXML private BarChart<String, Number> popularArtistsChart;
    
    // Estado y servicios
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    /** Administrador actual */
    private Admin currentAdmin;
    
    /** Archivo seleccionado para carga masiva */
    private File selectedBulkFile;
    
    /** Servicios del sistema */
    private DataManager dataManager;
    private ReportService reportService;
    private BulkDataLoader bulkDataLoader;
    private RecommendationEngine recommendationEngine;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        dataManager = DataManager.getInstance();
        reportService = new ReportService();
        bulkDataLoader = new BulkDataLoader();
        recommendationEngine = new RecommendationEngine();
        
        // Configurar tablas
        setupTables();
        
        // Ocultar indicadores de carga
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false);
        
        System.out.println("AdminDashboardController inicializado");
    }
    
    /**
     * Configura las tablas del dashboard.
     */
    private void setupTables() {
        // Tabla de catálogo
        if (catalogTitleColumn != null) catalogTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (catalogArtistColumn != null) catalogArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (catalogGenreColumn != null) catalogGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (catalogYearColumn != null) catalogYearColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
    }
    
    /**
     * Establece el administrador actual y actualiza la interfaz.
     */
    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        actualizarDatosAdmin();
        cargarCatalogo();
        actualizarMetricas();
        generarGraficos();
        
        System.out.println("Administrador establecido: " + admin.getUsername());
    }
    
    private void actualizarDatosAdmin() {
        if (currentAdmin == null) return;
        
        Platform.runLater(() -> {
            if (adminWelcomeLabel != null) {
                adminWelcomeLabel.setText("Panel Administrativo - " + currentAdmin.getNombreCompleto());
            }
            
            if (adminStatsLabel != null) {
                String stats = String.format("Nivel: %d/5 | Dept: %s",
                    currentAdmin.getNivelAcceso(),
                    currentAdmin.getDepartamento());
                adminStatsLabel.setText(stats);
            }
        });
    }
    
    private void cargarCatalogo() {
        if (catalogTable == null) return;
        
        Task<ObservableList<Cancion>> task = new Task<ObservableList<Cancion>>() {
            @Override
            protected ObservableList<Cancion> call() {
                List<Cancion> canciones = dataManager.getAllCanciones();
                return FXCollections.observableArrayList(canciones);
            }
        };
        
        task.setOnSucceeded(e -> {
            catalogTable.setItems(task.getValue());
            mostrarStatus("Catálogo cargado: " + task.getValue().size() + " canciones");
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleAddSong() {
        try {
            String titulo = newSongTitleField.getText().trim();
            String artista = newSongArtistField.getText().trim();
            String genero = newSongGenreField.getText().trim();
            String anioStr = newSongYearField.getText().trim();
            
            if (titulo.isEmpty() || artista.isEmpty() || genero.isEmpty() || anioStr.isEmpty()) {
                mostrarError("Todos los campos son obligatorios");
                return;
            }
            
            int anio = Integer.parseInt(anioStr);
            Cancion nuevaCancion = new Cancion(titulo, artista, genero, anio);
            
            boolean agregada = dataManager.addCancion(nuevaCancion);
            if (agregada) {
                currentAdmin.registrarAccion(
                    Admin.TipoAccionAdmin.AGREGAR_CANCION,
                    "Canción agregada: " + titulo,
                    nuevaCancion.getId()
                );
                
                mostrarStatus("Canción agregada: " + titulo);
                limpiarCamposNuevaCancion();
                cargarCatalogo();
                actualizarMetricas();
            } else {
                mostrarError("No se pudo agregar la canción");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("Año debe ser un número válido");
        } catch (Exception e) {
            mostrarError("Error agregando canción: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteSong() {
        if (catalogTable == null) return;
        
        Cancion cancionSeleccionada = catalogTable.getSelectionModel().getSelectedItem();
        if (cancionSeleccionada == null) {
            mostrarError("Selecciona una canción para eliminar");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar canción?");
        confirmacion.setContentText("Se eliminará: \"" + cancionSeleccionada.getTitulo() + "\"");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean eliminada = dataManager.removeCancion(cancionSeleccionada.getId());
                if (eliminada) {
                    currentAdmin.registrarAccion(
                        Admin.TipoAccionAdmin.ELIMINAR_CANCION,
                        "Canción eliminada: " + cancionSeleccionada.getTitulo(),
                        cancionSeleccionada.getId()
                    );
                    
                    mostrarStatus("Canción eliminada: " + cancionSeleccionada.getTitulo());
                    cargarCatalogo();
                    actualizarMetricas();
                }
            }
        });
    }
    
    @FXML
    private void handleSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo para carga masiva");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos de texto", "*.txt", "*.tsv")
        );
        
        selectedBulkFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        
        if (selectedBulkFile != null) {
            if (bulkLoadStatusLabel != null) {
                bulkLoadStatusLabel.setText("Archivo: " + selectedBulkFile.getName());
            }
            mostrarStatus("Archivo seleccionado");
        }
    }
    
    @FXML
    private void handleValidateFile() {
        if (selectedBulkFile == null) {
            mostrarError("Selecciona un archivo primero");
            return;
        }
        
        mostrarStatus("Validando archivo...");
        
        Task<BulkDataLoader.ResultadoValidacion> task = new Task<BulkDataLoader.ResultadoValidacion>() {
            @Override
            protected BulkDataLoader.ResultadoValidacion call() {
                return bulkDataLoader.validarArchivoFormato(selectedBulkFile.getAbsolutePath());
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                BulkDataLoader.ResultadoValidacion resultado = task.getValue();
                
                if (bulkLoadResultsArea != null) {
                    bulkLoadResultsArea.setText("Validación: " + resultado.mensaje);
                }
                
                if (resultado.valido) {
                    mostrarStatus("Archivo válido - Listo para carga");
                } else {
                    mostrarError("Archivo inválido");
                }
            });
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleBulkLoad() {
        if (selectedBulkFile == null) {
            mostrarError("Selecciona un archivo primero");
            return;
        }
        
        mostrarCargando(true);
        mostrarStatus("Realizando carga masiva...");
        
        Task<BulkDataLoader.ResultadoCargaMasiva> task = new Task<BulkDataLoader.ResultadoCargaMasiva>() {
            @Override
            protected BulkDataLoader.ResultadoCargaMasiva call() {
                return bulkDataLoader.cargarCancionesMasivas(selectedBulkFile.getAbsolutePath(), currentAdmin.getId());
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                BulkDataLoader.ResultadoCargaMasiva resultado = task.getValue();
                
                if (bulkLoadResultsArea != null) {
                    bulkLoadResultsArea.setText("Carga completada: " + resultado.mensaje);
                }
                
                if (resultado.exito) {
                    mostrarStatus("Carga masiva exitosa: " + resultado.cancionesAgregadas + " canciones");
                    cargarCatalogo();
                    actualizarMetricas();
                    generarGraficos();
                } else {
                    mostrarError("Carga masiva fallida");
                }
                
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleRefreshMetrics() {
        actualizarMetricas();
    }
    
    private void actualizarMetricas() {
        Platform.runLater(() -> {
            if (totalUsersLabel != null) {
                totalUsersLabel.setText(String.valueOf(dataManager.getAllUsuarios().size()));
            }
            
            if (totalSongsLabel != null) {
                totalSongsLabel.setText(String.valueOf(dataManager.getAllCanciones().size()));
            }
            
            if (systemStatsArea != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(dataManager.getSystemStats()).append("\n\n");
                sb.append(recommendationEngine.getEstadisticas());
                systemStatsArea.setText(sb.toString());
            }
        });
    }
    
    private void generarGraficos() {
        generarGraficoGeneros();
        generarGraficoArtistas();
    }
    
    private void generarGraficoGeneros() {
        if (genreDistributionChart == null) return;
        
        Task<ObservableList<PieChart.Data>> task = new Task<ObservableList<PieChart.Data>>() {
            @Override
            protected ObservableList<PieChart.Data> call() {
                Map<String, Long> generos = dataManager.getAllCanciones().stream()
                    .collect(Collectors.groupingBy(
                        Cancion::getGenero,
                        Collectors.counting()
                    ));
                
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                generos.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(8)
                    .forEach(entry -> {
                        pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                    });
                
                return pieData;
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                genreDistributionChart.setData(task.getValue());
                genreDistributionChart.setTitle("Distribución de Géneros");
            });
        });
        
        new Thread(task).start();
    }
    
    private void generarGraficoArtistas() {
        if (popularArtistsChart == null) return;
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                Map<String, Long> artistas = dataManager.getAllCanciones().stream()
                    .collect(Collectors.groupingBy(
                        Cancion::getArtista,
                        Collectors.counting()
                    ));
                
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Canciones por Artista");
                
                artistas.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(entry -> {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });
                
                Platform.runLater(() -> {
                    popularArtistsChart.getData().clear();
                    popularArtistsChart.getData().add(series);
                    popularArtistsChart.setTitle("Top 10 Artistas");
                });
                
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    private void limpiarCamposNuevaCancion() {
        if (newSongTitleField != null) newSongTitleField.clear();
        if (newSongArtistField != null) newSongArtistField.clear();
        if (newSongGenreField != null) newSongGenreField.clear();
        if (newSongYearField != null) newSongYearField.clear();
    }
    
    // Métodos de utilidad para UI
    private void mostrarStatus(String mensaje) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(mensaje);
                statusLabel.setStyle("-fx-text-fill: #1DB954;");
            }
        });
    }
    
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Error: " + mensaje);
                statusLabel.setStyle("-fx-text-fill: #FF6B6B;");
            }
        });
    }
    
    private void mostrarCargando(boolean mostrar) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(mostrar);
            }
        });
    }
}