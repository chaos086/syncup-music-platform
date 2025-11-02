package com.syncup.structures;

import com.syncup.models.Cancion;
import com.syncup.models.Usuario;
import java.util.*;

public class GrafoDeSimilitud {
    private final Map<String, List<Arista>> grafo = new HashMap<>();
    private final Map<String, Usuario> usuarios = new HashMap<>();
    private final Map<String, Cancion> canciones = new HashMap<>();
    private int numeroNodos = 0;
    private int numeroAristas = 0;

    public void agregarUsuario(Usuario u){ if(u==null||usuarios.containsKey(u.getId())) return; usuarios.put(u.getId(),u); grafo.put(u.getId(), new ArrayList<>()); numeroNodos++; }
    public void agregarCancion(Cancion c){ if(c!=null) canciones.putIfAbsent(c.getId(), c); }

    public void calcularSimilitudes(){
        for (List<Arista> l: grafo.values()) l.clear();
        numeroAristas = 0;
        List<String> ids = new ArrayList<>(usuarios.keySet());
        for (int i=0;i<ids.size();i++) for (int j=i+1;j<ids.size();j++){
            String a = ids.get(i), b = ids.get(j);
            double s = calcularSimilitud(usuarios.get(a), usuarios.get(b));
            if (s>0.1) agregarArista(a,b,s);
        }
    }

    private double calcularSimilitud(Usuario a, Usuario b){
        if (a==null||b==null) return 0.0;
        return Math.min(1.0,
            0.5*coefJaccard(a.getCancionesFavoritas(), b.getCancionesFavoritas())+
            0.3*coefJaccard(a.getGenerosFavoritos(), b.getGenerosFavoritos())+
            0.2*similitudArtistas(a.getCancionesFavoritas(), b.getCancionesFavoritas()));
    }

    private double coefJaccard(List<String> x, List<String> y){
        if (x.isEmpty() && y.isEmpty()) return 0.0;
        Set<String> a=new HashSet<>(x), b=new HashSet<>(y), inter=new HashSet<>(a); inter.retainAll(b); a.addAll(b); if(a.isEmpty()) return 0.0; return (double)inter.size()/a.size();
    }

    private double similitudArtistas(List<String> x, List<String> y){
        Set<String> a=new HashSet<>(), b=new HashSet<>();
        for(String id:x){ Cancion c=canciones.get(id); if(c!=null){ a.add(c.getArtista()); a.addAll(c.getArtistasColaboradores()); }}
        for(String id:y){ Cancion c=canciones.get(id); if(c!=null){ b.add(c.getArtista()); b.addAll(c.getArtistasColaboradores()); }}
        return coefJaccard(new ArrayList<>(a), new ArrayList<>(b));
    }

    private void agregarArista(String a,String b,double p){ if(!grafo.containsKey(a)||!grafo.containsKey(b)) return; grafo.get(a).add(new Arista(b,p)); grafo.get(b).add(new Arista(a,p)); numeroAristas+=2; }

    // NUEVO: método usado por RecommendationEngine
    public List<Cancion> obtenerRecomendaciones(String usuarioId, int limite){
        Usuario u = usuarios.get(usuarioId);
        if (u==null) return new ArrayList<>();
        List<UsuarioSimilar> similares = encontrarUsuariosSimilares(usuarioId, 10);
        Set<String> propias = new HashSet<>(u.getCancionesFavoritas());
        Map<String, Double> score = new HashMap<>();
        for (UsuarioSimilar s: similares){
            for (String cid: s.usuario.getCancionesFavoritas()){
                if (!propias.contains(cid)) score.put(cid, score.getOrDefault(cid,0.0)+s.similitud);
            }
        }
        List<Map.Entry<String,Double>> orden = new ArrayList<>(score.entrySet());
        orden.sort((x,y)->Double.compare(y.getValue(), x.getValue()));
        List<Cancion> res = new ArrayList<>();
        for (int i=0;i<Math.min(limite, orden.size()); i++){
            Cancion c = canciones.get(orden.get(i).getKey()); if(c!=null) res.add(c);
        }
        return res;
    }

    public List<UsuarioSimilar> encontrarUsuariosSimilares(String id, int limite){
        if(!grafo.containsKey(id)) return new ArrayList<>();
        Map<String, Double> dist=new HashMap<>(); Set<String> vis=new HashSet<>(); PriorityQueue<Nodo> pq=new PriorityQueue<>();
        for(String n: grafo.keySet()) dist.put(n, n.equals(id)?1.0:0.0); pq.offer(new Nodo(id,1.0));
        while(!pq.isEmpty()){ Nodo cur=pq.poll(); if(!vis.add(cur.id)) continue; for(Arista ar: grafo.get(cur.id)){ double ns=Math.min(cur.s, ar.p); if(ns>dist.get(ar.d)){ dist.put(ar.d, ns); pq.offer(new Nodo(ar.d, ns)); } } }
        List<UsuarioSimilar> out=new ArrayList<>();
        for(Map.Entry<String,Double> e: dist.entrySet()) if(!e.getKey().equals(id)&&e.getValue()>0.1){ Usuario uu=usuarios.get(e.getKey()); if(uu!=null) out.add(new UsuarioSimilar(uu,e.getValue())); }
        out.sort((x,y)->Double.compare(y.similitud,x.similitud));
        return out.subList(0, Math.min(limite, out.size()));
    }

    public String getEstadisticas(){ double dens = numeroNodos>1 ? (double)numeroAristas/(numeroNodos*(numeroNodos-1)) : 0.0; return String.format("=== Grafo de Similitud ===\nUsuarios: %d\nConexiones: %d\nDensidad: %.3f\nCanciones indexadas: %d", numeroNodos, numeroAristas/2, dens, canciones.size()); }

    public static class UsuarioSimilar{ public final Usuario usuario; public final double similitud; public UsuarioSimilar(Usuario u,double s){usuario=u;similitud=s;} }
    private static class Arista{ String d; double p; Arista(String d,double p){this.d=d;this.p=p;} }
    private static class Nodo implements Comparable<Nodo>{ String id; double s; Nodo(String i,double s){id=i;this.s=s;} public int compareTo(Nodo o){ return Double.compare(o.s, this.s);} }
}
