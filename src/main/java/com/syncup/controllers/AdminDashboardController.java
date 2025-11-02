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

    private Admin currentAdmin;
    private File selectedBulkFile;
    private DataManager dataManager;
    private ReportService reportService;
    private BulkDataLoader bulkDataLoader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
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

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;
        actualizarDatosAdmin();
        cargarCatalogo();
        actualizarMetricas();
        generarGraficos();
    }

    private void actualizarDatosAdmin() {
        if (currentAdmin == null) return;
        if (adminWelcomeLabel != null) adminWelcomeLabel.setText("Panel Administrativo - " + currentAdmin.getNombreCompleto());
        if (adminStatsLabel != null) adminStatsLabel.setText("Admin");
    }

    private void cargarCatalogo() {
        if (catalogTable == null) return;
        Task<ObservableList<Cancion>> task = new Task<ObservableList<Cancion>>() {
            @Override protected ObservableList<Cancion> call() {
                return FXCollections.observableArrayList(dataManager.getAllCanciones());
            }
        };
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
        FileChooser ch = new FileChooser();
        ch.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt","*.tsv"));
        selectedBulkFile = ch.showOpenDialog(selectFileButton.getScene().getWindow());
        if (selectedBulkFile != null && bulkLoadStatusLabel != null) bulkLoadStatusLabel.setText(selectedBulkFile.getName());
    }

    @FXML private void handleValidateFile() {
        if (selectedBulkFile == null) { mostrarError("Selecciona archivo"); return; }
        Task<BulkDataLoader.ResultadoValidacion> t = new Task<BulkDataLoader.ResultadoValidacion>(){
            @Override protected BulkDataLoader.ResultadoValidacion call(){
                return bulkDataLoader.validarArchivoFormato(selectedBulkFile.getAbsolutePath());
            }
        };
        t.setOnSucceeded(e -> { if (bulkLoadResultsArea!=null) bulkLoadResultsArea.setText(t.getValue().mensaje); });
        new Thread(t).start();
    }

    @FXML private void handleBulkLoad() {
        if (selectedBulkFile == null) { mostrarError("Selecciona archivo"); return; }
        if (bulkLoadProgress != null) bulkLoadProgress.setVisible(true);
        Task<BulkDataLoader.ResultadoCargaMasiva> t = new Task<BulkDataLoader.ResultadoCargaMasiva>(){
            @Override protected BulkDataLoader.ResultadoCargaMasiva call(){
                String adminId = currentAdmin != null ? currentAdmin.getId() : "admin";
                return bulkDataLoader.cargarCancionesMasivas(selectedBulkFile.getAbsolutePath(), adminId);
            }
        };
        t.setOnSucceeded(e -> {
            if (bulkLoadResultsArea!=null) bulkLoadResultsArea.setText(t.getValue().mensaje);
            if (bulkLoadProgress != null) bulkLoadProgress.setVisible(false);
            cargarCatalogo(); actualizarMetricas(); generarGraficos();
        });
        new Thread(t).start();
    }

    @FXML private void handleRefreshMetrics() { actualizarMetricas(); }

    private void actualizarMetricas() {
        if (totalUsersLabel!=null) totalUsersLabel.setText(String.valueOf(dataManager.getAllUsuarios().size()));
        if (totalSongsLabel!=null) totalSongsLabel.setText(String.valueOf(dataManager.getAllCanciones().size()));
        if (systemStatsArea!=null) systemStatsArea.setText(dataManager.getSystemStats());
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
