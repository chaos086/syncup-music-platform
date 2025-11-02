package com.syncup.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.services.SearchService;
import com.syncup.services.ReportService;
import com.syncup.algorithms.RecommendationEngine;
import com.syncup.structures.GrafoSocial;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.io.File;

/**
 * Controlador para el dashboard principal de usuarios.
 * RF-002: Gestión de perfil y favoritos
 * RF-005: Playlist "Descubrimiento Semanal"
 * RF-006: Radio personalizada 
 * RF-007: Seguir/dejar de seguir usuarios
 * RF-008: Sugerencias de usuarios
 * RF-009: Exportación de reportes CSV
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class UserDashboardController implements Initializable {
    
    // Componentes del perfil de usuario
    @FXML private Label welcomeLabel;
    @FXML private Label userStatsLabel;
    @FXML private TextField nombreCompletoField;
    @FXML private TextField emailField;
    @FXML private Button saveProfileButton;
    
    // Componentes de favoritos
    @FXML private TableView<Cancion> favoritesTable;
    @FXML private TableColumn<Cancion, String> favTitleColumn;
    @FXML private TableColumn<Cancion, String> favArtistColumn;
    @FXML private TableColumn<Cancion, String> favGenreColumn;
    @FXML private Button removeFavoriteButton;
    @FXML private Button exportFavoritesButton;
    
    // Componentes de descubrimiento
    @FXML private TableView<Cancion> discoveryTable;
    @FXML private TableColumn<Cancion, String> discTitleColumn;
    @FXML private TableColumn<Cancion, String> discArtistColumn;
    @FXML private TableColumn<Cancion, String> discGenreColumn;
    @FXML private Button generateDiscoveryButton;
    @FXML private Button addToFavoritesButton;
    
    // Componentes de búsqueda
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private TableView<Cancion> searchResultsTable;
    @FXML private TableColumn<Cancion, String> searchTitleColumn;
    @FXML private TableColumn<Cancion, String> searchArtistColumn;
    @FXML private Button searchButton;
    
    // Componentes sociales
    @FXML private TableView<Usuario> followingTable;
    @FXML private TableColumn<Usuario, String> followingUsernameColumn;
    @FXML private TableColumn<Usuario, String> followingNameColumn;
    @FXML private TableView<Usuario> suggestedUsersTable;
    @FXML private TableColumn<Usuario, String> suggestedUsernameColumn;
    @FXML private TableColumn<Usuario, String> suggestedNameColumn;
    @FXML private Button followUserButton;
    @FXML private Button unfollowUserButton;
    @FXML private Button refreshSuggestionsButton;
    
    // Componentes de estado
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    /** Usuario actual logueado */
    private Usuario currentUser;
    
    /** Servicios del sistema */
    private DataManager dataManager;
    private SearchService searchService;
    private ReportService reportService;
    private RecommendationEngine recommendationEngine;
    private GrafoSocial grafoSocial;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        dataManager = DataManager.getInstance();
        searchService = new SearchService();
        reportService = new ReportService();
        recommendationEngine = new RecommendationEngine();
        grafoSocial = new GrafoSocial();
        
        // Configurar tablas
        setupTables();
        
        // Configurar controles
        setupControls();
        
        // Ocultar indicador de carga inicialmente
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
        
        System.out.println("UserDashboardController inicializado");
    }
    
    /**
     * Configura las tablas del dashboard.
     */
    private void setupTables() {
        // Tabla de favoritos
        if (favTitleColumn != null) favTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (favArtistColumn != null) favArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (favGenreColumn != null) favGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        
        // Tabla de descubrimiento
        if (discTitleColumn != null) discTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (discArtistColumn != null) discArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (discGenreColumn != null) discGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        
        // Tabla de seguidos
        if (followingUsernameColumn != null) followingUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (followingNameColumn != null) followingNameColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        
        // Tabla de sugerencias
        if (suggestedUsernameColumn != null) suggestedUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (suggestedNameColumn != null) suggestedNameColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        
        // Tabla de búsqueda
        if (searchTitleColumn != null) searchTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (searchArtistColumn != null) searchArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
    }
    
    /**
     * Configura los controles del dashboard.
     */
    private void setupControls() {
        // ComboBox de tipos de búsqueda
        if (searchTypeComboBox != null) {
            searchTypeComboBox.setItems(FXCollections.observableArrayList(
                "Título", "Artista", "Género", "Todo"
            ));
            searchTypeComboBox.setValue("Todo");
        }
        
        // Configurar autocompletado en campo de búsqueda
        if (searchField != null) {
            setupAutoComplete();
        }
    }
    
    /**
     * Configura autocompletado para el campo de búsqueda.
     */
    private void setupAutoComplete() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 2) {
                // RF-003: Autocompletado usando Trie
                Task<List<String>> autoCompleteTask = new Task<List<String>>() {
                    @Override
                    protected List<String> call() {
                        String searchType = searchTypeComboBox.getValue();
                        switch (searchType) {
                            case "Título":
                                return searchService.autocompletarTitulos(newText, 10);
                            case "Artista":
                                return searchService.autocompletarArtistas(newText, 10);
                            case "Género":
                                return searchService.autocompletarGeneros(newText, 10);
                            default:
                                return searchService.autocompletarTitulos(newText, 10);
                        }
                    }
                };
                
                autoCompleteTask.setOnSucceeded(e -> {
                    List<String> sugerencias = autoCompleteTask.getValue();
                    if (!sugerencias.isEmpty()) {
                        mostrarStatus("Sugerencias: " + String.join(", ", sugerencias.subList(0, Math.min(3, sugerencias.size()))));
                    }
                });
                
                new Thread(autoCompleteTask).start();
            }
        });
    }
    
    /**
     * Establece el usuario actual y actualiza la interfaz.
     */
    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
        actualizarDatosUsuario();
        cargarFavoritos();
        cargarSeguidos();
        cargarSugerenciasUsuarios();
        
        System.out.println("Usuario establecido en dashboard: " + usuario.getUsername());
    }
    
    private void actualizarDatosUsuario() {
        if (currentUser == null) return;
        
        Platform.runLater(() -> {
            if (welcomeLabel != null) {
                welcomeLabel.setText("¡Bienvenido, " + currentUser.getNombreCompleto() + "!");
            }
            
            if (userStatsLabel != null) {
                String stats = String.format("Favoritas: %d | Siguiendo: %d | Seguidores: %d",
                    currentUser.getNumeroCancionesFavoritas(),
                    currentUser.getNumeroSeguidos(),
                    currentUser.getNumeroSeguidores());
                userStatsLabel.setText(stats);
            }
            
            if (nombreCompletoField != null) {
                nombreCompletoField.setText(currentUser.getNombreCompleto());
            }
            
            if (emailField != null) {
                emailField.setText(currentUser.getEmail());
            }
        });
    }
    
    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) return;
        
        try {
            if (nombreCompletoField != null) {
                currentUser.setNombreCompleto(nombreCompletoField.getText().trim());
            }
            if (emailField != null) {
                currentUser.setEmail(emailField.getText().trim());
            }
            
            dataManager.saveAllData();
            mostrarStatus("Perfil actualizado correctamente");
            actualizarDatosUsuario();
            
        } catch (Exception e) {
            mostrarError("Error actualizando perfil: " + e.getMessage());
        }
    }
    
    private void cargarFavoritos() {
        if (currentUser == null || favoritesTable == null) return;
        
        Task<ObservableList<Cancion>> task = new Task<ObservableList<Cancion>>() {
            @Override
            protected ObservableList<Cancion> call() {
                List<Cancion> favoritas = new ArrayList<>();
                for (String cancionId : currentUser.getCancionesFavoritas()) {
                    Cancion cancion = dataManager.getCancionById(cancionId);
                    if (cancion != null) {
                        favoritas.add(cancion);
                    }
                }
                return FXCollections.observableArrayList(favoritas);
            }
        };
        
        task.setOnSucceeded(e -> {
            favoritesTable.setItems(task.getValue());
            mostrarStatus("Favoritos cargados: " + task.getValue().size());
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleGenerateDiscovery() {
        if (currentUser == null || discoveryTable == null) return;
        
        mostrarCargando(true);
        mostrarStatus("Generando Descubrimiento Semanal...");
        
        Task<List<Cancion>> task = new Task<List<Cancion>>() {
            @Override
            protected List<Cancion> call() {
                return recommendationEngine.generarDescubrimientoSemanal(currentUser.getId(), 20);
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                List<Cancion> recomendaciones = task.getValue();
                discoveryTable.setItems(FXCollections.observableArrayList(recomendaciones));
                mostrarStatus("Descubrimiento Semanal generado: " + recomendaciones.size() + " canciones");
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleFollowUser() {
        if (currentUser == null || suggestedUsersTable == null) return;
        
        Usuario usuarioParaSeguir = suggestedUsersTable.getSelectionModel().getSelectedItem();
        if (usuarioParaSeguir == null) {
            mostrarError("Selecciona un usuario para seguir");
            return;
        }
        
        try {
            boolean seguido = currentUser.seguirUsuario(usuarioParaSeguir.getId());
            if (seguido) {
                usuarioParaSeguir.agregarSeguidor(currentUser.getId());
                grafoSocial.conectarUsuarios(currentUser.getId(), usuarioParaSeguir.getId());
                dataManager.saveAllData();
                
                mostrarStatus("Ahora sigues a " + usuarioParaSeguir.getUsername());
                actualizarDatosUsuario();
                cargarSeguidos();
                cargarSugerenciasUsuarios();
            } else {
                mostrarError("No se pudo seguir al usuario");
            }
        } catch (Exception e) {
            mostrarError("Error siguiendo usuario: " + e.getMessage());
        }
    }
    
    private void cargarSeguidos() {
        if (currentUser == null || followingTable == null) return;
        
        Task<ObservableList<Usuario>> task = new Task<ObservableList<Usuario>>() {
            @Override
            protected ObservableList<Usuario> call() {
                List<Usuario> seguidos = new ArrayList<>();
                for (String usuarioId : currentUser.getUsuariosSeguidos()) {
                    Usuario usuario = dataManager.getUsuarioById(usuarioId);
                    if (usuario != null) {
                        seguidos.add(usuario);
                    }
                }
                return FXCollections.observableArrayList(seguidos);
            }
        };
        
        task.setOnSucceeded(e -> followingTable.setItems(task.getValue()));
        new Thread(task).start();
    }
    
    private void cargarSugerenciasUsuarios() {
        if (currentUser == null || suggestedUsersTable == null) return;
        
        Task<ObservableList<Usuario>> task = new Task<ObservableList<Usuario>>() {
            @Override
            protected ObservableList<Usuario> call() {
                grafoSocial.agregarUsuario(currentUser);
                // Simplificado: sugerir usuarios aleatorios por ahora
                List<Usuario> todosUsuarios = dataManager.getAllUsuarios();
                List<Usuario> sugerencias = new ArrayList<>();
                
                for (Usuario u : todosUsuarios) {
                    if (!u.getId().equals(currentUser.getId()) && 
                        !currentUser.getUsuariosSeguidos().contains(u.getId()) &&
                        sugerencias.size() < 10) {
                        sugerencias.add(u);
                    }
                }
                
                return FXCollections.observableArrayList(sugerencias);
            }
        };
        
        task.setOnSucceeded(e -> {
            suggestedUsersTable.setItems(task.getValue());
            mostrarStatus("Sugerencias actualizadas: " + task.getValue().size() + " usuarios");
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleRefreshSuggestions() {
        cargarSugerenciasUsuarios();
    }
    
    @FXML
    private void handleSearch() {
        if (searchField == null || searchResultsTable == null) return;
        
        String termino = searchField.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Ingresa un término de búsqueda");
            return;
        }
        
        mostrarCargando(true);
        
        Task<List<Cancion>> task = new Task<List<Cancion>>() {
            @Override
            protected List<Cancion> call() {
                return searchService.busquedaSimple(termino);
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                List<Cancion> resultados = task.getValue();
                searchResultsTable.setItems(FXCollections.observableArrayList(resultados));
                mostrarStatus("Búsqueda completada: " + resultados.size() + " resultados");
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleAddToFavorites() {
        TableView<Cancion> tablaActiva = null;
        
        if (discoveryTable != null && discoveryTable.getSelectionModel().getSelectedItem() != null) {
            tablaActiva = discoveryTable;
        } else if (searchResultsTable != null && searchResultsTable.getSelectionModel().getSelectedItem() != null) {
            tablaActiva = searchResultsTable;
        }
        
        if (tablaActiva == null) {
            mostrarError("Selecciona una canción para agregar a favoritos");
            return;
        }
        
        Cancion cancionSeleccionada = tablaActiva.getSelectionModel().getSelectedItem();
        
        try {
            boolean agregada = currentUser.agregarCancionFavorita(cancionSeleccionada.getId());
            if (agregada) {
                dataManager.saveAllData();
                mostrarStatus("\"" + cancionSeleccionada.getTitulo() + "\" agregada a favoritos");
                actualizarDatosUsuario();
                cargarFavoritos();
            } else {
                mostrarError("La canción ya está en favoritos");
            }
        } catch (Exception e) {
            mostrarError("Error agregando a favoritos: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRemoveFavorite() {
        if (currentUser == null || favoritesTable == null) return;
        
        Cancion cancionSeleccionada = favoritesTable.getSelectionModel().getSelectedItem();
        if (cancionSeleccionada == null) {
            mostrarError("Selecciona una canción para remover de favoritos");
            return;
        }
        
        try {
            boolean removida = currentUser.removerCancionFavorita(cancionSeleccionada.getId());
            if (removida) {
                dataManager.saveAllData();
                mostrarStatus("\"" + cancionSeleccionada.getTitulo() + "\" removida de favoritos");
                actualizarDatosUsuario();
                cargarFavoritos();
            }
        } catch (Exception e) {
            mostrarError("Error removiendo de favoritos: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExportFavorites() {
        if (currentUser == null) return;
        
        mostrarCargando(true);
        mostrarStatus("Exportando favoritos a CSV...");
        
        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return reportService.exportarFavoritasUsuario(currentUser.getId());
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                String rutaArchivo = task.getValue();
                if (rutaArchivo != null) {
                    mostrarStatus("Favoritos exportados: " + new File(rutaArchivo).getName());
                } else {
                    mostrarError("Error exportando favoritos");
                }
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    // Métodos de utilidad
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