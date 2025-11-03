package com.syncup.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Cancion {
    private String id;
    private String titulo;
    private String artista;
    private String album;
    private String genero;
    private int anio;
    private int duracionSegundos;
    private LocalDate fechaAgregado;
    private long reproducciones;
    private long numeroFavoritos;
    private List<String> artistasColaboradores;
    private String rutaArchivo;
    private String urlImagenAlbum;
    private String coverUrl; // url de carátula
    private String descripcion; // NUEVO: créditos / disquera / álbum
    private double calificacionPromedio;
    private int numeroCalificaciones;
    private boolean disponible;
    private boolean contenidoExplicito;

    public Cancion(String id, String titulo, String artista, String album, String genero, int anio, int duracionSegundos) {
        this.id = id; this.titulo = titulo; this.artista = artista; this.album = album; this.genero = genero; this.anio = anio; this.duracionSegundos = duracionSegundos;
        this.fechaAgregado = LocalDate.now(); this.reproducciones = 0; this.numeroFavoritos = 0; this.artistasColaboradores = new ArrayList<>();
        this.rutaArchivo = ""; this.urlImagenAlbum = ""; this.coverUrl = ""; this.descripcion = ""; this.calificacionPromedio = 0.0; this.numeroCalificaciones = 0; this.disponible = true; this.contenidoExplicito = false;
    }

    public Cancion(String titulo, String artista, String genero, int anio) { this(generateId(titulo, artista), titulo, artista, "", genero, anio, 0); }

    private static String generateId(String titulo, String artista) {
        String base = (titulo + "_" + artista).replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return "song_" + base + "_" + System.currentTimeMillis();
    }

    public void reproducir() { this.reproducciones++; }
    public void marcarComoFavorita() { this.numeroFavoritos++; }
    public void desmarcarComoFavorita() { if (this.numeroFavoritos > 0) this.numeroFavoritos--; }

    public void agregarCalificacion(int calificacion) {
        if (calificacion >= 1 && calificacion <= 5) {
            double totalPuntos = this.calificacionPromedio * this.numeroCalificaciones;
            this.numeroCalificaciones++;
            this.calificacionPromedio = (totalPuntos + calificacion) / this.numeroCalificaciones;
        }
    }

    public boolean agregarArtistaColaborador(String artistaColaborador) { if (!artistasColaboradores.contains(artistaColaborador)) { artistasColaboradores.add(artistaColaborador); return true; } return false; }
    public boolean removerArtistaColaborador(String artistaColaborador) { return artistasColaboradores.remove(artistaColaborador); }

    public String getDuracionFormateada() { int minutos = duracionSegundos / 60; int segundos = duracionSegundos % 60; return String.format("%d:%02d", minutos, segundos); }

    public List<String> getTodosLosArtistas() { List<String> t = new ArrayList<>(); t.add(artista); t.addAll(artistasColaboradores); return t; }

    public boolean coincideConBusqueda(String termino) { if (termino == null || termino.trim().isEmpty()) return true; String s = termino.toLowerCase(); return titulo.toLowerCase().contains(s) || artista.toLowerCase().contains(s) || album.toLowerCase().contains(s) || genero.toLowerCase().contains(s) || artistasColaboradores.stream().anyMatch(a -> a.toLowerCase().contains(s)); }

    public double calcularPuntajePopularidad() { return (reproducciones * 1.0) + (numeroFavoritos * 2.0) + (calificacionPromedio * 100.0); }

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getTitulo() { return titulo; } public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getArtista() { return artista; } public void setArtista(String artista) { this.artista = artista; }
    public String getAlbum() { return album; } public void setAlbum(String album) { this.album = album; }
    public String getGenero() { return genero; } public void setGenero(String genero) { this.genero = genero; }
    public int getAnio() { return anio; } public void setAnio(int anio) { this.anio = anio; }
    public int getDuracionSegundos() { return duracionSegundos; } public void setDuracionSegundos(int duracionSegundos) { this.duracionSegundos = duracionSegundos; }
    public LocalDate getFechaAgregado() { return fechaAgregado; } public void setFechaAgregado(LocalDate fechaAgregado) { this.fechaAgregado = fechaAgregado; }
    public long getReproducciones() { return reproducciones; } public void setReproducciones(long reproducciones) { this.reproducciones = reproducciones; }
    public long getNumeroFavoritos() { return numeroFavoritos; } public void setNumeroFavoritos(long numeroFavoritos) { this.numeroFavoritos = numeroFavoritos; }
    public List<String> getArtistasColaboradores() { return new ArrayList<>(artistasColaboradores); } public void setArtistasColaboradores(List<String> artistasColaboradores) { this.artistasColaboradores = new ArrayList<>(artistasColaboradores); }
    public String getRutaArchivo() { return rutaArchivo; } public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
    public String getUrlImagenAlbum() { return urlImagenAlbum; } public void setUrlImagenAlbum(String urlImagenAlbum) { this.urlImagenAlbum = urlImagenAlbum; }
    public String getCoverUrl() { return coverUrl; } public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getCalificacionPromedio() { return calificacionPromedio; } public void setCalificacionPromedio(double calificacionPromedio) { this.calificacionPromedio = calificacionPromedio; }
    public int getNumeroCalificaciones() { return numeroCalificaciones; } public void setNumeroCalificaciones(int numeroCalificaciones) { this.numeroCalificaciones = numeroCalificaciones; }
    public boolean isDisponible() { return disponible; } public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public boolean isContenidoExplicito() { return contenidoExplicito; } public void setContenidoExplicito(boolean contenidoExplicito) { this.contenidoExplicito = contenidoExplicito; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Cancion cancion = (Cancion) o; return Objects.equals(id, cancion.id); }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return "Cancion{" + "id='" + id + '\'' + ", titulo='" + titulo + '\'' + ", artista='" + artista + '\'' + ", album='" + album + '\'' + ", genero='" + genero + '\'' + ", anio=" + anio + ", duracion='" + getDuracionFormateada() + '\'' + ", reproducciones=" + reproducciones + ", favoritos=" + numeroFavoritos + ", calificacion=" + String.format("%.1f", calificacionPromedio) + ", disponible=" + disponible + '}'; }
}
