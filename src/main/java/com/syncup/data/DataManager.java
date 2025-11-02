package com.syncup.data;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.models.Admin;
import com.syncup.structures.HashMap;
import com.syncup.structures.TrieAutocompletado;
import com.syncup.structures.GrafoSocial;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Gestor principal de datos del sistema SyncUp.
 * Maneja la persistencia y acceso a usuarios, canciones y índices de búsqueda.
 * Implementa patrón Singleton para acceso global.
 * RF-015, RF-016, RF-017: Indexación eficiente con HashMap
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class DataManager {
    
    /** Instancia singleton */
    private static DataManager instance;
    
    /** HashMap para acceso rápido a usuarios por ID */
    private HashMap<String, Usuario> usuariosById;
    
    /** HashMap para acceso rápido a usuarios por username */
    private HashMap<String, Usuario> usuariosByUsername;
    
    /** HashMap para acceso rápido a canciones por ID */
    private HashMap<String, Cancion> cancionesById;
    
    /** Trie para autocompletado de títulos de canciones */
    private TrieAutocompletado trieTitulos;
    
    /** Trie para autocompletado de artistas */
    private TrieAutocompletado trieArtistas;
    
    /** Trie para autocompletado de géneros */
    private TrieAutocompletado trieGeneros;
    
    /** Grafo social para conexiones entre usuarios */
    private GrafoSocial grafoSocial;
    
    /** Ruta base para archivos de datos */
    private static final String DATA_DIR = "data/";
    
    /** Usuario administrador por defecto */
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    
    /** Usuario demo por defecto */
    private static final String DEMO_USERNAME = "demo_user";
    private static final String DEMO_PASSWORD = "demo123";
    
    /**
     * Constructor privado para implementar Singleton.
     */
    private DataManager() {
        usuariosById = new HashMap<>();
        usuariosByUsername = new HashMap<>();
        cancionesById = new HashMap<>();
        trieTitulos = new TrieAutocompletado();
        trieArtistas = new TrieAutocompletado();
        trieGeneros = new TrieAutocompletado();
        grafoSocial = new GrafoSocial();
    }
    
    /**
     * Obtiene la instancia singleton del DataManager.
     * 
     * @return Instancia única del DataManager
     */
    public synchronized static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * Inicializa el sistema de datos, creando directorios y cargando datos existentes.
     */
    public void initialize() {
        System.out.println("Inicializando DataManager...");
        
        // Crear directorio de datos si no existe
        createDataDirectory();
        
        // Crear usuarios por defecto
        createDefaultUsers();
        
        // Cargar canciones de muestra
        loadSampleSongs();
        
        // Construir índices de búsqueda
        buildSearchIndices();
        
        System.out.println("DataManager inicializado correctamente.");
        System.out.println("Usuarios cargados: " + usuariosById.size());
        System.out.println("Canciones cargadas: " + cancionesById.size());
    }
    
    /**
     * Crea el directorio de datos si no existe.
     */
    private void createDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Directorio de datos creado: " + DATA_DIR);
            }
        }
    }
    
    /**
     * Construye los índices de búsqueda (Tries).
     */
    private void buildSearchIndices() {
        System.out.println("Construyendo índices de búsqueda...");
        
        // Agregar todas las canciones a los Tries
        for (Cancion cancion : cancionesById.values()) {
            trieTitulos.insert(cancion.getTitulo());
            trieArtistas.insert(cancion.getArtista());
            trieGeneros.insert(cancion.getGenero());
        }
        
        System.out.println("Índices de búsqueda construidos.");
    }
    
    /**
     * Crea usuarios por defecto del sistema.
     */
    private void createDefaultUsers() {
        // Crear administrador por defecto
        if (usuariosByUsername.get(ADMIN_USERNAME) == null) {
            Admin admin = new Admin(ADMIN_USERNAME, ADMIN_PASSWORD);
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setEmail("admin@syncup.com");
            admin.setEsAdmin(true);
            admin.setNivelAcceso(5); // Máximo nivel
            admin.setDepartamento("IT");
            
            addUsuario(admin);
            System.out.println("Usuario administrador creado: " + ADMIN_USERNAME);
        }
        
        // Crear usuario demo por defecto
        if (usuariosByUsername.get(DEMO_USERNAME) == null) {
            Usuario demo = new Usuario(DEMO_USERNAME, DEMO_PASSWORD);
            demo.setNombreCompleto("Usuario Demo");
            demo.setEmail("demo@syncup.com");
            
            // Agregar algunos géneros favoritos al demo
            demo.getGenerosFavoritos().add("Pop");
            demo.getGenerosFavoritos().add("Rock");
            demo.getGenerosFavoritos().add("Electronic");
            
            addUsuario(demo);
            System.out.println("Usuario demo creado: " + DEMO_USERNAME);
        }
    }
    
    /**
     * Carga canciones de muestra en el sistema.
     */
    private void loadSampleSongs() {
        if (!cancionesById.isEmpty()) {
            return; // Ya hay canciones cargadas
        }
        
        System.out.println("Cargando canciones de muestra...");
        
        // Lista de canciones de muestra
        Object[][] sampleSongs = {
            {"Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", 1975, 355},
            {"Imagine", "John Lennon", "Imagine", "Rock", 1971, 183},
            {"Billie Jean", "Michael Jackson", "Thriller", "Pop", 1983, 294},
            {"Hotel California", "Eagles", "Hotel California", "Rock", 1976, 391},
            {"Sweet Child O' Mine", "Guns N' Roses", "Appetite for Destruction", "Rock", 1987, 356},
            {"Smells Like Teen Spirit", "Nirvana", "Nevermind", "Grunge", 1991, 301},
            {"Dancing Queen", "ABBA", "Arrival", "Pop", 1976, 230},
            {"Wonderwall", "Oasis", "(What's the Story) Morning Glory?", "Britpop", 1995, 258},
            {"Lose Yourself", "Eminem", "8 Mile Soundtrack", "Hip Hop", 2002, 326},
            {"Shape of You", "Ed Sheeran", "÷ (Divide)", "Pop", 2017, 233},
            {"Blinding Lights", "The Weeknd", "After Hours", "Synthpop", 2019, 200},
            {"One More Time", "Daft Punk", "Discovery", "Electronic", 2000, 320},
            {"Mr. Brightside", "The Killers", "Hot Fuss", "Alternative Rock", 2003, 222},
            {"Uptown Funk", "Bruno Mars", "Uptown Special", "Funk", 2014, 270},
            {"Bad Guy", "Billie Eilish", "When We All Fall Asleep", "Pop", 2019, 194}
        };
        
        for (Object[] songData : sampleSongs) {
            String titulo = (String) songData[0];
            String artista = (String) songData[1];
            String album = (String) songData[2];
            String genero = (String) songData[3];
            int anio = (Integer) songData[4];
            int duracion = (Integer) songData[5];
            
            Cancion cancion = new Cancion(titulo, artista, genero, anio);
            cancion.setAlbum(album);
            cancion.setDuracionSegundos(duracion);
            
            // Simular algunas reproducciones y favoritos aleatorios
            cancion.setReproducciones((long) (Math.random() * 100000));
            cancion.setNumeroFavoritos((long) (Math.random() * 10000));
            cancion.agregarCalificacion((int) (Math.random() * 5) + 1);
            
            addCancion(cancion);
        }
        
        System.out.println("Canciones de muestra cargadas: " + sampleSongs.length);
    }
    
    // Métodos para manejo de usuarios
    
    /**
     * Agrega un usuario al sistema.
     */
    public boolean addUsuario(Usuario usuario) {
        if (usuario == null || usuariosByUsername.containsKey(usuario.getUsername())) {
            return false;
        }
        
        usuariosById.put(usuario.getId(), usuario);
        usuariosByUsername.put(usuario.getUsername(), usuario);
        
        // Agregar al grafo social
        grafoSocial.agregarUsuario(usuario);
        
        return true;
    }
    
    /**
     * Remueve un usuario del sistema.
     */
    public boolean removeUsuario(String usuarioId) {
        Usuario usuario = usuariosById.get(usuarioId);
        if (usuario != null) {
            usuariosById.remove(usuarioId);
            usuariosByUsername.remove(usuario.getUsername());
            return true;
        }
        return false;
    }
    
    /**
     * Obtiene un usuario por su ID.
     */
    public Usuario getUsuarioById(String id) {
        return usuariosById.get(id);
    }
    
    /**
     * Obtiene un usuario por su username.
     */
    public Usuario getUsuarioByUsername(String username) {
        return usuariosByUsername.get(username);
    }
    
    /**
     * Autentica un usuario con username y password.
     */
    public Usuario authenticateUser(String username, String password) {
        Usuario usuario = usuariosByUsername.get(username);
        if (usuario != null && usuario.getPassword().equals(password) && usuario.isActivo()) {
            return usuario;
        }
        return null;
    }
    
    // Métodos para manejo de canciones
    
    /**
     * Agrega una canción al sistema.
     */
    public boolean addCancion(Cancion cancion) {
        if (cancion == null || cancionesById.containsKey(cancion.getId())) {
            return false;
        }
        
        cancionesById.put(cancion.getId(), cancion);
        
        // Agregar a índices de búsqueda
        trieTitulos.insert(cancion.getTitulo());
        trieArtistas.insert(cancion.getArtista());
        trieGeneros.insert(cancion.getGenero());
        
        return true;
    }
    
    /**
     * Remueve una canción del sistema.
     */
    public boolean removeCancion(String cancionId) {
        return cancionesById.remove(cancionId) != null;
    }
    
    /**
     * Obtiene una canción por su ID.
     */
    public Cancion getCancionById(String id) {
        return cancionesById.get(id);
    }
    
    /**
     * Obtiene todas las canciones del sistema.
     */
    public List<Cancion> getAllCanciones() {
        return cancionesById.values();
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     */
    public List<Usuario> getAllUsuarios() {
        return usuariosById.values();
    }
    
    /**
     * Obtiene el grafo social del sistema.
     */
    public GrafoSocial getGrafoSocial() {
        return grafoSocial;
    }
    
    // Métodos para autocompletado
    
    /**
     * RF-003: Obtiene sugerencias de títulos de canciones.
     */
    public List<String> getSugerenciasTitulos(String prefix) {
        return trieTitulos.getSuggestions(prefix);
    }
    
    /**
     * RF-003: Obtiene sugerencias de artistas.
     */
    public List<String> getSugerenciasArtistas(String prefix) {
        return trieArtistas.getSuggestions(prefix);
    }
    
    /**
     * RF-003: Obtiene sugerencias de géneros.
     */
    public List<String> getSugerenciasGeneros(String prefix) {
        return trieGeneros.getSuggestions(prefix);
    }
    
    /**
     * Guarda todos los datos (placeholder - en memoria por ahora).
     */
    public void saveAllData() {
        System.out.println("Datos guardados en memoria - Usuarios: " + usuariosById.size() + 
                          ", Canciones: " + cancionesById.size());
    }
    
    /**
     * RF-013: Obtiene estadísticas del sistema.
     */
    public String getSystemStats() {
        return String.format(
            "=== SyncUp System Statistics ===\n" +
            "Usuarios: %d\n" +
            "Canciones: %d\n" +
            "Títulos indexados: %d\n" +
            "Artistas indexados: %d\n" +
            "Géneros indexados: %d\n" +
            "Conexiones sociales: %d\n" +
            "HashMap usuarios - Tamaño: %d, Factor carga: %.2f\n" +
            "HashMap canciones - Tamaño: %d, Factor carga: %.2f",
            usuariosById.size(),
            cancionesById.size(),
            trieTitulos.size(),
            trieArtistas.size(),
            trieGeneros.size(),
            grafoSocial.getNumeroConexiones(),
            usuariosById.size(), usuariosById.getLoadFactor(),
            cancionesById.size(), cancionesById.getLoadFactor()
        );
    }
}