package com.syncup.data;

import com.syncup.models.Usuario;
import com.syncup.persistence.JsonDataStore;
import com.syncup.utils.PasswordHasher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class UserRepository {
    private final JsonDataStore store;
    private final Map<String, Usuario> byId = new LinkedHashMap<>();
    private final Map<String, String> usernameToId = new HashMap<>();
    private final Map<String, String> emailToId = new HashMap<>();
    private long counter = 1;

    public UserRepository(){
        this.store = new JsonDataStore(Path.of("src/main/resources"));
        try{ load(); }catch(IOException e){ System.err.println("Error cargando usuarios: "+e); }
    }

    private synchronized void load() throws IOException {
        List<Map<String,Object>> raw = store.loadUsers();
        byId.clear(); usernameToId.clear(); emailToId.clear(); counter=1;
        for(Map<String,Object> m: raw){
            String id = String.valueOf(m.getOrDefault("id",""));
            String username = String.valueOf(m.getOrDefault("username",""));
            String email = String.valueOf(m.getOrDefault("email",""));
            String name = String.valueOf(m.getOrDefault("name",""));
            String hash = String.valueOf(m.getOrDefault("passwordHash",""));
            // NUEVO: cargar flag de admin desde persistencia
            Boolean isAdmin = (Boolean) m.getOrDefault("isAdmin", false);
            
            if(id.isBlank()||username.isBlank()) continue;
            Usuario u = new Usuario(id, username, hash, name, email);
            u.setPasswordHash(hash);
            u.setEsAdmin(isAdmin != null && isAdmin); // establecer rol desde persistencia
            
            byId.put(id, u);
            usernameToId.put(username.toLowerCase(Locale.ROOT), id);
            if(email!=null && !email.isBlank()) emailToId.put(email.toLowerCase(Locale.ROOT), id);
            try{ long n = Long.parseLong(id.replace("u_","")); if(n>=counter) counter=n+1; }catch(Exception ignored){}
        }
    }

    private synchronized void save(){
        try{
            List<Map<String,Object>> out = new ArrayList<>();
            for(Usuario u: byId.values()){
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                m.put("email", u.getEmail());
                m.put("name", u.getNombreCompleto());
                m.put("passwordHash", u.getPasswordHash()!=null? u.getPasswordHash(): "");
                // NUEVO: persistir rol de admin
                m.put("isAdmin", u.isEsAdmin());
                out.add(m);
            }
            store.saveUsers(out);
        }catch(Exception e){ System.err.println("Error guardando usuarios: "+e); }
    }

    public synchronized Optional<Usuario> findByUsernameOrEmail(String userOrEmail){
        if(userOrEmail==null) return Optional.empty();
        String key = userOrEmail.toLowerCase(Locale.ROOT);
        String id = usernameToId.get(key);
        if(id==null) id = emailToId.get(key);
        return id==null? Optional.empty(): Optional.ofNullable(byId.get(id));
    }

    public synchronized Optional<Usuario> findByUsername(String username){
        if(username==null) return Optional.empty();
        String id = usernameToId.get(username.toLowerCase(Locale.ROOT));
        return id==null? Optional.empty(): Optional.ofNullable(byId.get(id));
    }

    public synchronized Optional<Usuario> findByEmail(String email){
        if(email==null) return Optional.empty();
        String id = emailToId.get(email.toLowerCase(Locale.ROOT));
        return id==null? Optional.empty(): Optional.ofNullable(byId.get(id));
    }

    /**
     * Crea un nuevo usuario con rol especificado
     */
    public synchronized Usuario create(String name, String username, String email, String rawPassword){
        return create(name, username, email, rawPassword, false);
    }
    
    /**
     * Crea un nuevo usuario con rol especificado (admin o usuario normal)
     */
    public synchronized Usuario create(String name, String username, String email, String rawPassword, boolean isAdmin){
        if(username==null || username.isBlank() || username.contains(" ") || username.length()<3)
            throw new IllegalArgumentException("Username inválido");
        if(email!=null && !email.isBlank() && !email.contains("@"))
            throw new IllegalArgumentException("Email inválido");
        if(rawPassword==null || rawPassword.length()<6)
            throw new IllegalArgumentException("Contraseña muy corta");

        String ukey = username.toLowerCase(Locale.ROOT);
        if(usernameToId.containsKey(ukey))
            throw new IllegalStateException("Ya existe un usuario con ese username");
        if(email!=null && !email.isBlank() && emailToId.containsKey(email.toLowerCase(Locale.ROOT)))
            throw new IllegalStateException("Ya existe un usuario con ese email");

        String id = String.format("u_%04d", counter++);
        String hash = PasswordHasher.sha256(rawPassword);
        Usuario u = new Usuario(id, username, hash, name==null?"":name, email==null?"":email);
        u.setPasswordHash(hash);
        u.setEsAdmin(isAdmin); // establecer rol
        
        byId.put(id, u);
        usernameToId.put(ukey, id);
        if(email!=null && !email.isBlank()) emailToId.put(email.toLowerCase(Locale.ROOT), id);
        save(); // persistir con rol incluido
        return u;
    }
    
    /**
     * Actualiza el rol de admin de un usuario existente
     */
    public synchronized boolean updateAdminRole(String username, boolean isAdmin) {
        Optional<Usuario> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) return false;
        
        Usuario user = userOpt.get();
        user.setEsAdmin(isAdmin);
        save(); // persistir cambio de rol
        return true;
    }

    public synchronized boolean authenticate(String userOrEmail, String rawPassword){
        Optional<Usuario> ou = findByUsernameOrEmail(userOrEmail);
        if(ou.isEmpty()) return false;
        String calc = PasswordHasher.sha256(rawPassword);
        String stored = ou.get().getPasswordHash();
        if(stored==null || stored.isBlank()) return false; // forzar login con esquema nuevo
        return calc.equals(stored);
    }
    
    /**
     * Método especial para credenciales de administrador hardcodeadas
     */
    public synchronized boolean isAdminCredentials(String username, String password) {
        return "admin".equals(username) && "admin123".equals(password);
    }
}