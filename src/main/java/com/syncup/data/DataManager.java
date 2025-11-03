package com.syncup.data;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.structures.HashMap;
import com.syncup.structures.TrieAutocompletado;
import com.syncup.structures.GrafoSocial;

import java.util.*;
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

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String DEMO_USERNAME = "demo_user";
    private static final String DEMO_PASSWORD = "demo123";

    public static synchronized DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    public void initialize() {
        createDefaultUsers();
        loadSampleSongs();
        rebuildTries();
    }

    private void rebuildTries() {
        trieTitulos.clear(); trieArtistas.clear(); trieGeneros.clear();
        for (Cancion c : cancionesById.values()) {
            trieTitulos.insert(c.getTitulo());
            trieArtistas.insert(c.getArtista());
            trieGeneros.insert(c.getGenero());
        }
    }

    private void createDefaultUsers() {
        if (usuariosByUsername.get(ADMIN_USERNAME) == null) {
            Usuario adminUser = new Usuario(ADMIN_USERNAME, ADMIN_PASSWORD);
            adminUser.setNombreCompleto("Administrador del Sistema");
            adminUser.setEmail("admin@syncup.com");
            adminUser.setEsAdmin(true);
            addUsuario(adminUser);
        }
        if (usuariosByUsername.get(DEMO_USERNAME) == null) {
            Usuario u = new Usuario(DEMO_USERNAME, DEMO_PASSWORD);
            u.setNombreCompleto("Usuario Demo");
            u.setEmail("demo@syncup.com");
            addUsuario(u);
        }
    }

    private void loadSampleSongs() {
        if (!cancionesById.isEmpty()) return;
        Object[][] sample = {
            {"Bohemian Rhapsody","Queen","A Night at the Opera","Rock",1975,355, "https://i.scdn.co/image/ab67616d0000b2739f54b2b6e9d9f0d1cd0cbd2e"},
            {"Imagine","John Lennon","Imagine","Rock",1971,183, "https://i.scdn.co/image/ab67616d0000b273b349b1b5bff8d1e1c7aa2d2c"},
            {"Billie Jean","Michael Jackson","Thriller","Pop",1983,294, "https://i.scdn.co/image/ab67616d0000b2732bf88e9b1e5d1d4c9f0d0d0a"}
        };
        for (Object[] s : sample) {
            Cancion c = new Cancion((String)s[0], (String)s[1], (String)s[3], (Integer)s[4]);
            c.setAlbum((String)s[2]);
            c.setDuracionSegundos((Integer)s[5]);
            c.setCoverUrl((String)s[6]);
            addCancion(c);
        }
    }

    // NUEVO: creación de usuario por registro (no admin)
    public synchronized boolean createUser(String username, String password, String nombre, String email) {
        if (username == null || username.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty()) return false;
        if (usuariosByUsername.containsKey(username)) return false;
        Usuario u = new Usuario(username.trim(), password.trim());
        u.setNombreCompleto(nombre == null ? "" : nombre.trim());
        u.setEmail(email == null ? "" : email.trim());
        u.setEsAdmin(false);
        return addUsuario(u);
    }

    // Usuarios
    public boolean addUsuario(Usuario u) {
        if (u == null || usuariosByUsername.containsKey(u.getUsername())) return false;
        usuariosById.put(u.getId(), u);
        usuariosByUsername.put(u.getUsername(), u);
        grafoSocial.agregarUsuario(u);
        return true;
    }

    public boolean removeUsuario(String id) {
        Usuario u = usuariosById.get(id);
        if (u == null) return false;
        if (ADMIN_USERNAME.equals(u.getUsername())) return false; // proteger admin
        usuariosById.remove(id);
        usuariosByUsername.remove(u.getUsername());
        return true;
    }

    public Usuario getUsuarioById(String id) { return usuariosById.get(id); }
    public Usuario getUsuarioByUsername(String username) { return usuariosByUsername.get(username); }
    public List<Usuario> getAllUsuarios() { return new ArrayList<>(usuariosById.values()); }

    public Usuario authenticateUser(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return usuariosByUsername.get(ADMIN_USERNAME);
        }
        Usuario u = usuariosByUsername.get(username);
        return (u != null && u.getPassword().equals(password) && u.isActivo()) ? u : null;
    }

    // Canciones
    public boolean addCancion(Cancion c) {
        if (c == null || cancionesById.containsKey(c.getId())) return false;
        cancionesById.put(c.getId(), c);
        trieTitulos.insert(c.getTitulo());
        trieArtistas.insert(c.getArtista());
        trieGeneros.insert(c.getGenero());
        return true;
    }

    public boolean removeCancion(String id) { return cancionesById.remove(id) != null; }
    public Cancion getCancionById(String id) { return cancionesById.get(id); }
    public List<Cancion> getAllCanciones() { return new ArrayList<>(cancionesById.values()); }

    // Autocompletado
    public List<String> getSugerenciasTitulos(String p) { return trieTitulos.getSuggestions(p); }
    public List<String> getSugerenciasArtistas(String p) { return trieArtistas.getSuggestions(p); }
    public List<String> getSugerenciasGeneros(String p) { return trieGeneros.getSuggestions(p); }

    public void saveAllData() { /* No-op en memoria */ }

    public String getSystemStats() {
        return "Usuarios:"+usuariosById.size()+"\n"+
               "Canciones:"+cancionesById.size();
    }

    // =================== NUEVO: Seguidores / Seguidos / Álbumes / Cover ===================

    // Conteos de seguidores y seguidos
    public int getSeguidoresCount(String userId){
        Usuario u = usuariosById.get(userId); if(u==null) return 0;
        return grafoSocial.getSeguidores(userId).size();
    }
    public int getSeguidosCount(String userId){
        Usuario u = usuariosById.get(userId); if(u==null) return 0;
        return grafoSocial.getSeguidos(userId).size();
    }
    public List<Usuario> getSeguidores(String userId){ return new ArrayList<>(grafoSocial.getSeguidores(userId)); }
    public List<Usuario> getSeguidos(String userId){ return new ArrayList<>(grafoSocial.getSeguidos(userId)); }

    // Álbumes por usuario (demo): por ahora devuelve álbumes de sus canciones favoritas si existen; si no, por artista del catálogo
    public List<String> getAlbumsByUser(String userId){
        Usuario u = usuariosById.get(userId); if(u==null) return Collections.emptyList();
        List<String> albums = new ArrayList<>();
        for(String cid: u.getCancionesFavoritas()){
            Cancion c = cancionesById.get(cid);
            if(c!=null && c.getAlbum()!=null && !c.getAlbum().isEmpty()) albums.add(c.getAlbum());
        }
        if(albums.isEmpty()){
            // fallback: primeros 5 álbumes distintos del catálogo
            albums.addAll(cancionesById.values().stream()
                .map(Cancion::getAlbum)
                .filter(a->a!=null && !a.isEmpty())
                .distinct().limit(5).collect(Collectors.toList()));
        }
        return albums;
    }

    // Cover URL de una canción por id (para carátulas reales)
    public String getCoverUrl(String cancionId){
        Cancion c = cancionesById.get(cancionId); return c!=null ? c.getCoverUrl() : null;
    }
}
