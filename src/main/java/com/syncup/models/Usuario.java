package com.syncup.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un usuario del sistema SyncUp.
 * Implementa funcionalidades para gestión de perfil, favoritos y red social.
 * RF-015, RF-016, RF-017: equals y hashCode basados en username para indexación eficiente.
 */
public class Usuario {

    private String id;
    private String username;
    /** Contraseña en texto claro (legado). No usar directamente. */
    private String password;
    /** Hash seguro de la contraseña */
    private String passwordHash;
    private String nombreCompleto;
    private String email;
    private LocalDateTime fechaRegistro;
    private List<String> cancionesFavoritas;
    private List<String> usuariosSeguidos;
    private List<String> seguidores;
    private List<String> playlists;
    private List<String> generosFavoritos;
    private boolean activo;
    private boolean esAdmin;

    public Usuario(String id, String username, String passwordOrHash, String nombreCompleto, String email) {
        this.id = id;
        this.username = username;
        // Mantener compatibilidad: si parece hash (empieza por "sha256:"), guardarlo en passwordHash; si no, en password
        if(passwordOrHash!=null && passwordOrHash.startsWith("sha256:")){
            this.passwordHash = passwordOrHash;
            this.password = null;
        } else {
            this.password = passwordOrHash;
            this.passwordHash = null;
        }
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

    public Usuario(String username, String password) { this(generateId(username), username, password, "", ""); }

    private static String generateId(String username) { return "user_" + username + "_" + System.currentTimeMillis(); }

    public boolean agregarCancionFavorita(String cancionId) { if (!cancionesFavoritas.contains(cancionId)) { cancionesFavoritas.add(cancionId); return true; } return false; }
    public boolean removerCancionFavorita(String cancionId) { return cancionesFavoritas.remove(cancionId); }
    public boolean esCancionFavorita(String cancionId) { return cancionesFavoritas.contains(cancionId); }
    public boolean seguirUsuario(String usuarioId) { if (!usuariosSeguidos.contains(usuarioId) && !usuarioId.equals(this.id)) { usuariosSeguidos.add(usuarioId); return true; } return false; }
    public boolean dejarDeSeguir(String usuarioId) { return usuariosSeguidos.remove(usuarioId); }
    public boolean agregarSeguidor(String usuarioId) { if (!seguidores.contains(usuarioId) && !usuarioId.equals(this.id)) { seguidores.add(usuarioId); return true; } return false; }
    public boolean removerSeguidor(String usuarioId) { return seguidores.remove(usuarioId); }
    public int getNumeroCancionesFavoritas() { return cancionesFavoritas.size(); }
    public int getNumeroSeguidos() { return usuariosSeguidos.size(); }
    public int getNumeroSeguidores() { return seguidores.size(); }

    // Getters/Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public List<String> getCancionesFavoritas() { return new ArrayList<>(cancionesFavoritas); }
    public void setCancionesFavoritas(List<String> cancionesFavoritas) { this.cancionesFavoritas = new ArrayList<>(cancionesFavoritas); }
    public List<String> getUsuariosSeguidos() { return new ArrayList<>(usuariosSeguidos); }
    public void setUsuariosSeguidos(List<String> usuariosSeguidos) { this.usuariosSeguidos = new ArrayList<>(usuariosSeguidos); }
    public List<String> getSeguidores() { return new ArrayList<>(seguidores); }
    public void setSeguidores(List<String> seguidores) { this.seguidores = new ArrayList<>(seguidores); }
    public List<String> getPlaylists() { return new ArrayList<>(playlists); }
    public void setPlaylists(List<String> playlists) { this.playlists = new ArrayList<>(playlists); }
    public List<String> getGenerosFavoritos() { return new ArrayList<>(generosFavoritos); }
    public void setGenerosFavoritos(List<String> generosFavoritos) { this.generosFavoritos = new ArrayList<>(generosFavoritos); }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isEsAdmin() { return esAdmin; }
    public void setEsAdmin(boolean esAdmin) { this.esAdmin = esAdmin; }

    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Usuario u = (Usuario) o; return Objects.equals(username, u.username); }
    @Override public int hashCode() { return Objects.hash(username); }
    @Override public String toString() { return "Usuario{" + "id='" + id + '\'' + ", username='" + username + '\'' + ", nombreCompleto='" + nombreCompleto + '\'' + ", email='" + email + '\'' + ", activo=" + activo + ", esAdmin=" + esAdmin + ", favoritas=" + cancionesFavoritas.size() + ", seguidos=" + usuariosSeguidos.size() + ", seguidores=" + seguidores.size() + '}'; }
}
