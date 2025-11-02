package com.syncup.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Clase de pruebas unitarias para la entidad Usuario.
 * Implementa pruebas para funcionalidades clave del usuario.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
class UsuarioTest {
    
    private Usuario usuario;
    private Usuario otroUsuario;
    
    @BeforeEach
    void setUp() {
        usuario = new Usuario("testuser", "password123");
        usuario.setNombreCompleto("Usuario de Prueba");
        usuario.setEmail("test@syncup.com");
        
        otroUsuario = new Usuario("otheruser", "password456");
        otroUsuario.setNombreCompleto("Otro Usuario");
    }
    
    @Test
    @DisplayName("Debe crear usuario con datos básicos correctamente")
    void testCreacionUsuarioBasico() {
        assertNotNull(usuario.getId());
        assertEquals("testuser", usuario.getUsername());
        assertEquals("password123", usuario.getPassword());
        assertEquals("Usuario de Prueba", usuario.getNombreCompleto());
        assertEquals("test@syncup.com", usuario.getEmail());
        assertTrue(usuario.isActivo());
        assertFalse(usuario.isEsAdmin());
        assertNotNull(usuario.getFechaRegistro());
    }
    
    @Test
    @DisplayName("Debe manejar canciones favoritas correctamente")
    void testManejoCanciones() {
        // Inicialmente vacío
        assertTrue(usuario.getCancionesFavoritas().isEmpty());
        assertEquals(0, usuario.getNumeroCancionesFavoritas());
        
        // Agregar canciones favoritas
        String cancion1 = "cancion_123";
        String cancion2 = "cancion_456";
        
        assertTrue(usuario.agregarCancionFavorita(cancion1));
        assertTrue(usuario.agregarCancionFavorita(cancion2));
        assertEquals(2, usuario.getNumeroCancionesFavoritas());
        assertTrue(usuario.esCancionFavorita(cancion1));
        assertTrue(usuario.esCancionFavorita(cancion2));
        
        // No debe agregar duplicados
        assertFalse(usuario.agregarCancionFavorita(cancion1));
        assertEquals(2, usuario.getNumeroCancionesFavoritas());
        
        // Remover canción favorita
        assertTrue(usuario.removerCancionFavorita(cancion1));
        assertEquals(1, usuario.getNumeroCancionesFavoritas());
        assertFalse(usuario.esCancionFavorita(cancion1));
        
        // No debe remover canción inexistente
        assertFalse(usuario.removerCancionFavorita("cancion_inexistente"));
        assertEquals(1, usuario.getNumeroCancionesFavoritas());
    }
    
    @Test
    @DisplayName("Debe manejar sistema de seguimiento correctamente")
    void testSistemaSeguimiento() {
        // Inicialmente sin seguidos ni seguidores
        assertEquals(0, usuario.getNumeroSeguidos());
        assertEquals(0, usuario.getNumeroSeguidores());
        
        // Seguir a otro usuario
        assertTrue(usuario.seguirUsuario(otroUsuario.getId()));
        assertEquals(1, usuario.getNumeroSeguidos());
        
        // Agregar seguidor
        assertTrue(otroUsuario.agregarSeguidor(usuario.getId()));
        assertEquals(1, otroUsuario.getNumeroSeguidores());
        
        // No debe seguirse a sí mismo
        assertFalse(usuario.seguirUsuario(usuario.getId()));
        assertEquals(1, usuario.getNumeroSeguidos());
        
        // No debe agregar duplicados en seguidos
        assertFalse(usuario.seguirUsuario(otroUsuario.getId()));
        assertEquals(1, usuario.getNumeroSeguidos());
        
        // Dejar de seguir
        assertTrue(usuario.dejarDeSeguir(otroUsuario.getId()));
        assertEquals(0, usuario.getNumeroSeguidos());
        
        // No debe dejar de seguir si no sigue
        assertFalse(usuario.dejarDeSeguir(otroUsuario.getId()));
    }
    
