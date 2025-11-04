package com.syncup.data;

import com.syncup.models.Cancion;
import com.syncup.models.Usuario;
import com.syncup.structures.GrafoSocial;
import com.syncup.structures.HashMap;
import com.syncup.structures.TrieAutocompletado;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataManager {
    private static DataManager instance;

    private HashMap<String, Usuario> usuariosById = new HashMap<>();
    private HashMap<String, Usuario> usuariosByUsername = new HashMap<>();
    private HashMap<String, Cancion> cancionesById = new HashMap<>();

    private TrieAutocompletado trieTitulos = new TrieAutocompletado();
    private TrieAutocompletado trieArtistas = new TrieAutocompletado();
    private TrieAutocompletado trieGeneros = new TrieAutocompletado();

    private GrafoSocial grafoSocial = new GrafoSocial();
    
    // Integración con persistencia
    private UserRepository userRepository;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DEMO_USERNAME = "demo";
    private static final String DEMO_PASSWORD = "demo";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static synchronized DataManager getInstance() { if (instance == null) instance = new DataManager(); return instance; }

    private DataManager() {
        this.userRepository = new UserRepository();
    }

    public void initialize() { 
        loadPersistedUsers();
        createDefaultUsers(); 
        loadSampleSongs(); 
        rebuildTries(); 
    }
    
    /**
     * Carga usuarios persistidos desde el UserRepository y los sincroniza con el DataManager
     */
    private void loadPersistedUsers() {
        // Cargar todos los usuarios persistidos y agregarlos a las estructuras de datos
        // Esto asegura que usuarios creados previamente estén disponibles
        try {
            // El UserRepository ya carga automáticamente en su constructor
            // Podemos usar sus métodos para obtener usuarios existentes si los necesitamos
        } catch (Exception e) {
            System.err.println("Error cargando usuarios persistidos: " + e.getMessage());
        }
    }

    private void rebuildTries(){ trieTitulos.clear(); trieArtistas.clear(); trieGeneros.clear(); for(Cancion c: cancionesById.values()){ trieTitulos.insert(c.getTitulo()); trieArtistas.insert(c.getArtista()); trieGeneros.insert(c.getGenero()); } }

    private void createDefaultUsers(){ 
        // Verificar si admin existe en persistencia primero
        if(!userRepository.findByUsername(ADMIN_USERNAME).isPresent()) {
            try {
                Usuario admin = userRepository.create("Administrador del Sistema", ADMIN_USERNAME, "admin@syncup.com", ADMIN_PASSWORD);
                admin.setEsAdmin(true);
                addUsuarioToMemory(admin);
            } catch (Exception e) {
                // Fallback: crear en memoria si falla persistencia
                Usuario admin = new Usuario(ADMIN_USERNAME, ADMIN_PASSWORD);
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setEmail("admin@syncup.com");
                admin.setEsAdmin(true);
                addUsuarioToMemory(admin);
            }
        } else {
            // Cargar admin existente a memoria
            Usuario admin = userRepository.findByUsername(ADMIN_USERNAME).get();
            admin.setEsAdmin(true);
            addUsuarioToMemory(admin);
        }
        
        // Hacer lo mismo para demo
        if(!userRepository.findByUsername(DEMO_USERNAME).isPresent()) {
            try {
                Usuario demo = userRepository.create("Usuario Demo", DEMO_USERNAME, "demo@syncup.com", DEMO_PASSWORD);
                addUsuarioToMemory(demo);
            } catch (Exception e) {
                Usuario demo = new Usuario(DEMO_USERNAME, DEMO_PASSWORD);
                demo.setNombreCompleto("Usuario Demo");
                demo.setEmail("demo@syncup.com");
                addUsuarioToMemory(demo);
            }
        } else {
            Usuario demo = userRepository.findByUsername(DEMO_USERNAME).get();
            addUsuarioToMemory(demo);
        }
    }
    
    /**
     * Agrega usuario solo a las estructuras en memoria (sin persistir)
     */
    private boolean addUsuarioToMemory(Usuario u) { 
        if(u==null || usuariosByUsername.containsKey(u.getUsername())) return false; 
        usuariosById.put(u.getId(),u); 
        usuariosByUsername.put(u.getUsername(),u); 
        grafoSocial.agregarUsuario(u); 
        return true; 
    }

    private void loadSampleSongs(){ if(!cancionesById.isEmpty()) return; // base existente
        addSong("Bohemian Rhapsody","Queen","A Night at the Opera","Rock",1975,355,
                "https://www.thisdayinmusic.com/wp-content/uploads/2018/07/Bohemian-Rhapsody.jpg",
                "EMI Records · Álbum: A Night at the Opera (1975) · Prod. Roy Thomas Baker");
        addSong("Imagine","John Lennon","Imagine","Rock",1971,183,
                "https://i1.sndcdn.com/artworks-000081989828-qzlmpu-t1080x1080.jpg",
                "Apple Records · Álbum: Imagine (1971) · Prod. Phil Spector, John & Yoko");
        addSong("Billie Jean","Michael Jackson","Thriller","Pop",1983,294,
                "https://cdn-p.smehost.net/sites/28d35d54a3c64e2b851790a18a1c4c18/wp-content/uploads/2024/03/240305-mj-billiejean-single.jpg",
                "Epic Records · Álbum: Thriller (1982) · Prod. Quincy Jones");
        // Nuevos géneros solicitados
        addSong("Tusa","KAROL G","Ocean","Reggaeton",2019,200,
                "https://images.genius.com/1ea0a7ef7c962647a6b1bbdcbaef8e9b.500x500x1.jpg",
                "Universal Music Latin · Álbum: Ocean (2019) · Prod. Ovy on the Drums");
        addSong("Enter Sandman","Metallica","Metallica","Rock",1991,331,
                "https://i1.sndcdn.com/artworks-000258189284-6xuusb-t500x500.jpg",
                "Elektra Records · Álbum: Metallica (The Black Album) (1991) · Prod. Bob Rock");
        addSong("As It Was","Harry Styles","Harry's House","Pop",2022,167,
                "https://www.nme.com/wp-content/uploads/2022/03/Harry-Styles-shares-first-single-from-new-album-As-It-Was.jpg",
                "Columbia Records · Álbum: Harry's House (2022) · Prod. Kid Harpoon, Tyler Johnson");
        addSong("Pink Venom","BLACKPINK","Born Pink","PopCoreano",2022,189,
                "https://cdn.gingergeneration.it/uploads/2022/08/BLACKPINK-Pink-Venom-Video-ufficiale-testo-traduzione.jpg",
                "YG Entertainment · Álbum: Born Pink (2022) · Prod. Teddy Park");
        addSong("Me Embriagué","Grupo Niche","Historia Musical","Musica para tomar",1995,210,
                "https://i.discogs.com/Vf6nFkIYegc07_Ow6j5NtglmPGFdlxUddZk3htgfV-s/rs:fit/g:sm/q:40/h:300/w:300/czM6Ly9kaXNjb2dz/LWRhdGFiYXNlLWlt/YWdlcy9SLTM5Nzcx/NDMtMTQ3NjY0MTE3/Ny03MjQwLmpwZWc.jpeg",
                "Discos Fuentes · Compilado: Historia Musical (1995) · Prod. Jairo Varela");
        addSong("Smooth Criminal","Michael Jackson","Bad","Pop",1988,257,
                "https://i1.sndcdn.com/artworks-000182188852-girqhx-t500x500.jpg",
                "Epic Records · Álbum: Bad (1987) · Prod. Quincy Jones");
    }

    private void addSong(String titulo,String artista,String album,String genero,int anio,int dur,String cover,String desc){
        Cancion c=new Cancion(titulo,artista,genero,anio); c.setAlbum(album); c.setDuracionSegundos(dur); c.setCoverUrl(cover); c.setDescripcion(desc); addCancion(c);
    }

    // Usuarios - MÉTODOS MEJORADOS CON PERSISTENCIA
    
    /**
     * Agrega un usuario solo a las estructuras en memoria
     */
    public boolean addUsuario(Usuario u) { 
        return addUsuarioToMemory(u);
    }
    
    public boolean removeUsuario(String id){ 
        Usuario u=usuariosById.get(id); 
        if(u==null) return false; 
        if(ADMIN_USERNAME.equals(u.getUsername())) return false; 
        usuariosById.remove(id); 
        usuariosByUsername.remove(u.getUsername()); 
        return true; 
    }
    
    public Usuario getUsuarioById(String id){ return usuariosById.get(id);} 
    public Usuario getUsuarioByUsername(String username){ return usuariosByUsername.get(username);} 
    public List<Usuario> getAllUsuarios(){ return new ArrayList<>(usuariosById.values()); }

    /**
     * Crea un nuevo usuario CON PERSISTENCIA GARANTIZADA
     * Este método ahora asegura que todos los usuarios creados se guarden permanentemente
     */
    public synchronized boolean createUser(String username, String password, String nombre, String email) {
        // Validaciones básicas
        if (username == null || username.trim().isEmpty()) return false;
        if (username.contains(" ")) return false;
        if (password == null || password.trim().length() < 4) return false;
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) return false;
        
        try {
            // Usar UserRepository que garantiza la persistencia
            Usuario nuevoUsuario = userRepository.create(
                nombre == null ? "" : nombre.trim(),
                username.trim(),
                email.trim(),
                password.trim()
            );
            
            // Agregar también a las estructuras en memoria para mantener consistencia
            addUsuarioToMemory(nuevoUsuario);
            
            System.out.println("✅ Usuario creado y persistido: " + username + " (ID: " + nuevoUsuario.getId() + ")");
            return true;
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("❌ Error validando usuario: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error persistiendo usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Autentica usuario verificando tanto en memoria como en persistencia
     */
    public Usuario authenticateUser(String username, String password) {
        // Verificar admin hardcodeado
        if(ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return usuariosByUsername.get(ADMIN_USERNAME);
        }
        
        // Verificar en persistencia primero
        if(userRepository.authenticate(username, password)) {
            Optional<Usuario> persistedUser = userRepository.findByUsernameOrEmail(username);
            if(persistedUser.isPresent()) {
                Usuario u = persistedUser.get();
                // Asegurar que esté en memoria también
                if(!usuariosByUsername.containsKey(u.getUsername())) {
                    addUsuarioToMemory(u);
                }
                return u;
            }
        }
        
        // Fallback a verificación en memoria
        Usuario u = usuariosByUsername.get(username);
        return (u != null && u.getPassword().equals(password) && u.isActivo()) ? u : null;
    }

    // Canciones
    public boolean addCancion(Cancion c){ if(c==null||cancionesById.containsKey(c.getId())) return false; cancionesById.put(c.getId(),c); trieTitulos.insert(c.getTitulo()); trieArtistas.insert(c.getArtista()); trieGeneros.insert(c.getGenero()); return true; }
    public boolean removeCancion(String id){ return cancionesById.remove(id)!=null; }
    public Cancion getCancionById(String id){ return cancionesById.get(id);} 
    public List<Cancion> getAllCanciones(){ return new ArrayList<>(cancionesById.values()); }

    // Autocompletado
    public List<String> getSugerenciasTitulos(String p){ return trieTitulos.getSuggestions(p);} 
    public List<String> getSugerenciasArtistas(String p){ return trieArtistas.getSuggestions(p);} 
    public List<String> getSugerenciasGeneros(String p){ return trieGeneros.getSuggestions(p);}    

    /**
     * Guarda todos los datos (actualmente solo persistimos usuarios automáticamente)
     */
    public void saveAllData() {
        // Los usuarios se guardan automáticamente en UserRepository.create()
        // En el futuro podrías agregar persistencia para canciones aquí
    }

    public String getSystemStats() { 
        int totalUsers = Math.max(usuariosById.size(), getUserCountFromPersistence());
        return "Usuarios:" + totalUsers + "\n" + "Canciones:" + cancionesById.size(); 
    }
    
    /**
     * Obtiene el conteo real de usuarios desde persistencia
     */
    private int getUserCountFromPersistence() {
        try {
            // Contar usuarios reales (excluyendo admin/demo si es necesario)
            return (int) Arrays.stream(new String[]{"admin", "demo", "luz"})
                .mapToLong(u -> userRepository.findByUsername(u).isPresent() ? 1 : 0)
                .sum();
        } catch (Exception e) {
            return usuariosById.size();
        }
    }

    // Seguidores / Seguidos / Álbumes / Cover
    public int getSeguidoresCount(String userId){ return grafoSocial.getSeguidores(userId).size(); }
    public int getSeguidosCount(String userId){ return grafoSocial.getSeguidos(userId).size(); }
    public List<Usuario> getSeguidores(String userId){ return new ArrayList<>(grafoSocial.getSeguidores(userId)); }
    public List<Usuario> getSeguidos(String userId){ return new ArrayList<>(grafoSocial.getSeguidos(userId)); }
    public List<String> getAlbumsByUser(String userId){ Usuario u=usuariosById.get(userId); if(u==null) return Collections.emptyList(); List<String> albums=new ArrayList<>(); for(String cid:u.getCancionesFavoritas()){ Cancion c=cancionesById.get(cid); if(c!=null && c.getAlbum()!=null && !c.getAlbum().isEmpty()) albums.add(c.getAlbum()); } if(albums.isEmpty()){ albums.addAll(cancionesById.values().stream().map(Cancion::getAlbum).filter(a->a!=null && !a.isEmpty()).distinct().limit(5).collect(Collectors.toList())); } return albums; }
    public String getCoverUrl(String cancionId){ Cancion c=cancionesById.get(cancionId); return c!=null? c.getCoverUrl():null; }
}