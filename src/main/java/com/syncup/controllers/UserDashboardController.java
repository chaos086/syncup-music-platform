package com.syncup.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.algorithms.RecommendationEngine;
import com.syncup.utils.StyleManager;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserDashboardController implements Initializable {
    // Header + sidebar actions
    @FXML private Button logoutButton;

    // Search + catalog
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button generateDiscoveryButton;
    @FXML private TableView<Cancion> songsTable;
    @FXML private TableColumn<Cancion, String> titleColumn;
    @FXML private TableColumn<Cancion, String> artistColumn;
    @FXML private TableColumn<Cancion, String> genreColumn;
    @FXML private TableColumn<Cancion, Integer> yearColumn;
    @FXML private Button addToFavoritesButton;

    // Favorites view
    @FXML private TableView<Cancion> favoritesTable;
    @FXML private TableColumn<Cancion, String> favTitleColumn;
    @FXML private TableColumn<Cancion, String> favArtistColumn;
    @FXML private TableColumn<Cancion, String> favGenreColumn;

    @FXML private VBox catalogPane;
    @FXML private VBox favoritesPane;

    // Header labels and status
    @FXML private Label welcomeLabel;
    @FXML private Label userStatsLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // Player controls
    @FXML private ImageView playerCover;
    @FXML private Label playerTitle;
    @FXML private Label playerArtist;
    @FXML private Label playerCurrent;
    @FXML private Label playerTotal;
    @FXML private Slider playerSeek;
    @FXML private Slider playerVolume;

    @FXML private Button btnPrev;
    @FXML private Button btnPlayPause;
    @FXML private Button btnNext;

    private Usuario currentUser;
    private DataManager dataManager;
    private RecommendationEngine recommendationEngine;

    // Playback state (simulado)
    private List<Cancion> currentQueue = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false;
    private int durationSeconds = 0;
    private int currentSeconds = 0;
    private Timeline progressTimer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        recommendationEngine = new RecommendationEngine();
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        setupTables();
        setupPlayer();
    }

    private void setupTables() {
        if (titleColumn != null) titleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (artistColumn != null) artistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (genreColumn != null) genreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (yearColumn != null) yearColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
        if (favTitleColumn != null) favTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (favArtistColumn != null) favArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (favGenreColumn != null) favGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));

        if (songsTable != null) {
            songsTable.setOnMouseClicked(e -> {
                Cancion c = songsTable.getSelectionModel().getSelectedItem();
                if (c != null) startPlaybackFrom(c, dataManager.getAllCanciones());
            });
        }
        if (favoritesTable != null) {
            favoritesTable.setOnMouseClicked(e -> {
                Cancion c = favoritesTable.getSelectionModel().getSelectedItem();
                if (c != null) startPlaybackFrom(c, favoritesTable.getItems());
            });
        }
    }

    private void setupPlayer() {
        if (playerVolume != null) playerVolume.setValue(70);
        if (playerSeek != null) playerSeek.valueChangingProperty().addListener((o, oldV, newV) -> {
            if (!newV) { currentSeconds = (int) playerSeek.getValue(); updatePlayerTime(); }
        });
    }

    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
        actualizarUI();
        cargarCanciones();
        cargarFavoritos();
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
        task.setOnSucceeded(e -> songsTable.setItems(task.getValue()));
        new Thread(task).start();
    }

    private void cargarFavoritos() {
        if (currentUser == null || favoritesTable == null) return;
        Task<ObservableList<Cancion>> task = new Task<ObservableList<Cancion>>() {
            @Override protected ObservableList<Cancion> call() {
                List<Cancion> favoritas = new ArrayList<>();
                for (String id : currentUser.getCancionesFavoritas()) {
                    Cancion c = dataManager.getCancionById(id);
                    if (c != null) favoritas.add(c);
                }
                return FXCollections.observableArrayList(favoritas);
            }
        };
        task.setOnSucceeded(e -> favoritesTable.setItems(task.getValue()));
        new Thread(task).start();
    }

    // Sidebar handlers
    @FXML private void handleSidebarHome() { toggleViews(true); }
    @FXML private void handleSidebarFavorites() { cargarFavoritos(); toggleViews(false); }
    @FXML private void handleSidebarDiscover() { handleGenerateDiscovery(); }
    @FXML private void handleSidebarSearchFocus() { if (searchField!=null) searchField.requestFocus(); }

    private void toggleViews(boolean showCatalog) {
        if (catalogPane!=null && favoritesPane!=null) {
            catalogPane.setVisible(showCatalog); catalogPane.setManaged(showCatalog);
            favoritesPane.setVisible(!showCatalog); favoritesPane.setManaged(!showCatalog);
        }
    }

    // Search & Discovery
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
    }

    @FXML private void handleGenerateDiscovery() {
        if (currentUser == null) return;
        Task<List<Cancion>> task = new Task<List<Cancion>>() {
            @Override protected List<Cancion> call() { return recommendationEngine.generarDescubrimientoSemanal(currentUser.getId(), 20); }
        };
        task.setOnSucceeded(e -> {
            List<Cancion> recs = task.getValue();
            if (songsTable != null) songsTable.setItems(FXCollections.observableArrayList(recs));
            currentQueue = recs; currentIndex = -1; // preparada para reproducir
        });
        new Thread(task).start();
    }

    @FXML private void handleAddToFavorites() {
        if (currentUser == null || songsTable == null) return;
        Cancion sel = songsTable.getSelectionModel().getSelectedItem();
        if (sel == null) { setStatus("Selecciona una canción"); return; }
        boolean ok = currentUser.agregarCancionFavorita(sel.getId());
        if (ok) { setStatus("Añadida a favoritos"); cargarFavoritos(); actualizarUI(); }
        else setStatus("Ya estaba en favoritos");
    }

    // Player controls
    @FXML private void handlePlayPause() {
        if (currentIndex<0 && songsTable!=null && !songsTable.getItems().isEmpty()) {
            startPlaybackFrom(songsTable.getItems().get(0), songsTable.getItems());
            return;
        }
        isPlaying = !isPlaying;
        btnPlayPause.setText(isPlaying?"⏸":"▶");
        if (isPlaying) startTimer(); else stopTimer();
    }

    @FXML private void handleNext() {
        if (currentQueue==null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % currentQueue.size();
        applySong(currentQueue.get(currentIndex));
    }

    @FXML private void handlePrev() {
        if (currentQueue==null || currentQueue.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentQueue.size()) % currentQueue.size();
        applySong(currentQueue.get(currentIndex));
    }

    private void startPlaybackFrom(Cancion c, List<Cancion> queue) {
        currentQueue = new ArrayList<>(queue);
        currentIndex = currentQueue.indexOf(c);
        if (currentIndex<0) currentIndex = 0;
        isPlaying = true;
        btnPlayPause.setText("⏸");
        applySong(currentQueue.get(currentIndex));
    }

    private void applySong(Cancion c) {
        if (playerTitle!=null) playerTitle.setText(c.getTitulo());
        if (playerArtist!=null) playerArtist.setText(c.getArtista());
        if (playerCover!=null) playerCover.setImage(new Image(getClass().getResourceAsStream("/images/cover-placeholder.png"),40,40,true,true));
        durationSeconds = 210; // simulado
        currentSeconds = 0;
        if (playerSeek!=null) { playerSeek.setMax(durationSeconds); playerSeek.setValue(0); }
        updatePlayerTime();
        startTimer();
    }

    private void startTimer() {
        stopTimer();
        progressTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!isPlaying) return;
            currentSeconds = Math.min(currentSeconds+1, durationSeconds);
            if (playerSeek!=null) playerSeek.setValue(currentSeconds);
            updatePlayerTime();
            if (currentSeconds>=durationSeconds) handleNext();
        }));
        progressTimer.setCycleCount(Timeline.INDEFINITE);
        progressTimer.play();
    }

    private void stopTimer() { if (progressTimer!=null) { progressTimer.stop(); progressTimer = null; } }

    private void updatePlayerTime() {
        if (playerCurrent!=null) playerCurrent.setText(formatTime(currentSeconds));
        if (playerTotal!=null) playerTotal.setText(formatTime(durationSeconds));
    }

    private String formatTime(int s) { int m=s/60; int r=s%60; return String.format("%d:%02d",m,r); }

    // Logout
    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            StyleManager.applySpotifyTheme(scene);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("SyncUp - Login");
            stage.centerOnScreen();
        } catch (Exception ex) { System.err.println("Error volviendo al login: "+ex); }
    }

    private void setStatus(String m){ if(statusLabel!=null) statusLabel.setText(m); }
}