    @Test
    @DisplayName("Debe validar igualdad de usuarios correctamente")
    void testIgualdadUsuarios() {
        Usuario usuario1 = new Usuario("user1", "pass1");
        Usuario usuario2 = new Usuario("user2", "pass2");
        Usuario usuario1Copia = new Usuario("user1", "pass1");
        
        // Usuarios diferentes deben ser diferentes
        assertNotEquals(usuario1, usuario2);
        assertNotEquals(usuario1.hashCode(), usuario2.hashCode());
        
        // Usuarios con mismo ID deben ser iguales
        usuario1Copia.setId(usuario1.getId());
        assertEquals(usuario1, usuario1Copia);
        assertEquals(usuario1.hashCode(), usuario1Copia.hashCode());
        
        // Usuario no debe ser igual a null u otros objetos
        assertNotEquals(usuario1, null);
        assertNotEquals(usuario1, "string");
    }
    
    @Test
    @DisplayName("Debe manejar géneros favoritos correctamente")
    void testGenerosFavoritos() {
        assertTrue(usuario.getGenerosFavoritos().isEmpty());
        
        // Agregar géneros favoritos
        usuario.getGenerosFavoritos().add("Rock");
        usuario.getGenerosFavoritos().add("Pop");
        usuario.getGenerosFavoritos().add("Electronic");
        
        assertEquals(3, usuario.getGenerosFavoritos().size());
        assertTrue(usuario.getGenerosFavoritos().contains("Rock"));
        assertTrue(usuario.getGenerosFavoritos().contains("Pop"));
        assertTrue(usuario.getGenerosFavoritos().contains("Electronic"));
    }
    
    @Test
    @DisplayName("Debe manejar playlists correctamente")
    void testPlaylists() {
        assertTrue(usuario.getPlaylists().isEmpty());
        
        // Agregar playlists
        usuario.getPlaylists().add("playlist_123");
        usuario.getPlaylists().add("playlist_456");
        
        assertEquals(2, usuario.getPlaylists().size());
        assertTrue(usuario.getPlaylists().contains("playlist_123"));
        assertTrue(usuario.getPlaylists().contains("playlist_456"));
    }
    
    @Test
    @DisplayName("Debe manejar configuración de administrador correctamente")
    void testConfiguracionAdmin() {
        // Por defecto no es admin
        assertFalse(usuario.isEsAdmin());
        
        // Cambiar a admin
        usuario.setEsAdmin(true);
        assertTrue(usuario.isEsAdmin());
        
        // Volver a usuario normal
        usuario.setEsAdmin(false);
        assertFalse(usuario.isEsAdmin());
    }
    
    @Test
    @DisplayName("Debe generar toString informativo")
    void testToString() {
        String usuarioString = usuario.toString();
        
        assertTrue(usuarioString.contains("Usuario{"));
        assertTrue(usuarioString.contains(usuario.getId()));
        assertTrue(usuarioString.contains(usuario.getUsername()));
        assertTrue(usuarioString.contains(usuario.getNombreCompleto()));
        assertTrue(usuarioString.contains(usuario.getEmail()));
    }
    
    @Test
    @DisplayName("Debe manejar inmutabilidad de listas correctamente")
    void testInmutabilidadListas() {
        // Agregar algunas canciones favoritas
        usuario.agregarCancionFavorita("cancion1");
        usuario.agregarCancionFavorita("cancion2");
        
        // Obtener lista de favoritas
        var favoritas = usuario.getCancionesFavoritas();
        int sizeOriginal = favoritas.size();
        
        // Modificar la lista obtenida no debe afectar el usuario
        favoritas.add("cancion3");
        
        // Verificar que el usuario no fue modificado
        assertEquals(sizeOriginal, usuario.getCancionesFavoritas().size());
        assertFalse(usuario.esCancionFavorita("cancion3"));
    }
    
    @Test
    @DisplayName("Debe manejar fecha de registro correctamente")
    void testFechaRegistro() {
        LocalDateTime ahora = LocalDateTime.now();
        
        // La fecha de registro debe estar cerca del tiempo actual
        assertTrue(usuario.getFechaRegistro().isBefore(ahora.plusMinutes(1)));
        assertTrue(usuario.getFechaRegistro().isAfter(ahora.minusMinutes(1)));
        
        // Debe permitir establecer fecha personalizada
        LocalDateTime fechaPersonalizada = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        usuario.setFechaRegistro(fechaPersonalizada);
        assertEquals(fechaPersonalizada, usuario.getFechaRegistro());
    }
}