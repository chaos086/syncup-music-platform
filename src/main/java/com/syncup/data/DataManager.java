package com.syncup.data;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.models.Admin;
import com.syncup.structures.HashMap;
import com.syncup.structures.TrieAutocompletado;
import com.syncup.structures.GrafoSocial;

import java.io.File;
import java.util.List;

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
        trieTitulos.clear();
        trieArtistas.clear();
        trieGeneros.clear();
        for (Cancion c : cancionesById.values()) {
            trieTitulos.insert(c.getTitulo());
            trieArtistas.insert(c.getArtista());
            trieGeneros.insert(c.getGenero());
        }
    }

    private void createDefaultUsers() {
        if (usuariosByUsername.get(ADMIN_USERNAME) == null) {
            Admin a = new Admin(ADMIN_USERNAME, ADMIN_PASSWORD);
            a.setNombreCompleto("Administrador del Sistema");
            a.setEmail("admin@syncup.com");
            a.setEsAdmin(true);
            addUsuario(a);
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
            {"Bohemian Rhapsody","Queen","A Night at the Opera","Rock",1975,355},
            {"Imagine","John Lennon","Imagine","Rock",1971,183},
            {"Billie Jean","Michael Jackson","Thriller","Pop",1983,294}
        };
        for (Object[] s : sample) {
            Cancion c = new Cancion((String)s[0], (String)s[1], (String)s[3], (Integer)s[4]);
            c.setAlbum((String)s[2]);
            c.setDuracionSegundos((Integer)s[5]);
            addCancion(c);
        }
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
        usuariosById.remove(id);
        usuariosByUsername.remove(u.getUsername());
        return true;
    }

    public Usuario getUsuarioById(String id) { return usuariosById.get(id); }
    public Usuario getUsuarioByUsername(String username) { return usuariosByUsername.get(username); }
    public List<Usuario> getAllUsuarios() { return usuariosById.values(); }

    public Usuario authenticateUser(String username, String password) {
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
    public List<Cancion> getAllCanciones() { return cancionesById.values(); }

    // Autocompletado
    public List<String> getSugerenciasTitulos(String p) { return trieTitulos.getSuggestions(p); }
    public List<String> getSugerenciasArtistas(String p) { return trieArtistas.getSuggestions(p); }
    public List<String> getSugerenciasGeneros(String p) { return trieGeneros.getSuggestions(p); }

    public void saveAllData() { /* No-op en memoria */ }

    public String getSystemStats() {
        return "Usuarios:"+usuariosById.size()+"\n"+
               "Canciones:"+cancionesById.size();
    }
}
