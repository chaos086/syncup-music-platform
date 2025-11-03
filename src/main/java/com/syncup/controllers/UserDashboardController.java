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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    @FXML private Button logoutButton; @FXML private Button btnProfileTop;
    @FXML private TextField searchField; @FXML private Button searchButton; @FXML private Button generateDiscoveryButton;
    @FXML private TableView<Cancion> songsTable; @FXML private TableColumn<Cancion,String> titleColumn; @FXML private TableColumn<Cancion,String> artistColumn; @FXML private TableColumn<Cancion,String> genreColumn; @FXML private TableColumn<Cancion,Integer> yearColumn; @FXML private TableColumn<Cancion,Void> coverColumn; @FXML private TableColumn<Cancion,String> descColumn;
    @FXML private Button addToFavoritesButton;
    @FXML private TableView<Cancion> favoritesTable; @FXML private TableColumn<Cancion,Void> favCoverColumn; @FXML private TableColumn<Cancion,String> favTitleColumn; @FXML private TableColumn<Cancion,String> favArtistColumn; @FXML private TableColumn<Cancion,String> favGenreColumn; @FXML private TableColumn<Cancion,String> favDescColumn;
    @FXML private VBox catalogPane; @FXML private VBox favoritesPane; @FXML private VBox profilePane;
    @FXML private Label welcomeLabel; @FXML private Label userStatsLabel; @FXML private Label statusLabel; @FXML private ProgressIndicator loadingIndicator;
    @FXML private ImageView playerCover; @FXML private Label playerTitle; @FXML private Label playerArtist; @FXML private Label playerCurrent; @FXML private Label playerTotal; @FXML private Slider playerSeek; @FXML private Slider playerVolume; @FXML private Button btnPrev; @FXML private Button btnPlayPause; @FXML private Button btnNext;

    // Perfil
    @FXML private Label profileName; @FXML private Label profileUsername; @FXML private Label profileEmail; @FXML private Label profileFollowing; @FXML private Label profileFollowers; @FXML private ListView<String> albumsList;

    private Usuario currentUser; private DataManager dataManager; private RecommendationEngine recommendationEngine;
    private List<Cancion> currentQueue = new ArrayList<>(); private int currentIndex=-1; private boolean isPlaying=false; private int durationSeconds=0; private int currentSeconds=0; private Timeline progressTimer;

    @Override public void initialize(URL location, ResourceBundle resources){ dataManager=DataManager.getInstance(); recommendationEngine=new RecommendationEngine(); if(loadingIndicator!=null) loadingIndicator.setVisible(false); setupTables(); setupPlayer(); }

    private void setupTables(){
        if(titleColumn!=null){ titleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo")); titleColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(artistColumn!=null){ artistColumn.setCellValueFactory(new PropertyValueFactory<>("artista")); artistColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(genreColumn!=null){ genreColumn.setCellValueFactory(new PropertyValueFactory<>("genero")); genreColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(yearColumn!=null){ yearColumn.setCellValueFactory(new PropertyValueFactory<>("anio")); yearColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(descColumn!=null){ descColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion")); descColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(favTitleColumn!=null){ favTitleColumn.setCellValueFactory(new PropertyValueFactory<>("titulo")); favTitleColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(favArtistColumn!=null){ favArtistColumn.setCellValueFactory(new PropertyValueFactory<>("artista")); favArtistColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(favGenreColumn!=null){ favGenreColumn.setCellValueFactory(new PropertyValueFactory<>("genero")); favGenreColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }
        if(favDescColumn!=null){ favDescColumn.setCellValueFactory(new PropertyValueFactory<>("descripcion")); favDescColumn.setStyle("-fx-alignment: CENTER_LEFT;"); }

        // Imágenes: llenar el espacio 48x48, centradas
        Callback<TableColumn<Cancion, Void>, TableCell<Cancion, Void>> factory = TableCells.coverCellFactory();
        if(coverColumn!=null) coverColumn.setCellFactory(factory);
        if(favCoverColumn!=null) favCoverColumn.setCellFactory(factory);

        if (songsTable != null) songsTable.setFixedCellSize(52); // altura consistente
        if (favoritesTable != null) favoritesTable.setFixedCellSize(52);

        if (songsTable != null) songsTable.setOnMouseClicked(e -> { Cancion c = songsTable.getSelectionModel().getSelectedItem(); if (c != null) startPlaybackFrom(c, songsTable.getItems()); });
        if (favoritesTable != null) favoritesTable.setOnMouseClicked(e -> { Cancion c = favoritesTable.getSelectionModel().getSelectedItem(); if (c != null) startPlaybackFrom(c, favoritesTable.getItems()); });
    }

    private void setupPlayer(){ if (playerVolume!=null) playerVolume.setValue(70); if(playerSeek!=null) playerSeek.valueChangingProperty().addListener((o,oldV,newV)->{ if(!newV){ currentSeconds=(int)playerSeek.getValue(); updatePlayerTime(); }}); }

    public void setCurrentUser(Usuario usuario){ this.currentUser=usuario; actualizarUI(); cargarCanciones(); cargarFavoritos(); cargarPerfil(); }

    private void actualizarUI(){ if(currentUser==null) return; Platform.runLater(()->{ if(welcomeLabel!=null) welcomeLabel.setText("¡Bienvenido, "+currentUser.getNombreCompleto()+"!"); if(userStatsLabel!=null){ String stats=String.format("Favoritas: %d | Usuario: %s", currentUser.getNumeroCancionesFavoritas(), currentUser.getUsername()); userStatsLabel.setText(stats);} }); }

    private void cargarCanciones(){ if(songsTable==null) return; Task<ObservableList<Cancion>> t=new Task<>(){ @Override protected ObservableList<Cancion> call(){ return FXCollections.observableArrayList(dataManager.getAllCanciones()); } }; t.setOnSucceeded(e-> songsTable.setItems(t.getValue())); new Thread(t).start(); }

    private void cargarFavoritos(){ if(currentUser==null||favoritesTable==null) return; Task<ObservableList<Cancion>> t=new Task<>(){ @Override protected ObservableList<Cancion> call(){ List<Cancion> fav=new ArrayList<>(); for(String id: currentUser.getCancionesFavoritas()){ Cancion c=dataManager.getCancionById(id); if(c!=null) fav.add(c);} return FXCollections.observableArrayList(fav);} }; t.setOnSucceeded(e-> favoritesTable.setItems(t.getValue())); new Thread(t).start(); }

    private void cargarPerfil(){ if(currentUser==null) return; if(profileName!=null) profileName.setText(currentUser.getNombreCompleto()); if(profileUsername!=null) profileUsername.setText(currentUser.getUsername()); if(profileEmail!=null) profileEmail.setText(currentUser.getEmail()!=null?currentUser.getEmail():"—"); if(profileFollowing!=null) profileFollowing.setText(String.valueOf(dataManager.getSeguidosCount(currentUser.getId()))); if(profileFollowers!=null) profileFollowers.setText(String.valueOf(dataManager.getSeguidoresCount(currentUser.getId()))); if(albumsList!=null) albumsList.setItems(FXCollections.observableArrayList(dataManager.getAlbumsByUser(currentUser.getId()))); }

    // Sidebar
    @FXML private void handleSidebarHome(){ toggleViews(catalogPane); }
    @FXML private void handleSidebarFavorites(){ cargarFavoritos(); toggleViews(favoritesPane); }
    @FXML private void handleSidebarDiscover(){ handleGenerateDiscovery(); toggleViews(catalogPane); }
    @FXML private void handleSidebarSearchFocus(){ if(searchField!=null) searchField.requestFocus(); }
    @FXML private void handleSidebarProfile(){ toggleViews(profilePane); }

    private void toggleViews(VBox toShow){ if(catalogPane!=null){ catalogPane.setVisible(false); catalogPane.setManaged(false);} if(favoritesPane!=null){ favoritesPane.setVisible(false); favoritesPane.setManaged(false);} if(profilePane!=null){ profilePane.setVisible(false); profilePane.setManaged(false);} if(toShow!=null){ toShow.setVisible(true); toShow.setManaged(true);} }

    // Search & Discovery
    @FXML private void handleSearch(){ if(searchField==null||songsTable==null) return; String q=searchField.getText().trim().toLowerCase(); if(q.isEmpty()){ cargarCanciones(); return; } List<Cancion> res = dataManager.getAllCanciones().stream().filter(c-> c.getTitulo().toLowerCase().contains(q)||c.getArtista().toLowerCase().contains(q)||c.getGenero().toLowerCase().contains(q)).collect(Collectors.toList()); songsTable.setItems(FXCollections.observableArrayList(res)); }
    @FXML private void handleGenerateDiscovery(){ if(currentUser==null) return; Task<List<Cancion>> t=new Task<>(){ @Override protected List<Cancion> call(){ return recommendationEngine.generarDescubrimientoSemanal(currentUser.getId(),20);} }; t.setOnSucceeded(e->{ List<Cancion> recs=t.getValue(); if(songsTable!=null) songsTable.setItems(FXCollections.observableArrayList(recs)); currentQueue=recs; currentIndex=-1;}); new Thread(t).start(); }

    @FXML private void handleAddToFavorites(){ if(currentUser==null||songsTable==null) return; Cancion sel=songsTable.getSelectionModel().getSelectedItem(); if(sel==null){ setStatus("Selecciona una canción"); return;} boolean ok=currentUser.agregarCancionFavorita(sel.getId()); if(ok){ setStatus("Añadida a favoritos"); cargarFavoritos(); actualizarUI(); } else setStatus("Ya estaba en favoritos"); }

    // Player controls (simulados)
    @FXML private void handlePlayPause(){ if(currentIndex<0 && songsTable!=null && songsTable.getItems()!=null && !songsTable.getItems().isEmpty()){ startPlaybackFrom(songsTable.getItems().get(0), songsTable.getItems()); return;} isPlaying=!isPlaying; btnPlayPause.setText(isPlaying?"⏸":"▶"); if(isPlaying) startTimer(); else stopTimer(); }
    @FXML private void handleNext(){ if(currentQueue==null||currentQueue.isEmpty()) return; currentIndex=(currentIndex+1)%currentQueue.size(); applySong(currentQueue.get(currentIndex)); }
    @FXML private void handlePrev(){ if(currentQueue==null||currentQueue.isEmpty()) return; currentIndex=(currentIndex-1+currentQueue.size())%currentQueue.size(); applySong(currentQueue.get(currentIndex)); }

    private void startPlaybackFrom(Cancion c, List<Cancion> queue){ currentQueue=new ArrayList<>(queue); currentIndex=currentQueue.indexOf(c); if(currentIndex<0) currentIndex=0; isPlaying=true; btnPlayPause.setText("⏸"); applySong(currentQueue.get(currentIndex)); }
    private void applySong(Cancion c){ if(playerTitle!=null) playerTitle.setText(c.getTitulo()); if(playerArtist!=null) playerArtist.setText(c.getArtista()); Image img; String url=c.getCoverUrl(); if(url!=null && !url.isEmpty()) img=new Image(url,48,48,false,true,true); else img=new Image(getClass().getResourceAsStream("/images/cover-placeholder.png"),48,48,false,true); if(playerCover!=null){ playerCover.setFitWidth(48); playerCover.setFitHeight(48); playerCover.setPreserveRatio(false); playerCover.setImage(img);} durationSeconds= c.getDuracionSegundos()>0? c.getDuracionSegundos():210; currentSeconds=0; if(playerSeek!=null){ playerSeek.setMax(durationSeconds); playerSeek.setValue(0);} updatePlayerTime(); startTimer(); }
    private void startTimer(){ stopTimer(); progressTimer=new Timeline(new KeyFrame(Duration.seconds(1),e->{ if(!isPlaying) return; currentSeconds=Math.min(currentSeconds+1,durationSeconds); if(playerSeek!=null) playerSeek.setValue(currentSeconds); updatePlayerTime(); if(currentSeconds>=durationSeconds) handleNext(); })); progressTimer.setCycleCount(Timeline.INDEFINITE); progressTimer.play(); }
    private void stopTimer(){ if(progressTimer!=null){ progressTimer.stop(); progressTimer=null; } }
    private void updatePlayerTime(){ if(playerCurrent!=null) playerCurrent.setText(formatTime(currentSeconds)); if(playerTotal!=null) playerTotal.setText(formatTime(durationSeconds)); }
    private String formatTime(int s){ int m=s/60; int r=s%60; return String.format("%d:%02d",m,r); }

    @FXML private void handleLogout(){ try{ FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/login.fxml")); Parent root=loader.load(); Scene scene=new Scene(root,1200,800); StyleManager.applySpotifyTheme(scene); Stage stage=(Stage) logoutButton.getScene().getWindow(); stage.setScene(scene); stage.setTitle("SyncUp - Login"); stage.centerOnScreen(); } catch(Exception ex){ System.err.println("Error volviendo al login: "+ex);} }

    private void setStatus(String m){ if(statusLabel!=null) statusLabel.setText(m); }
}
