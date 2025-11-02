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
    @FXML private Button editSongButton;
    @FXML private Button deleteSongButton;
    @FXML private TextField newSongTitleField;
    @FXML private TextField newSongArtistField;
    @FXML private TextField newSongGenreField;
    @FXML private TextField newSongYearField;
    
    // Componentes de gestión de usuarios (RF-011)
    @FXML private TableView<Usuario> usersTable;
    @FXML private TableColumn<Usuario, String> userUsernameColumn;
    @FXML private TableColumn<Usuario, String> userNameColumn;
    @FXML private TableColumn<Usuario, String> userEmailColumn;
    @FXML private TableColumn<Usuario, Boolean> userActiveColumn;
    @FXML private Button deleteUserButton;
    @FXML private Button toggleUserStatusButton;
    
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
    @FXML private Label totalPlaysLabel;
    @FXML private Label systemUptimeLabel;
    @FXML private TextArea systemStatsArea;
    @FXML private Button refreshMetricsButton;
    
    // Componentes de gráficos (RF-014)
    @FXML private PieChart genreDistributionChart;
    @FXML private BarChart<String, Number> popularArtistsChart;
    @FXML private LineChart<String, Number> userGrowthChart;
    @FXML private Button refreshChartsButton;
    
    // Componentes de reportes
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private Button generateReportButton;
    @FXML private ListView<String> reportsListView;
    
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
        
        // Configurar controles
        setupControls();
        
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
        
        // Tabla de usuarios
        if (userUsernameColumn != null) userUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (userNameColumn != null) userNameColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        if (userEmailColumn != null) userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userActiveColumn != null) userActiveColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }
    
    /**
     * Configura los controles del dashboard.
     */
    private void setupControls() {
        // ComboBox de tipos de reporte
        if (reportTypeComboBox != null) {
            reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Catálogo Completo", "Usuarios del Sistema", "Estadísticas Generales"
            ));
            reportTypeComboBox.setValue("Catálogo Completo");
        }
    }
    
    /**
     * Establece el administrador actual y actualiza la interfaz.
     * 
     * @param admin Administrador logueado
     */
    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        
        // Actualizar información del admin
        actualizarDatosAdmin();
        
        // Cargar datos iniciales
        cargarCatalogo();
        cargarUsuarios();
        actualizarMetricas();
        generarGraficos();
        cargarListaReportes();
        
        System.out.println("Administrador establecido en dashboard: " + admin.getUsername());
    }
    
    /**
     * Actualiza los datos del administrador en la interfaz.
     */
    private void actualizarDatosAdmin() {
        if (currentAdmin == null) return;
        
        Platform.runLater(() -> {
            if (adminWelcomeLabel != null) {
                adminWelcomeLabel.setText("Panel Administrativo - " + currentAdmin.getNombreCompleto());
            }
            
            if (adminStatsLabel != null) {
                String stats = String.format("Nivel: %d/5 | Dept: %s | Acciones: %d",
                    currentAdmin.getNivelAcceso(),
                    currentAdmin.getDepartamento(),
                    currentAdmin.getHistorialAcciones().size());
                adminStatsLabel.setText(stats);
            }
        });
    }
    
    /**
     * RF-010: Carga el catálogo completo de canciones.
     */
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
    
    /**
     * RF-011: Carga todos los usuarios del sistema.
     */
    private void cargarUsuarios() {
        if (usersTable == null) return;
        
        Task<ObservableList<Usuario>> task = new Task<ObservableList<Usuario>>() {
            @Override
            protected ObservableList<Usuario> call() {
                List<Usuario> usuarios = dataManager.getAllUsuarios();
                return FXCollections.observableArrayList(usuarios);
            }
        };
        
        task.setOnSucceeded(e -> {
            usersTable.setItems(task.getValue());
            mostrarStatus("Usuarios cargados: " + task.getValue().size());
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-010: Maneja agregar nueva canción al catálogo.
     */
    @FXML
    private void handleAddSong() {
        if (!verificarPermisosGestionCatalogo()) return;
        
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
                    "Canción agregada: " + titulo + " - " + artista,
                    nuevaCancion.getId()
                );
                
                mostrarStatus("Canción agregada exitosamente: " + titulo);
                limpiarCamposNuevaCancion();
                cargarCatalogo();
                actualizarMetricas();
                generarGraficos();
            } else {
                mostrarError("No se pudo agregar la canción (posible duplicado)");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("Año debe ser un número válido");
        } catch (Exception e) {
            mostrarError("Error agregando canción: " + e.getMessage());
        }
    }
    
    /**
     * RF-010: Maneja eliminar canción seleccionada del catálogo.
     */
    @FXML
    private void handleDeleteSong() {
        if (!verificarPermisosGestionCatalogo() || catalogTable == null) return;
        
        Cancion cancionSeleccionada = catalogTable.getSelectionModel().getSelectedItem();
        if (cancionSeleccionada == null) {
            mostrarError("Selecciona una canción para eliminar");
            return;
        }
        
        // Confirmar eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar canción?");
        confirmacion.setContentText("Se eliminará: \"" + cancionSeleccionada.getTitulo() + 
                                   "\" de " + cancionSeleccionada.getArtista());
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
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
                        generarGraficos();
                    } else {
                        mostrarError("No se pudo eliminar la canción");
                    }
                } catch (Exception e) {
                    mostrarError("Error eliminando canción: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * RF-011: Maneja eliminar usuario seleccionado.
     */
    @FXML
    private void handleDeleteUser() {
        if (!verificarPermisosGestionUsuarios() || usersTable == null) return;
        
        Usuario usuarioSeleccionado = usersTable.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            mostrarError("Selecciona un usuario para eliminar");
            return;
        }
        
        if (usuarioSeleccionado.getId().equals(currentAdmin.getId())) {
            mostrarError("No puedes eliminar tu propia cuenta");
            return;
        }
        
        // Confirmar eliminación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación de Usuario");
        confirmacion.setHeaderText("¿Eliminar usuario?");
        confirmacion.setContentText("Se eliminará al usuario: " + usuarioSeleccionado.getUsername() + 
                                   "\nEsta acción no se puede deshacer.");
        
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean eliminado = dataManager.removeUsuario(usuarioSeleccionado.getId());
                    if (eliminado) {
                        currentAdmin.registrarAccion(
                            Admin.TipoAccionAdmin.ELIMINAR_USUARIO,
                            "Usuario eliminado: " + usuarioSeleccionado.getUsername(),
                            usuarioSeleccionado.getId()
                        );
                        
                        mostrarStatus("Usuario eliminado: " + usuarioSeleccionado.getUsername());
                        cargarUsuarios();
                        actualizarMetricas();
                    } else {
                        mostrarError("No se pudo eliminar el usuario");
                    }
                } catch (Exception e) {
                    mostrarError("Error eliminando usuario: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * RF-011: Alterna el estado activo/inactivo de un usuario.
     */
    @FXML
    private void handleToggleUserStatus() {
        if (!verificarPermisosGestionUsuarios() || usersTable == null) return;
        
        Usuario usuarioSeleccionado = usersTable.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            mostrarError("Selecciona un usuario para cambiar su estado");
            return;
        }
        
        try {
            boolean nuevoEstado = !usuarioSeleccionado.isActivo();
            usuarioSeleccionado.setActivo(nuevoEstado);
            dataManager.saveAllData();
            
            String accion = nuevoEstado ? "activado" : "desactivado";
            currentAdmin.registrarAccion(
                Admin.TipoAccionAdmin.EDITAR_USUARIO,
                "Usuario " + accion + ": " + usuarioSeleccionado.getUsername(),
                usuarioSeleccionado.getId()
            );
            
            mostrarStatus("Usuario " + usuarioSeleccionado.getUsername() + " " + accion);
            cargarUsuarios();
            
        } catch (Exception e) {
            mostrarError("Error cambiando estado de usuario: " + e.getMessage());
        }
    }
    
    /**
     * RF-012: Maneja selección de archivo para carga masiva.
     */
    @FXML
    private void handleSelectFile() {
        if (!verificarPermisosCargaMasiva()) return;
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo para carga masiva");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos de texto", "*.txt", "*.tsv")
        );
        
        selectedBulkFile = fileChooser.showOpenDialog(selectFileButton.getScene().getWindow());
        
        if (selectedBulkFile != null) {
            if (bulkLoadStatusLabel != null) {
                bulkLoadStatusLabel.setText("Archivo seleccionado: " + selectedBulkFile.getName());
            }
            mostrarStatus("Archivo seleccionado para carga masiva");
        }
    }
    
    /**
     * RF-012: Valida el archivo seleccionado antes de la carga masiva.
     */
    @FXML
    private void handleValidateFile() {
        if (!verificarPermisosCargaMasiva()) return;
        
        if (selectedBulkFile == null) {
            mostrarError("Selecciona un archivo primero");
            return;
        }
        
        mostrarCargando(true);
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
                    StringBuilder sb = new StringBuilder();
                    sb.append("=== VALIDACIÓN DE ARCHIVO ===\n");
                    sb.append(resultado.mensaje).append("\n\n");
                    
                    if (!resultado.errores.isEmpty()) {
                        sb.append("ERRORES ENCONTRADOS:\n");
                        for (String error : resultado.errores) {
                            sb.append("- ").append(error).append("\n");
                        }
                    }
                    
                    bulkLoadResultsArea.setText(sb.toString());
                }
                
                if (resultado.valido) {
                    mostrarStatus("Archivo válido - Listo para carga masiva");
                } else {
                    mostrarError("Archivo inválido - Revisa los errores");
                }
                
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-012: Realiza carga masiva de canciones.
     */
    @FXML
    private void handleBulkLoad() {
        if (!verificarPermisosCargaMasiva()) return;
        
        if (selectedBulkFile == null) {
            mostrarError("Selecciona un archivo primero");
            return;
        }
        
        mostrarCargando(true);
        if (bulkLoadProgress != null) bulkLoadProgress.setVisible(true);
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
                    StringBuilder sb = new StringBuilder();
                    sb.append("=== RESULTADO CARGA MASIVA ===\n");
                    sb.append(resultado.mensaje).append("\n\n");
                    
                    if (!resultado.errores.isEmpty()) {
                        sb.append("ERRORES:\n");
                        for (String error : resultado.errores) {
                            sb.append("- ").append(error).append("\n");
                        }
                    }
                    
                    bulkLoadResultsArea.setText(sb.toString());
                }
                
                if (resultado.exito) {
                    mostrarStatus("Carga masiva completada: " + resultado.cancionesAgregadas + " canciones");
                    cargarCatalogo();
                    actualizarMetricas();
                    generarGraficos();
                    
                    // Actualizar sistema de recomendaciones
                    recommendationEngine.actualizarSistema();
                } else {
                    mostrarError("Carga masiva fallida");
                }
                
                mostrarCargando(false);
                if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false);
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                mostrarError("Error en carga masiva: " + task.getException().getMessage());
                mostrarCargando(false);
                if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-013: Actualiza las métricas del sistema.
     */
    @FXML
    private void handleRefreshMetrics() {
        actualizarMetricas();
    }
    
    /**
     * RF-013: Actualiza métricas del sistema.
     */
    private void actualizarMetricas() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    // Métricas básicas
                    if (totalUsersLabel != null) {
                        totalUsersLabel.setText(String.valueOf(dataManager.getAllUsuarios().size()));
                    }
                    
                    if (totalSongsLabel != null) {
                        totalSongsLabel.setText(String.valueOf(dataManager.getAllCanciones().size()));
                    }
                    
                    if (totalPlaysLabel != null) {
                        long totalReproducciones = dataManager.getAllCanciones().stream()
                            .mapToLong(Cancion::getReproducciones)
                            .sum();
                        totalPlaysLabel.setText(String.valueOf(totalReproducciones));
                    }
                    
                    if (systemUptimeLabel != null) {
                        systemUptimeLabel.setText("Sistema Operativo");
                    }
                    
                    // Estadísticas detalladas
                    if (systemStatsArea != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(dataManager.getSystemStats()).append("\n\n");
                        sb.append(recommendationEngine.getEstadisticas()).append("\n\n");
                        sb.append(currentAdmin.getResumenEstadisticas());
                        systemStatsArea.setText(sb.toString());
                    }
                });
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * RF-014: Genera gráficos interactivos con JavaFX Charts.
     */
    @FXML
    private void handleRefreshCharts() {
        generarGraficos();
    }
    
    /**
     * RF-014: Genera todos los gráficos del dashboard.
     */
    private void generarGraficos() {
        // Gráfico de distribución de géneros
        generarGraficoGeneros();
        
        // Gráfico de artistas populares
        generarGraficoArtistasPopulares();
        
        // Gráfico de crecimiento de usuarios (simulado)
        generarGraficoCrecimientoUsuarios();
    }
    
    /**
     * RF-014: Genera gráfico circular de distribución de géneros.
     */
    private void generarGraficoGeneros() {
        if (genreDistributionChart == null) return;
        
        Task<ObservableList<PieChart.Data>> task = new Task<ObservableList<PieChart.Data>>() {
            @Override
            protected ObservableList<PieChart.Data> call() {
                Map<String, Integer> generos = dataManager.getAllCanciones().stream()
                    .collect(Collectors.groupingBy(
                        Cancion::getGenero,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                    ));
                
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                generos.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10) // Top 10 géneros
                    .forEach(entry -> {
                        pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                    });
                
                return pieData;
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                genreDistributionChart.setData(task.getValue());
                genreDistributionChart.setTitle("Distribución de Géneros Musicales");
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-014: Genera gráfico de barras de artistas populares.
     */
    private void generarGraficoArtistasPopulares() {
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
                    .limit(15) // Top 15 artistas
                    .forEach(entry -> {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });
                
                Platform.runLater(() -> {
                    popularArtistsChart.getData().clear();
                    popularArtistsChart.getData().add(series);
                    popularArtistsChart.setTitle("Top 15 Artistas con Más Canciones");
                });
                
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * RF-014: Genera gráfico de líneas de crecimiento de usuarios (simulado).
     */
    private void generarGraficoCrecimientoUsuarios() {
        if (userGrowthChart == null) return;
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Usuarios Registrados");
                
                // Datos simulados de crecimiento
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                int usuariosActuales = dataManager.getAllUsuarios().size();
                
                for (int i = 0; i < meses.length; i++) {
                    int usuarios = Math.max(1, usuariosActuales - (meses.length - i - 1) * 2 + (int)(Math.random() * 5));
                    series.getData().add(new XYChart.Data<>(meses[i], usuarios));
                }
                
                Platform.runLater(() -> {
                    userGrowthChart.getData().clear();
                    userGrowthChart.getData().add(series);
                    userGrowthChart.setTitle("Crecimiento de Usuarios 2025");
                });
                
                return null;
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * RF-029: Genera reporte seleccionado.
     */
    @FXML
    private void handleGenerateReport() {
        if (reportTypeComboBox == null) return;
        
        String tipoReporte = reportTypeComboBox.getValue();
        if (tipoReporte == null) {
            mostrarError("Selecciona un tipo de reporte");
            return;
        }
        
        mostrarCargando(true);
        mostrarStatus("Generando reporte: " + tipoReporte);
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                switch (tipoReporte) {
                    case "Catálogo Completo":
                        return reportService.exportarCatalogoCompleto(currentAdmin.getId());
                    case "Usuarios del Sistema":
                        return reportService.exportarReporteUsuarios(currentAdmin.getId());
                    case "Estadísticas Generales":
                        return reportService.generarReporteEstadisticas(currentAdmin.getId());
                    default:
                        return null;
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                String rutaArchivo = task.getValue();
                if (rutaArchivo != null) {
                    mostrarStatus("Reporte generado: " + new File(rutaArchivo).getName());
                    cargarListaReportes();
                } else {
                    mostrarError("Error generando reporte");
                }
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * Carga la lista de reportes generados.
     */
    private void cargarListaReportes() {
        if (reportsListView == null) return;
        
        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() {
                List<String> reportes = reportService.listarReportesGenerados();
                return FXCollections.observableArrayList(reportes);
            }
        };
        
        task.setOnSucceeded(e -> reportsListView.setItems(task.getValue()));
        new Thread(task).start();
    }
    
    /**
     * Limpia los campos del formulario de nueva canción.
     */
    private void limpiarCamposNuevaCancion() {
        if (newSongTitleField != null) newSongTitleField.clear();
        if (newSongArtistField != null) newSongArtistField.clear();
        if (newSongGenreField != null) newSongGenreField.clear();
        if (newSongYearField != null) newSongYearField.clear();
    }
    
    // Métodos de verificación de permisos
    
    private boolean verificarPermisosGestionCatalogo() {
        if (currentAdmin == null || !currentAdmin.isPuedeGestionarCatalogo()) {
            mostrarError("No tienes permisos para gestionar el catálogo");
            return false;
        }
        return true;
    }
    
    private boolean verificarPermisosGestionUsuarios() {
        if (currentAdmin == null || !currentAdmin.isPuedeGestionarUsuarios()) {
            mostrarError("No tienes permisos para gestionar usuarios");
            return false;
        }
        return true;
    }
    
    private boolean verificarPermisosCargaMasiva() {
        if (currentAdmin == null || !currentAdmin.isPuedeCargaMasiva()) {
            mostrarError("No tienes permisos para realizar carga masiva");
            return false;
        }
        return true;
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