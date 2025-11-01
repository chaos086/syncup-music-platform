package com.syncup.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa una canción en el sistema SyncUp.
 * Contiene toda la información relevante de una canción incluyendo metadatos
 * y estadísticas de reproducción.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class Cancion {
    
    /** Identificador único de la canción */
    private String id;
    
    /** Título de la canción */
    private String titulo;
    
    /** Artista principal */
    private String artista;
    
    /** Álbum al que pertenece */
    private String album;
    
    /** Género musical */
    private String genero;
    
    /** Año de lanzamiento */
    private int anio;
    
    /** Duración en segundos */
    private int duracionSegundos;
    
    /** Fecha de agregado al sistema */
    private LocalDate fechaAgregado;
    
    /** Número de reproducciones */
    private long reproducciones;
    
    /** Número de veces marcada como favorita */
    private long numeroFavoritos;
    
    /** Lista de artistas colaboradores */
    private List<String> artistasColaboradores;
    
    /** URL o ruta del archivo de audio */
    private String rutaArchivo;
    
    /** URL de la imagen/cover del álbum */
    private String urlImagenAlbum;
    
    /** Calificación promedio (1-5 estrellas) */
    private double calificacionPromedio;
    
    /** Número de calificaciones */
    private int numeroCalificaciones;
    
    /** Indica si la canción está disponible */
    private boolean disponible;
    
    /** Indica si es contenido explícito */
    private boolean contenidoExplicito;
    
    /**
     * Constructor completo para crear una nueva canción.
     * 
     * @param id Identificador único
     * @param titulo Título de la canción
     * @param artista Artista principal
     * @param album Álbum
     * @param genero Género musical
     * @param anio Año de lanzamiento
     * @param duracionSegundos Duración en segundos
     */
    public Cancion(String id, String titulo, String artista, String album, String genero, int anio, int duracionSegundos) {
        this.id = id;
        this.titulo = titulo;
        this.artista = artista;
        this.album = album;
        this.genero = genero;
        this.anio = anio;
        this.duracionSegundos = duracionSegundos;
        this.fechaAgregado = LocalDate.now();
        this.reproducciones = 0;
        this.numeroFavoritos = 0;
        this.artistasColaboradores = new ArrayList<>();
        this.rutaArchivo = "";
        this.urlImagenAlbum = "";
        this.calificacionPromedio = 0.0;
        this.numeroCalificaciones = 0;
        this.disponible = true;
        this.contenidoExplicito = false;
    }
    
    /**
     * Constructor simplificado para crear una canción.
     * 
     * @param titulo Título de la canción
     * @param artista Artista principal
     * @param genero Género musical
     * @param anio Año de lanzamiento
     */
    public Cancion(String titulo, String artista, String genero, int anio) {
        this(generateId(titulo, artista), titulo, artista, "", genero, anio, 0);
    }
    
    /**
     * Genera un ID único para la canción basado en título y artista.
     * 
     * @param titulo Título de la canción
     * @param artista Artista principal
     * @return ID único generado
     */
    private static String generateId(String titulo, String artista) {
        String base = (titulo + "_" + artista).replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return "song_" + base + "_" + System.currentTimeMillis();
    }
    
    /**
     * Incrementa el contador de reproducciones.
     */
    public void reproducir() {
        this.reproducciones++;
    }
    
    /**
     * Incrementa el contador de favoritos.
     */
    public void marcarComoFavorita() {
        this.numeroFavoritos++;
    }
    
    /**
     * Decrementa el contador de favoritos.
     */
    public void desmarcarComoFavorita() {
        if (this.numeroFavoritos > 0) {
            this.numeroFavoritos--;
        }
    }
    
    /**
     * Agrega una nueva calificación y recalcula el promedio.
     * 
     * @param calificacion Calificación del 1 al 5
     */
    public void agregarCalificacion(int calificacion) {
        if (calificacion >= 1 && calificacion <= 5) {
            double totalPuntos = this.calificacionPromedio * this.numeroCalificaciones;
            this.numeroCalificaciones++;
            this.calificacionPromedio = (totalPuntos + calificacion) / this.numeroCalificaciones;
        }
    }
    
    /**
     * Agrega un artista colaborador.
     * 
     * @param artistaColaborador Nombre del artista colaborador
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean agregarArtistaColaborador(String artistaColaborador) {
        if (!artistasColaboradores.contains(artistaColaborador)) {
            artistasColaboradores.add(artistaColaborador);
            return true;
        }
        return false;
    }
    
    /**
     * Remueve un artista colaborador.
     * 
     * @param artistaColaborador Nombre del artista colaborador
     * @return true si se removió exitosamente, false si no existía
     */
    public boolean removerArtistaColaborador(String artistaColaborador) {
        return artistasColaboradores.remove(artistaColaborador);
    }
    
    /**
     * Obtiene la duración formateada como mm:ss.
     * 
     * @return Duración formateada
     */
    public String getDuracionFormateada() {
        int minutos = duracionSegundos / 60;
        int segundos = duracionSegundos % 60;
        return String.format("%d:%02d", minutos, segundos);
    }
    
    /**
     * Obtiene todos los artistas (principal + colaboradores).
     * 
     * @return Lista con todos los artistas
     */
    public List<String> getTodosLosArtistas() {
        List<String> todosArtistas = new ArrayList<>();
        todosArtistas.add(artista);
        todosArtistas.addAll(artistasColaboradores);
        return todosArtistas;
    }
    
    /**
     * Verifica si la canción coincide con los criterios de búsqueda.
     * 
     * @param termino Término de búsqueda
     * @return true si coincide, false en caso contrario
     */
    public boolean coincideConBusqueda(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return true;
        }
        
        String terminoLower = termino.toLowerCase();
        return titulo.toLowerCase().contains(terminoLower) ||
               artista.toLowerCase().contains(terminoLower) ||
               album.toLowerCase().contains(terminoLower) ||
               genero.toLowerCase().contains(terminoLower) ||
               artistasColaboradores.stream().anyMatch(a -> a.toLowerCase().contains(terminoLower));
    }
    
    /**
     * Calcula un puntaje de popularidad basado en reproducciones y favoritos.
     * 
     * @return Puntaje de popularidad
     */
    public double calcularPuntajePopularidad() {
        // Fórmula: reproducciones * 1.0 + favoritos * 2.0 + calificación * 100
        return (reproducciones * 1.0) + (numeroFavoritos * 2.0) + (calificacionPromedio * 100.0);
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getArtista() {
        return artista;
    }
    
    public void setArtista(String artista) {
        this.artista = artista;
    }
    
    public String getAlbum() {
        return album;
    }
    
    public void setAlbum(String album) {
        this.album = album;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public int getAnio() {
        return anio;
    }
    
    public void setAnio(int anio) {
        this.anio = anio;
    }
    
    public int getDuracionSegundos() {
        return duracionSegundos;
    }
    
    public void setDuracionSegundos(int duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }
    
    public LocalDate getFechaAgregado() {
        return fechaAgregado;
    }
    
    public void setFechaAgregado(LocalDate fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }
    
    public long getReproducciones() {
        return reproducciones;
    }
    
    public void setReproducciones(long reproducciones) {
        this.reproducciones = reproducciones;
    }
    
    public long getNumeroFavoritos() {
        return numeroFavoritos;
    }
    
    public void setNumeroFavoritos(long numeroFavoritos) {
        this.numeroFavoritos = numeroFavoritos;
    }
    
    public List<String> getArtistasColaboradores() {
        return new ArrayList<>(artistasColaboradores);
    }
    
    public void setArtistasColaboradores(List<String> artistasColaboradores) {
        this.artistasColaboradores = new ArrayList<>(artistasColaboradores);
    }
    
    public String getRutaArchivo() {
        return rutaArchivo;
    }
    
    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }
    
    public String getUrlImagenAlbum() {
        return urlImagenAlbum;
    }
    
    public void setUrlImagenAlbum(String urlImagenAlbum) {
        this.urlImagenAlbum = urlImagenAlbum;
    }
    
    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }
    
    public void setCalificacionPromedio(double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
    
    public int getNumeroCalificaciones() {
        return numeroCalificaciones;
    }
    
    public void setNumeroCalificaciones(int numeroCalificaciones) {
        this.numeroCalificaciones = numeroCalificaciones;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public boolean isContenidoExplicito() {
        return contenidoExplicito;
    }
    
    public void setContenidoExplicito(boolean contenidoExplicito) {
        this.contenidoExplicito = contenidoExplicito;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancion cancion = (Cancion) o;
        return Objects.equals(id, cancion.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Cancion{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", artista='" + artista + '\'' +
                ", album='" + album + '\'' +
                ", genero='" + genero + '\'' +
                ", anio=" + anio +
                ", duracion='" + getDuracionFormateada() + '\'' +
                ", reproducciones=" + reproducciones +
                ", favoritos=" + numeroFavoritos +
                ", calificacion=" + String.format("%.1f", calificacionPromedio) +
                ", disponible=" + disponible +
                '}';
    }
}