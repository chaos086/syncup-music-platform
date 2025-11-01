package com.syncup.data;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.structures.HashMap;
import com.syncup.structures.TrieAutocompletado;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Gestor principal de datos del sistema SyncUp.
 * Maneja la persistencia y acceso a usuarios, canciones y índices de búsqueda.
 * Implementa patrón Singleton para acceso global.
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
    
    /** Ruta base para archivos de datos */
    private static final String DATA_DIR = "data/";
    
    /** Archivo de usuarios */
    private static final String USUARIOS_FILE = DATA_DIR + "usuarios.txt";
    
    /** Archivo de canciones */
    private static final String CANCIONES_FILE = DATA_DIR + "canciones.txt";
    
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
        
        // Cargar datos existentes
        loadData();
        
        // Crear usuarios por defecto si no existen
        createDefaultUsers();
        
        // Cargar canciones de muestra si no hay canciones
        if (cancionesById.isEmpty()) {
            loadSampleSongs();
        }
        
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
            } else {
                System.err.println("No se pudo crear el directorio de datos: " + DATA_DIR);
            }
        }
    }
    
    /**
     * Carga todos los datos desde archivos.
     */
    private void loadData() {
        loadUsuarios();
        loadCanciones();
        buildSearchIndices();
    }
    
    /**
     * Carga usuarios desde archivo.
     */
    private void loadUsuarios() {
        File file = new File(USUARIOS_FILE);
        if (!file.exists()) {
            System.out.println("Archivo de usuarios no encontrado. Se creará uno nuevo.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Usuario usuario = parseUsuarioFromLine(line);
                if (usuario != null) {
                    addUsuarioToIndices(usuario);
                }
            }
            System.out.println("Usuarios cargados desde archivo: " + usuariosById.size());
        } catch (IOException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Carga canciones desde archivo.
     */
    private void loadCanciones() {
        File file = new File(CANCIONES_FILE);
        if (!file.exists()) {
            System.out.println("Archivo de canciones no encontrado. Se creará uno nuevo.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Cancion cancion = parseCancionFromLine(line);
                if (cancion != null) {
                    addCancionToIndices(cancion);
                }
            }
            System.out.println("Canciones cargadas desde archivo: " + cancionesById.size());
        } catch (IOException e) {
            System.err.println("Error al cargar canciones: " + e.getMessage());
        }
    }
    
    /**
     * Construye los índices de búsqueda (Tries).
     */
    private void buildSearchIndices() {
        System.out.println("Construyendo índices de búsqueda...");
        
        // Limpiar Tries existentes
        trieTitulos.clear();
        trieArtistas.clear();
        trieGeneros.clear();
        
        // Agregar todas las canciones a los Tries
        for (Cancion cancion : cancionesById.values()) {
            trieTitulos.insert(cancion.getTitulo());
            trieArtistas.insert(cancion.getArtista());
            trieGeneros.insert(cancion.getGenero());
            
            // Agregar artistas colaboradores
            for (String artista : cancion.getArtistasColaboradores()) {
                trieArtistas.insert(artista);
            }
        }
        
        System.out.println("Índices de búsqueda construidos.");
    }
    
    /**
     * Crea usuarios por defecto del sistema.
     */
    private void createDefaultUsers() {
        // Crear administrador por defecto
        if (usuariosByUsername.get(ADMIN_USERNAME) == null) {
            Usuario admin = new Usuario(ADMIN_USERNAME, ADMIN_PASSWORD);
            admin.setNombreCompleto("Administrador del Sistema");
            admin.setEmail("admin@syncup.com");
            admin.setEsAdmin(true);
            
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
            {"Somebody That I Used to Know", "Gotye", "Making Mirrors", "Indie Pop", 2011, 244},
            {"Shape of You", "Ed Sheeran", "÷ (Divide)", "Pop", 2017, 233},
            {"Blinding Lights", "The Weeknd", "After Hours", "Synthpop", 2019, 200},
            {"One More Time", "Daft Punk", "Discovery", "Electronic", 2000, 320},
            {"Mr. Brightside", "The Killers", "Hot Fuss", "Alternative Rock", 2003, 222},
            {"Uptown Funk", "Mark Ronson ft. Bruno Mars", "Uptown Special", "Funk", 2014, 270}
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
     * 
     * @param usuario Usuario a agregar
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean addUsuario(Usuario usuario) {
        if (usuario == null || usuariosByUsername.containsKey(usuario.getUsername())) {
            return false;
        }
        
        addUsuarioToIndices(usuario);
        return true;
    }
    
    /**
     * Agrega un usuario a los índices internos.
     * 
     * @param usuario Usuario a agregar
     */
    private void addUsuarioToIndices(Usuario usuario) {
        usuariosById.put(usuario.getId(), usuario);
        usuariosByUsername.put(usuario.getUsername(), usuario);
    }
    
    /**
     * Obtiene un usuario por su ID.
     * 
     * @param id ID del usuario
     * @return Usuario encontrado o null
     */
    public Usuario getUsuarioById(String id) {
        return usuariosById.get(id);
    }
    
    /**
     * Obtiene un usuario por su username.
     * 
     * @param username Username del usuario
     * @return Usuario encontrado o null
     */
    public Usuario getUsuarioByUsername(String username) {
        return usuariosByUsername.get(username);
    }
    
    /**
     * Autentica un usuario con username y password.
     * 
     * @param username Username del usuario
     * @param password Password del usuario
     * @return Usuario autenticado o null si las credenciales son inválidas
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
     * 
     * @param cancion Canción a agregar
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean addCancion(Cancion cancion) {
        if (cancion == null || cancionesById.containsKey(cancion.getId())) {
            return false;
        }
        
        addCancionToIndices(cancion);
        return true;
    }
    
    /**
     * Agrega una canción a los índices internos.
     * 
     * @param cancion Canción a agregar
     */
    private void addCancionToIndices(Cancion cancion) {
        cancionesById.put(cancion.getId(), cancion);
        trieTitulos.insert(cancion.getTitulo());
        trieArtistas.insert(cancion.getArtista());
        trieGeneros.insert(cancion.getGenero());
    }
    
    /**
     * Obtiene una canción por su ID.
     * 
     * @param id ID de la canción
     * @return Canción encontrada o null
     */
    public Cancion getCancionById(String id) {
        return cancionesById.get(id);
    }
    
    /**
     * Obtiene todas las canciones del sistema.
     * 
     * @return Lista con todas las canciones
     */
    public List<Cancion> getAllCanciones() {
        return cancionesById.values();
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return Lista con todos los usuarios
     */
    public List<Usuario> getAllUsuarios() {
        return usuariosById.values();
    }
    
    // Métodos para autocompletado
    
    /**
     * Obtiene sugerencias de títulos de canciones.
     * 
     * @param prefix Prefijo a buscar
     * @return Lista de sugerencias
     */
    public List<String> getSugerenciasTitulos(String prefix) {
        return trieTitulos.getSuggestions(prefix);
    }
    
    /**
     * Obtiene sugerencias de artistas.
     * 
     * @param prefix Prefijo a buscar
     * @return Lista de sugerencias
     */
    public List<String> getSugerenciasArtistas(String prefix) {
        return trieArtistas.getSuggestions(prefix);
    }
    
    /**
     * Obtiene sugerencias de géneros.
     * 
     * @param prefix Prefijo a buscar
     * @return Lista de sugerencias
     */
    public List<String> getSugerenciasGeneros(String prefix) {
        return trieGeneros.getSuggestions(prefix);
    }
    
    /**
     * Guarda todos los datos en archivos.
     */
    public void saveAllData() {
        saveUsuarios();
        saveCanciones();
        System.out.println("Todos los datos guardados exitosamente.");
    }
    
    /**
     * Guarda usuarios en archivo.
     */
    private void saveUsuarios() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USUARIOS_FILE))) {
            for (Usuario usuario : usuariosById.values()) {
                writer.println(usuarioToLine(usuario));
            }
            System.out.println("Usuarios guardados: " + usuariosById.size());
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Guarda canciones en archivo.
     */
    private void saveCanciones() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CANCIONES_FILE))) {
            for (Cancion cancion : cancionesById.values()) {
                writer.println(cancionToLine(cancion));
            }
            System.out.println("Canciones guardadas: " + cancionesById.size());
        } catch (IOException e) {
            System.err.println("Error al guardar canciones: " + e.getMessage());
        }
    }
    
    // Métodos auxiliares para parsing
    
    private Usuario parseUsuarioFromLine(String line) {
        try {
            String[] parts = line.split("\t");
            if (parts.length >= 6) {
                Usuario usuario = new Usuario(parts[1], parts[2]); // username, password
                usuario.setId(parts[0]);
                usuario.setNombreCompleto(parts[3]);
                usuario.setEmail(parts[4]);
                usuario.setEsAdmin(Boolean.parseBoolean(parts[5]));
                return usuario;
            }
        } catch (Exception e) {
            System.err.println("Error parsing usuario: " + e.getMessage());
        }
        return null;
    }
    
    private Cancion parseCancionFromLine(String line) {
        try {
            String[] parts = line.split("\t");
            if (parts.length >= 7) {
                Cancion cancion = new Cancion(parts[1], parts[2], parts[4], Integer.parseInt(parts[5]));
                cancion.setId(parts[0]);
                cancion.setAlbum(parts[3]);
                cancion.setDuracionSegundos(Integer.parseInt(parts[6]));
                return cancion;
            }
        } catch (Exception e) {
            System.err.println("Error parsing cancion: " + e.getMessage());
        }
        return null;
    }
    
    private String usuarioToLine(Usuario usuario) {
        return String.join("\t",
            usuario.getId(),
            usuario.getUsername(),
            usuario.getPassword(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            String.valueOf(usuario.isEsAdmin())
        );
    }
    
    private String cancionToLine(Cancion cancion) {
        return String.join("\t",
            cancion.getId(),
            cancion.getTitulo(),
            cancion.getArtista(),
            cancion.getAlbum(),
            cancion.getGenero(),
            String.valueOf(cancion.getAnio()),
            String.valueOf(cancion.getDuracionSegundos())
        );
    }
    
    /**
     * Obtiene estadísticas del sistema.
     * 
     * @return String con estadísticas
     */
    public String getSystemStats() {
        return String.format(
            "=== SyncUp System Statistics ===\n" +
            "Usuarios: %d\n" +
            "Canciones: %d\n" +
            "Títulos indexados: %d\n" +
            "Artistas indexados: %d\n" +
            "Géneros indexados: %d\n" +
            "HashMap usuarios stats: %s\n" +
            "HashMap canciones stats: %s",
            usuariosById.size(),
            cancionesById.size(),
            trieTitulos.size(),
            trieArtistas.size(),
            trieGeneros.size(),
            usuariosById.getDistributionStats(),
            cancionesById.getDistributionStats()
        );
    }
}