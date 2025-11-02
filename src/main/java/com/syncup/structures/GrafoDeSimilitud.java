package com.syncup.structures;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import java.util.*;

/**
 * Grafo de similitud entre usuarios usando solo colecciones java.util
 * para evitar incompatibilidades con la HashMap propia del proyecto.
 */
public class GrafoDeSimilitud {
    private final Map<String, List<Arista>> grafo;
    private final Map<String, Usuario> usuarios;
    private final Map<String, Cancion> canciones;
    private int numeroNodos;
    private int numeroAristas;

    public GrafoDeSimilitud() {
        this.grafo = new java.util.HashMap<>();
        this.usuarios = new java.util.HashMap<>();
        this.canciones = new java.util.HashMap<>();
        this.numeroNodos = 0;
        this.numeroAristas = 0;
    }

    public void agregarUsuario(Usuario usuario) {
        if (usuario == null || usuarios.containsKey(usuario.getId())) return;
        usuarios.put(usuario.getId(), usuario);
        grafo.put(usuario.getId(), new ArrayList<>());
        numeroNodos++;
    }

    public void agregarCancion(Cancion cancion) { if (cancion != null) canciones.putIfAbsent(cancion.getId(), cancion); }

    public void calcularSimilitudes() {
        for (List<Arista> aristas : grafo.values()) aristas.clear();
        numeroAristas = 0;
        List<String> ids = new ArrayList<>(usuarios.keySet());
        for (int i=0;i<ids.size();i++) {
            for (int j=i+1;j<ids.size();j++) {
                String u1 = ids.get(i), u2 = ids.get(j);
                double s = calcularSimilitudEntreUsuarios(usuarios.get(u1), usuarios.get(u2));
                if (s > 0.1) agregarArista(u1, u2, s);
            }
        }
    }

    private double calcularSimilitudEntreUsuarios(Usuario u1, Usuario u2) {
        if (u1==null||u2==null) return 0.0;
        double fav = calcularSimilitudJaccard(u1.getCancionesFavoritas(), u2.getCancionesFavoritas());
        double gen = calcularSimilitudJaccard(u1.getGenerosFavoritos(), u2.getGenerosFavoritos());
        double art = calcularSimilitudArtistas(u1.getCancionesFavoritas(), u2.getCancionesFavoritas());
        return Math.min(1.0, fav*0.5 + gen*0.3 + art*0.2);
    }

    private double calcularSimilitudJaccard(List<String> a, List<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 0.0;
        Set<String> s1 = new HashSet<>(a), s2 = new HashSet<>(b);
        Set<String> inter = new HashSet<>(s1); inter.retainAll(s2);
        Set<String> uni = new HashSet<>(s1); uni.addAll(s2);
        if (uni.isEmpty()) return 0.0;
        return (double) inter.size() / uni.size();
    }

    private double calcularSimilitudArtistas(List<String> f1, List<String> f2) {
        Set<String> a1 = new HashSet<>(), a2 = new HashSet<>();
        for (String id: f1) { Cancion c = canciones.get(id); if (c!=null){ a1.add(c.getArtista()); a1.addAll(c.getArtistasColaboradores()); } }
        for (String id: f2) { Cancion c = canciones.get(id); if (c!=null){ a2.add(c.getArtista()); a2.addAll(c.getArtistasColaboradores()); } }
        return calcularSimilitudJaccard(new ArrayList<>(a1), new ArrayList<>(a2));
    }

    private void agregarArista(String a, String b, double peso) {
        if (!grafo.containsKey(a) || !grafo.containsKey(b)) return;
        grafo.get(a).add(new Arista(b,peso));
        grafo.get(b).add(new Arista(a,peso));
        numeroAristas += 2;
    }

    public List<UsuarioSimilar> encontrarUsuariosSimilares(String id, int limite) {
        if (!grafo.containsKey(id)) return new ArrayList<>();
        Map<String, Double> dist = new java.util.HashMap<>();
        Set<String> vis = new HashSet<>();
        PriorityQueue<NodoDijkstra> pq = new PriorityQueue<>();
        for (String n : grafo.keySet()) dist.put(n, n.equals(id)?1.0:0.0);
        pq.offer(new NodoDijkstra(id,1.0));
        while(!pq.isEmpty()){
            NodoDijkstra cur = pq.poll();
            if (!vis.add(cur.usuarioId)) continue;
            for (Arista ar : grafo.get(cur.usuarioId)){
                double ns = Math.min(cur.similitud, ar.peso);
                if (ns > dist.get(ar.destino)) { dist.put(ar.destino, ns); pq.offer(new NodoDijkstra(ar.destino, ns)); }
            }
        }
        List<UsuarioSimilar> res = new ArrayList<>();
        for (Map.Entry<String,Double> e: dist.entrySet()){
            if (!e.getKey().equals(id) && e.getValue()>0.1){
                Usuario u = usuarios.get(e.getKey());
                if (u!=null) res.add(new UsuarioSimilar(u,e.getValue()));
            }
        }
        res.sort((x,y)->Double.compare(y.similitud,x.similitud));
        return res.subList(0, Math.min(limite, res.size()));
    }

    // NUEVO: Estadísticas del grafo usadas por RecommendationEngine
    public String getEstadisticas() {
        double densidad = numeroNodos > 1 ? (double) numeroAristas / (numeroNodos * (numeroNodos - 1)) : 0.0;
        return String.format(
            "=== Grafo de Similitud ===\nUsuarios: %d\nConexiones: %d\nDensidad: %.3f\nCanciones indexadas: %d",
            numeroNodos, numeroAristas/2, densidad, canciones.size()
        );
    }

    public static class UsuarioSimilar { public final Usuario usuario; public final double similitud; public UsuarioSimilar(Usuario u,double s){usuario=u;similitud=s;} }
    private static class Arista { String destino; double peso; Arista(String d,double p){destino=d;peso=p;} }
    private static class NodoDijkstra implements Comparable<NodoDijkstra>{ String usuarioId; double similitud; NodoDijkstra(String u,double s){usuarioId=u;similitud=s;} public int compareTo(NodoDijkstra o){ return Double.compare(o.similitud, this.similitud);} }

    public int getNumeroNodos(){ return numeroNodos; }
    public int getNumeroAristas(){ return numeroAristas/2; }
    public boolean estanConectados(String a,String b){ if(!grafo.containsKey(a)) return false; return grafo.get(a).stream().anyMatch(x->x.destino.equals(b)); }
    public double obtenerPesoArista(String a,String b){ if(!grafo.containsKey(a)) return 0.0; return grafo.get(a).stream().filter(x->x.destino.equals(b)).mapToDouble(x->x.peso).findFirst().orElse(0.0);}    
}
