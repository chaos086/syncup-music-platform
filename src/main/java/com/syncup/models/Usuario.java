package com.syncup.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un usuario del sistema SyncUp.
 * Implementa funcionalidades para gestión de perfil, favoritos y red social.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class Usuario {
    
    /** Identificador único del usuario */
    private String id;
    
    /** Nombre de usuario */
    private String username;
    
    /** Contraseña del usuario (hash) */
    private String password;
    
    /** Nombre completo del usuario */
    private String nombreCompleto;
    
    /** Correo electrónico del usuario */
    private String email;
    
    /** Fecha de registro */
    private LocalDateTime fechaRegistro;
    
    /** Lista de canciones favoritas */
    private List<String> cancionesFavoritas;
    
    /** Lista de usuarios seguidos */
    private List<String> usuariosSeguidos;
    
    /** Lista de seguidores */
    private List<String> seguidores;
    
    /** Lista de playlists del usuario */
    private List<String> playlists;
    
    /** Preferencias musicales del usuario */
    private List<String> generosFavoritos;
    
    /** Estado activo del usuario */
    private boolean activo;
    
    /** Indica si es administrador */
    private boolean esAdmin;
    
    /**
     * Constructor completo para crear un nuevo usuario.
     * 
     * @param id Identificador único
     * @param username Nombre de usuario
     * @param password Contraseña
     * @param nombreCompleto Nombre completo
     * @param email Correo electrónico
     */
    public Usuario(String id, String username, String password, String nombreCompleto, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.fechaRegistro = LocalDateTime.now();
        this.cancionesFavoritas = new ArrayList<>();
        this.usuariosSeguidos = new ArrayList<>();
        this.seguidores = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.generosFavoritos = new ArrayList<>();
        this.activo = true;
        this.esAdmin = false;
    }
    
    /**
     * Constructor simple para crear usuario.
     * 
     * @param username Nombre de usuario
     * @param password Contraseña
     */
    public Usuario(String username, String password) {
        this(generateId(username), username, password, "", "");
    }
    
    /**
     * Genera un ID único para el usuario basado en el username y timestamp.
     * 
     * @param username Nombre de usuario
     * @return ID único generado
     */
    private static String generateId(String username) {
        return "user_" + username + "_" + System.currentTimeMillis();
    }
    
    /**
     * Agrega una canción a la lista de favoritas.
     * 
     * @param cancionId ID de la canción a agregar
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean agregarCancionFavorita(String cancionId) {
        if (!cancionesFavoritas.contains(cancionId)) {
            cancionesFavoritas.add(cancionId);
            return true;
        }
        return false;
    }
    
    /**
     * Remueve una canción de la lista de favoritas.
     * 
     * @param cancionId ID de la canción a remover
     * @return true si se removió exitosamente, false si no existía
     */
    public boolean removerCancionFavorita(String cancionId) {
        return cancionesFavoritas.remove(cancionId);
    }
    
    /**
     * Verifica si una canción está en favoritas.
     * 
     * @param cancionId ID de la canción
     * @return true si está en favoritas, false en caso contrario
     */
    public boolean esCancionFavorita(String cancionId) {
        return cancionesFavoritas.contains(cancionId);
    }
    
    /**
     * Sigue a otro usuario.
     * 
     * @param usuarioId ID del usuario a seguir
     * @return true si se siguió exitosamente, false si ya se seguía
     */
    public boolean seguirUsuario(String usuarioId) {
        if (!usuariosSeguidos.contains(usuarioId) && !usuarioId.equals(this.id)) {
            usuariosSeguidos.add(usuarioId);
            return true;
        }
        return false;
    }
    
    /**
     * Deja de seguir a un usuario.
     * 
     * @param usuarioId ID del usuario a dejar de seguir
     * @return true si se dejó de seguir exitosamente, false si no se seguía
     */
    public boolean dejarDeSeguir(String usuarioId) {
        return usuariosSeguidos.remove(usuarioId);
    }
    
    /**
     * Agrega un seguidor a la lista.
     * 
     * @param usuarioId ID del nuevo seguidor
     * @return true si se agregó exitosamente
     */
    public boolean agregarSeguidor(String usuarioId) {
        if (!seguidores.contains(usuarioId) && !usuarioId.equals(this.id)) {
            seguidores.add(usuarioId);
            return true;
        }
        return false;
    }
    
    /**
     * Remueve un seguidor de la lista.
     * 
     * @param usuarioId ID del seguidor a remover
     * @return true si se removió exitosamente
     */
    public boolean removerSeguidor(String usuarioId) {
        return seguidores.remove(usuarioId);
    }
    
    /**
     * Obtiene el número de canciones favoritas.
     * 
     * @return Número de canciones favoritas
     */
    public int getNumeroCancionesFavoritas() {
        return cancionesFavoritas.size();
    }
    
    /**
     * Obtiene el número de usuarios seguidos.
     * 
     * @return Número de usuarios seguidos
     */
    public int getNumeroSeguidos() {
        return usuariosSeguidos.size();
    }
    
    /**
     * Obtiene el número de seguidores.
     * 
     * @return Número de seguidores
     */
    public int getNumeroSeguidores() {
        return seguidores.size();
    }
    
    // Getters y Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public List<String> getCancionesFavoritas() {
        return new ArrayList<>(cancionesFavoritas);
    }
    
    public void setCancionesFavoritas(List<String> cancionesFavoritas) {
        this.cancionesFavoritas = new ArrayList<>(cancionesFavoritas);
    }
    
    public List<String> getUsuariosSeguidos() {
        return new ArrayList<>(usuariosSeguidos);
    }
    
    public void setUsuariosSeguidos(List<String> usuariosSeguidos) {
        this.usuariosSeguidos = new ArrayList<>(usuariosSeguidos);
    }
    
    public List<String> getSeguidores() {
        return new ArrayList<>(seguidores);
    }
    
    public void setSeguidores(List<String> seguidores) {
        this.seguidores = new ArrayList<>(seguidores);
    }
    
    public List<String> getPlaylists() {
        return new ArrayList<>(playlists);
    }
    
    public void setPlaylists(List<String> playlists) {
        this.playlists = new ArrayList<>(playlists);
    }
    
    public List<String> getGenerosFavoritos() {
        return new ArrayList<>(generosFavoritos);
    }
    
    public void setGenerosFavoritos(List<String> generosFavoritos) {
        this.generosFavoritos = new ArrayList<>(generosFavoritos);
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public boolean isEsAdmin() {
        return esAdmin;
    }
    
    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", email='" + email + '\'' +
                ", activo=" + activo +
                ", esAdmin=" + esAdmin +
                ", favoritas=" + cancionesFavoritas.size() +
                ", seguidos=" + usuariosSeguidos.size() +
                ", seguidores=" + seguidores.size() +
                '}';
    }
}