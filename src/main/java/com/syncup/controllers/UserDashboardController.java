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
import javafx.geometry.Insets;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.algorithms.RecommendationEngine;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserDashboardController implements Initializable {
    // Componentes principales
    @FXML private Label welcomeLabel;
    @FXML private Label userStatsLabel;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView<Cancion> songsTable;
    @FXML private TableColumn<Cancion, String> titleColumn;
    @FXML private TableColumn<Cancion, String> artistColumn;
    @FXML private TableColumn<Cancion, String> genreColumn;
    @FXML private TableColumn<Cancion, Integer> yearColumn;
    @FXML private Button addToFavoritesButton;
    @FXML private Button generateDiscoveryButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private Usuario currentUser;
    private DataManager dataManager;
    private RecommendationEngine recommendationEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        recommendationEngine = new RecommendationEngine();
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        setupTables();
        System.out.println("UserDashboardController inicializado");
    }

    private void setupTables() {
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (artistColumn != null) artistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (genreColumn != null) genreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (yearColumn != null) yearColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
    }

    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
        actualizarUI();
        cargarCanciones();
        System.out.println("Usuario establecido: " + usuario.getUsername());
    }

    private void actualizarUI() {
        if (currentUser == null) return;
        Platform.runLater(() -> {
            if (welcomeLabel != null) welcomeLabel.setText("¡Bienvenido, " + currentUser.getNombreCompleto() + "!");
            if (userStatsLabel != null) {
                String stats = String.format("Favoritas: %d | Usuario: %s",
                    currentUser.getNumeroCancionesFavoritas(), currentUser.getUsername());
                userStatsLabel.setText(stats);
            }
        });
    }

    private void cargarCanciones() {
        if (songsTable == null) return;
        Task<ObservableList<Cancion>> task = new Task<ObservableList<Cancion>>() {
            @Override protected ObservableList<Cancion> call() {
                return FXCollections.observableArrayList(dataManager.getAllCanciones());
            }
        };
        task.setOnSucceeded(e -> {
            songsTable.setItems(task.getValue());
            mostrarStatus("Catálogo cargado: " + task.getValue().size() + " canciones");
        });
        new Thread(task).start();
    }

    @FXML private void handleSearch() {
        if (searchField == null || songsTable == null) return;
        String termino = searchField.getText().trim().toLowerCase();
        if (termino.isEmpty()) { cargarCanciones(); return; }
        
        List<Cancion> filtradas = dataManager.getAllCanciones().stream()
            .filter(c -> c.getTitulo().toLowerCase().contains(termino) || 
                        c.getArtista().toLowerCase().contains(termino) ||
                        c.getGenero().toLowerCase().contains(termino))
            .collect(Collectors.toList());
        
        songsTable.setItems(FXCollections.observableArrayList(filtradas));
        mostrarStatus("Búsqueda: " + filtradas.size() + " resultados");
    }

    @FXML private void handleAddToFavorites() {
        if (currentUser == null || songsTable == null) return;
        Cancion sel = songsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarError("Selecciona una canción"); return; }
        
        boolean agregada = currentUser.agregarCancionFavorita(sel.getId());
        if (agregada) {
            mostrarStatus("\"" + sel.getTitulo() + "\" agregada a favoritos");
            actualizarUI();
        } else {
            mostrarError("La canción ya está en favoritos");
        }
    }

    @FXML private void handleGenerateDiscovery() {
        if (currentUser == null) return;
        mostrarCargando(true);
        mostrarStatus("Generando Descubrimiento Semanal...");
        
        Task<List<Cancion>> task = new Task<List<Cancion>>() {
            @Override protected List<Cancion> call() {
                return recommendationEngine.generarDescubrimientoSemanal(currentUser.getId(), 20);
            }
        };
        
        task.setOnSucceeded(e -> {
            List<Cancion> recs = task.getValue();
            if (songsTable != null) songsTable.setItems(FXCollections.observableArrayList(recs));
            mostrarStatus("Descubrimiento Semanal: " + recs.size() + " canciones");
            mostrarCargando(false);
        });
        
        new Thread(task).start();
    }

    private void mostrarStatus(String m) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(m);
                statusLabel.setStyle("-fx-text-fill: #1DB954;");
            }
        });
    }

    private void mostrarError(String m) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Error: " + m);
                statusLabel.setStyle("-fx-text-fill: #FF6B6B;");
            }
        });
    }

    private void mostrarCargando(boolean mostrar) {
        Platform.runLater(() -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(mostrar);
        });
    }
}
