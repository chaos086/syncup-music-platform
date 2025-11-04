package com.syncup.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.data.UserRepository;
import com.syncup.data.MetricsService;
import com.syncup.services.ReportService;
import com.syncup.services.BulkDataLoader;
import com.syncup.utils.StyleManager;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    @FXML private Label adminWelcomeLabel;
    @FXML private Label adminStatsLabel;
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
    @FXML private Label bulkLoadStatusLabel;
    @FXML private Button selectFileButton;
    @FXML private Button validateFileButton;
    @FXML private Button bulkLoadButton;
    @FXML private TextArea bulkLoadResultsArea;
    @FXML private ProgressIndicator bulkLoadProgress;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalSongsLabel;
    @FXML private TextArea systemStatsArea;
    @FXML private Button refreshMetricsButton;
    @FXML private PieChart genreDistributionChart;
    @FXML private BarChart<String, Number> popularArtistsChart;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button logoutButton;

    private Label activeUsersLabel; // KPI extra
    private Label adminUsersLabel;  // KPI extra

    private Usuario currentUser;
    private File selectedBulkFile;
    private DataManager dataManager;
    private UserRepository userRepository;
    private MetricsService metricsService;
    private ReportService reportService;
    private BulkDataLoader bulkDataLoader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        userRepository = new UserRepository();
        metricsService = new MetricsService();
        reportService = new ReportService();
        bulkDataLoader = new BulkDataLoader();
        if (loadingIndicator != null) loadingIndicator.setVisible(false);
        if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false);
        setupTables();
    }

    private void setupTables() {
        if (catalogTitleColumn != null) catalogTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        if (catalogArtistColumn != null) catalogArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista"));
        if (catalogGenreColumn != null) catalogGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero"));
        if (catalogYearColumn != null) catalogYearColumn.setCellValueFactory(new PropertyValueFactory<>("anio"));
    }

    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
        actualizarDatosAdmin();
        cargarCatalogo();
        actualizarMetricas();
        generarGraficos();
    }

    private void actualizarDatosAdmin() {
        if (currentUser == null) return;
        if (adminWelcomeLabel != null) adminWelcomeLabel.setText("Panel Administrativo - " + currentUser.getNombreCompleto());
        if (adminStatsLabel != null) adminStatsLabel.setText("Admin: " + currentUser.getUsername());
    }

    private void cargarCatalogo() {
        if (catalogTable == null) return;
        Task<ObservableList<Cancion>> task = new Task<>() { @Override protected ObservableList<Cancion> call() { return FXCollections.observableArrayList(dataManager.getAllCanciones()); } };
        task.setOnSucceeded(e -> catalogTable.setItems(task.getValue()));
        new Thread(task).start();
    }

    @FXML private void handleAddSong() {
        try {
            String t = newSongTitleField.getText().trim();
            String a = newSongArtistField.getText().trim();
            String g = newSongGenreField.getText().trim();
            int y = Integer.parseInt(newSongYearField.getText().trim());
            if (t.isEmpty() || a.isEmpty() || g.isEmpty()) { mostrarError("Campos vacíos"); return; }
            Cancion c = new Cancion(t,a,g,y);
            if (dataManager.addCancion(c)) { mostrarStatus("Canción agregada"); cargarCatalogo(); actualizarMetricas(); }
        } catch (Exception ex) { mostrarError("Datos inválidos"); }
    }

    @FXML private void handleDeleteSong() {
        if (catalogTable == null) return;
        Cancion sel = catalogTable.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarError("Selecciona una canción"); return; }
        if (dataManager.removeCancion(sel.getId())) { mostrarStatus("Canción eliminada"); cargarCatalogo(); actualizarMetricas(); }
    }

    @FXML private void handleSelectFile() {
        FileChooser ch = new FileChooser(); ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt","*.tsv"));
        selectedBulkFile = ch.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedBulkFile != null && bulkLoadStatusLabel != null) bulkLoadStatusLabel.setText(selectedBulkFile.getName());
    }

    @FXML private void handleValidateFile() {
        if (selectedBulkFile == null) { mostrarError("Selecciona archivo"); return; }
        Task<BulkDataLoader.ResultadoValidacion> t = new Task<>(){ @Override protected BulkDataLoader.ResultadoValidacion call(){ return bulkDataLoader.validarArchivoFormato(selectedBulkFile.getAbsolutePath()); } };
        t.setOnSucceeded(e -> { if (bulkLoadResultsArea!=null) bulkLoadResultsArea.setText(t.getValue().mensaje); }); new Thread(t).start();
    }

    @FXML private void handleBulkLoad() {
        if (selectedBulkFile == null) { mostrarError("Selecciona archivo"); return; }
        if (bulkLoadProgress != null) bulkLoadProgress.setVisible(true);
        Task<BulkDataLoader.ResultadoCargaMasiva> t = new Task<>(){ @Override protected BulkDataLoader.ResultadoCargaMasiva call(){ String adminId = currentUser != null ? currentUser.getId() : "admin"; return bulkDataLoader.cargarCancionesMasivas(selectedBulkFile.getAbsolutePath(), adminId); } };
        t.setOnSucceeded(e -> { if (bulkLoadResultsArea!=null) bulkLoadResultsArea.setText(t.getValue().mensaje); if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false); cargarCatalogo(); actualizarMetricas(); generarGraficos(); }); new Thread(t).start();
    }

    @FXML private void handleRefreshMetrics() { actualizarMetricas(); generarGraficos(); }

    @FXML private void handleLogout() { try { FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml")); Parent root = loader.load(); Scene scene = new Scene(root, 1200, 800); StyleManager.applySpotifyTheme(scene); Stage stage = (Stage) (logoutButton != null ? logoutButton.getScene().getWindow() : statusLabel.getScene().getWindow()); stage.setScene(scene); stage.setTitle("SyncUp - Login"); stage.centerOnScreen(); System.out.println("Admin \"" + currentUser.getUsername() + "\" cerró sesión"); } catch (Exception ex) { System.err.println("Error volviendo al login: " + ex); mostrarError("Error cerrando sesión"); } }

    private void actualizarMetricas() {
        int total = metricsService.getTotalUsers();
        int activos = metricsService.getActiveUsers();
        int admins = metricsService.getAdminUsers();
        if (totalUsersLabel!=null) totalUsersLabel.setText(String.valueOf(total));
        if (totalSongsLabel!=null) totalSongsLabel.setText(String.valueOf(dataManager.getAllCanciones().size()));
        if (systemStatsArea!=null) systemStatsArea.setText("Usuarios totales:"+total+"\nUsuarios activos:"+activos+"\nAdmins:"+admins+"\nCanciones:"+dataManager.getAllCanciones().size());
        
        // KPIs visibles en UI si existen labels dedicados
        if (activeUsersLabel!=null) activeUsersLabel.setText(String.valueOf(activos));
        if (adminUsersLabel!=null) adminUsersLabel.setText(String.valueOf(admins));
    }

    private void generarGraficos() {
        if (genreDistributionChart != null) {
            Map<String, Long> m = dataManager.getAllCanciones().stream().collect(Collectors.groupingBy(Cancion::getGenero, Collectors.counting()));
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            for (Map.Entry<String, Long> e : m.entrySet()) data.add(new PieChart.Data(e.getKey(), e.getValue()));
            genreDistributionChart.setData(data);
        }
        if (popularArtistsChart != null) {
            Map<String, Long> a = dataManager.getAllCanciones().stream().collect(Collectors.groupingBy(Cancion::getArtista, Collectors.counting()));
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            a.entrySet().stream().limit(10).forEach(e -> s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));
            popularArtistsChart.getData().clear(); popularArtistsChart.getData().add(s);
        }
    }

    private void mostrarStatus(String m) { if (statusLabel!=null) statusLabel.setText(m); }
    private void mostrarError(String m) { if (statusLabel!=null) { statusLabel.setText("Error: "+m); } }
}
