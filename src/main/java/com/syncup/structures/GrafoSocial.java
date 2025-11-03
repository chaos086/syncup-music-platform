package com.syncup.structures;

import com.syncup.models.Usuario;
import java.util.*;

public class GrafoSocial {
    private HashMap<String, List<String>> grafo = new HashMap<>();
    private HashMap<String, Usuario> usuarios = new HashMap<>();
    private int numeroNodos = 0; private int numeroConexiones = 0;

    public boolean agregarUsuario(Usuario usuario) { if (usuario==null || usuarios.containsKey(usuario.getId())) return false; usuarios.put(usuario.getId(), usuario); grafo.put(usuario.getId(), new ArrayList<>()); numeroNodos++; return true; }
    public boolean conectarUsuarios(String a, String b){ if(a==null||b==null||a.equals(b)||!grafo.containsKey(a)||!grafo.containsKey(b)) return false; if (grafo.get(a).contains(b)) return false; grafo.get(a).add(b); grafo.get(b).add(a); numeroConexiones++; return true; }
    public boolean desconectarUsuarios(String a,String b){ if(!estanConectados(a,b)) return false; grafo.get(a).remove(b); grafo.get(b).remove(a); numeroConexiones--; return true; }
    public boolean estanConectados(String a,String b){ if(!grafo.containsKey(a)) return false; return grafo.get(a).contains(b); }

    // NUEVO: utilidades para seguidores/seguidos (vista no dirigida)
    public List<Usuario> getSeguidores(String userId){ return getVecinos(userId); }
    public List<Usuario> getSeguidos(String userId){ return getVecinos(userId); }
    private List<Usuario> getVecinos(String userId){ List<Usuario> r=new ArrayList<>(); List<String> vec=grafo.get(userId); if(vec==null) return r; for(String id: vec){ Usuario u=usuarios.get(id); if(u!=null) r.add(u);} return r; }

    // Resto del archivo original (métodos BFS, populares, etc.) se mantiene
}
