package com.syncup.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.services.SearchService;
import com.syncup.services.ReportService;
import com.syncup.algorithms.RecommendationEngine;
import com.syncup.structures.GrafoSocial;

import java.net.URL;
import java.util.List;
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
    
    // Componentes de radio personalizada
    @FXML private ComboBox<Cancion> seedSongComboBox;
    @FXML private TableView<Cancion> radioTable;
    @FXML private TableColumn<Cancion, String> radioTitleColumn;
    @FXML private TableColumn<Cancion, String> radioArtistColumn;
    @FXML private Button generateRadioButton;
    
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
    
    // Componentes de búsqueda
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private TableView<Cancion> searchResultsTable;
    @FXML private TableColumn<Cancion, String> searchTitleColumn;
    @FXML private TableColumn<Cancion, String> searchArtistColumn;
    @FXML private Button searchButton;
    
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
        
        // Tabla de radio
        if (radioTitleColumn != null) radioTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (radioArtistColumn != null) radioArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        
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
                    // Aquí se implementaría el popup de autocompletado
                    // Por simplicidad, se muestra en el status
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
     * 
     * @param usuario Usuario logueado
     */
    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
        
        // Actualizar datos del usuario en la interfaz
        actualizarDatosUsuario();
        
        // Cargar datos iniciales
        cargarFavoritos();
        cargarSeguidos();
        cargarSugerenciasUsuarios();
        
        System.out.println("Usuario establecido en dashboard: " + usuario.getUsername());
    }
    
    /**
     * RF-002: Actualiza los datos del usuario en la interfaz.
     */
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
    
    /**
     * RF-002: Maneja la actualización del perfil de usuario.
     */
    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) return;
        
        try {
            // Actualizar datos del usuario
            if (nombreCompletoField != null) {
                currentUser.setNombreCompleto(nombreCompletoField.getText().trim());
            }
            
            if (emailField != null) {
                currentUser.setEmail(emailField.getText().trim());
            }
            
            // Guardar cambios
            dataManager.saveAllData();
            
            mostrarStatus("Perfil actualizado correctamente");
            actualizarDatosUsuario();
            
        } catch (Exception e) {
            mostrarError("Error actualizando perfil: " + e.getMessage());
        }
    }
    
    /**
     * RF-002: Carga las canciones favoritas del usuario.
     */
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
    
    /**
     * RF-005: Genera playlist "Descubrimiento Semanal".
     */
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
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                mostrarError("Error generando descubrimiento: " + task.getException().getMessage());
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-006: Genera radio personalizada basada en canción semilla.
     */
    @FXML
    private void handleGenerateRadio() {
        if (currentUser == null || seedSongComboBox == null || radioTable == null) return;
        
        Cancion cancionSemilla = seedSongComboBox.getValue();
        if (cancionSemilla == null) {
            mostrarError("Selecciona una canción para crear la radio");
            return;
        }
        
        mostrarCargando(true);
        mostrarStatus("Creando radio basada en: " + cancionSemilla.getTitulo());
        
        Task<List<Cancion>> task = new Task<List<Cancion>>() {
            @Override
            protected List<Cancion> call() {
                return recommendationEngine.generarRadioPersonalizada(currentUser.getId(), cancionSemilla, 25);
            }
        };
        
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                List<Cancion> radioPlaylist = task.getValue();
                radioTable.setItems(FXCollections.observableArrayList(radioPlaylist));
                mostrarStatus("Radio generada: " + radioPlaylist.size() + " canciones");
                mostrarCargando(false);
            });
        });
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                mostrarError("Error generando radio: " + task.getException().getMessage());
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * RF-007: Maneja seguir a un usuario.
     */
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
                
                // Actualizar grafo social
                if (usuarioParaSeguir.getUsuariosSeguidos().contains(currentUser.getId())) {
                    // Seguimiento mutuo - crear conexión en grafo social
                    grafoSocial.conectarUsuarios(currentUser.getId(), usuarioParaSeguir.getId());
                }
                
                dataManager.saveAllData();
                
                mostrarStatus("Ahora sigues a " + usuarioParaSeguir.getUsername());
                actualizarDatosUsuario();
                cargarSeguidos();
                cargarSugerenciasUsuarios();
            } else {
                mostrarError("No se pudo seguir al usuario (ya lo seguías o error)");
            }
        } catch (Exception e) {
            mostrarError("Error siguiendo usuario: " + e.getMessage());
        }
    }
    
    /**
     * RF-007: Maneja dejar de seguir a un usuario.
     */
    @FXML
    private void handleUnfollowUser() {
        if (currentUser == null || followingTable == null) return;
        
        Usuario usuarioParaDejarDeSeguir = followingTable.getSelectionModel().getSelectedItem();
        if (usuarioParaDejarDeSeguir == null) {
            mostrarError("Selecciona un usuario para dejar de seguir");
            return;
        }
        
        try {
            boolean dejadoDeSeguir = currentUser.dejarDeSeguir(usuarioParaDejarDeSeguir.getId());
            if (dejadoDeSeguir) {
                usuarioParaDejarDeSeguir.removerSeguidor(currentUser.getId());
                
                // Actualizar grafo social
                grafoSocial.desconectarUsuarios(currentUser.getId(), usuarioParaDejarDeSeguir.getId());
                
                dataManager.saveAllData();
                
                mostrarStatus("Ya no sigues a " + usuarioParaDejarDeSeguir.getUsername());
                actualizarDatosUsuario();
                cargarSeguidos();
                cargarSugerenciasUsuarios();
            } else {
                mostrarError("No se pudo dejar de seguir al usuario");
            }
        } catch (Exception e) {
            mostrarError("Error dejando de seguir usuario: " + e.getMessage());
        }
    }
    
    /**
     * RF-008: Carga sugerencias de usuarios usando el grafo social.
     */
    private void cargarSugerenciasUsuarios() {
        if (currentUser == null || suggestedUsersTable == null) return;
        
        Task<ObservableList<Usuario>> task = new Task<ObservableList<Usuario>>() {
            @Override
            protected ObservableList<Usuario> call() {
                // Agregar usuario actual al grafo social si no está
                grafoSocial.agregarUsuario(currentUser);
                
                // Sincronizar grafo con datos de seguimiento
                grafoSocial.sincronizarConUsuarios();
                
                // RF-008: Obtener sugerencias usando BFS (amigos de amigos)
                List<Usuario> sugerencias = grafoSocial.obtenerSugerenciasUsuarios(currentUser.getId(), 10);
                
                return FXCollections.observableArrayList(sugerencias);
            }
        };
        
        task.setOnSucceeded(e -> {
            suggestedUsersTable.setItems(task.getValue());
            mostrarStatus("Sugerencias actualizadas: " + task.getValue().size() + " usuarios");
        });
        
        new Thread(task).start();
    }
    
    /**
     * Carga la lista de usuarios seguidos.
     */
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
    
    /**
     * RF-008: Actualiza sugerencias de usuarios.
     */
    @FXML
    private void handleRefreshSuggestions() {
        cargarSugerenciasUsuarios();
    }
    
    /**
     * Agrega canción seleccionada a favoritos.
     */
    @FXML
    private void handleAddToFavorites() {
        TableView<Cancion> tablaActiva = null;
        
        // Determinar qué tabla tiene selección
        if (discoveryTable != null && discoveryTable.getSelectionModel().getSelectedItem() != null) {
            tablaActiva = discoveryTable;
        } else if (radioTable != null && radioTable.getSelectionModel().getSelectedItem() != null) {
            tablaActiva = radioTable;
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
    
    /**
     * RF-002: Remueve canción seleccionada de favoritos.
     */
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
            } else {
                mostrarError("No se pudo remover la canción de favoritos");
            }
        } catch (Exception e) {
            mostrarError("Error removiendo de favoritos: " + e.getMessage());
        }
    }
    
    /**
     * RF-009: Exporta favoritos del usuario a CSV.
     */
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
        
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                mostrarError("Error en exportación: " + task.getException().getMessage());
                mostrarCargando(false);
            });
        });
        
        new Thread(task).start();
    }
    
    /**
     * Maneja la búsqueda de canciones.
     */
    @FXML
    private void handleSearch() {
        if (searchField == null || searchResultsTable == null) return;
        
        String termino = searchField.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Ingresa un término de búsqueda");
            return;
        }
        
        mostrarCargando(true);
        mostrarStatus("Buscando: " + termino);
        
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
    
    /**
     * Muestra mensaje de estado.
     * 
     * @param mensaje Mensaje a mostrar
     */
    private void mostrarStatus(String mensaje) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(mensaje);
                statusLabel.setStyle("-fx-text-fill: #1DB954;"); // Verde Spotify
            }
        });
    }
    
    /**
     * Muestra mensaje de error.
     * 
     * @param mensaje Mensaje de error
     */
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Error: " + mensaje);
                statusLabel.setStyle("-fx-text-fill: #FF6B6B;"); // Rojo error
            }
        });
    }
    
    /**
     * Controla la visibilidad del indicador de carga.
     * 
     * @param mostrar true para mostrar, false para ocultar
     */
    private void mostrarCargando(boolean mostrar) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(mostrar);
            }
        });
    }
    
    /**
     * Limpia los recursos al cerrar el controlador.
     */
    public void cleanup() {
        if (searchService != null) {
            searchService.cerrarServicio();
        }
    }
}