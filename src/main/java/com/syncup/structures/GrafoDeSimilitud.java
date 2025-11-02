package com.syncup.structures;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import java.util.*;

/**
 * Implementación de Grafo Ponderado para calcular similitudes entre usuarios
 * basado en sus gustos musicales. Utiliza el algoritmo de Dijkstra para 
 * encontrar usuarios similares y generar recomendaciones.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class GrafoDeSimilitud {
    
    /** Mapa de adyacencias que representa el grafo */
    private HashMap<String, List<Arista>> grafo;
    
    /** Mapa para acceso rápido a usuarios por ID */
    private HashMap<String, Usuario> usuarios;
    
    /** Mapa para acceso rápido a canciones por ID */
    private HashMap<String, Cancion> canciones;
    
    /** Número total de nodos (usuarios) en el grafo */
    private int numeroNodos;
    
    /** Número total de aristas en el grafo */
    private int numeroAristas;
    
    /**
     * Constructor que inicializa el grafo vacío.
     */
    public GrafoDeSimilitud() {
        this.grafo = new HashMap<>();
        this.usuarios = new HashMap<>();
        this.canciones = new HashMap<>();
        this.numeroNodos = 0;
        this.numeroAristas = 0;
    }
    
    /**
     * Agrega un usuario al grafo.
     * 
     * @param usuario Usuario a agregar
     */
    public void agregarUsuario(Usuario usuario) {
        if (usuario == null || usuarios.containsKey(usuario.getId())) {
            return;
        }
        
        usuarios.put(usuario.getId(), usuario);
        grafo.put(usuario.getId(), new ArrayList<>());
        numeroNodos++;
        
        System.out.println("Usuario agregado al grafo: " + usuario.getUsername());
    }
    
    /**
     * Agrega una canción al sistema para cálculos de similitud.
     * 
     * @param cancion Canción a agregar
     */
    public void agregarCancion(Cancion cancion) {
        if (cancion != null && !canciones.containsKey(cancion.getId())) {
            canciones.put(cancion.getId(), cancion);
        }
    }
    
    /**
     * Calcula y actualiza todas las similitudes entre usuarios.
     * Este método debe llamarse después de agregar usuarios y canciones.
     */
    public void calcularSimilitudes() {
        System.out.println("Calculando similitudes entre " + numeroNodos + " usuarios...");
        
        // Limpiar aristas existentes
        for (List<Arista> aristas : grafo.values()) {
            aristas.clear();
        }
        numeroAristas = 0;
        
        // Calcular similitudes entre todos los pares de usuarios
        List<String> userIds = new ArrayList<>(usuarios.keySet());
        
        for (int i = 0; i < userIds.size(); i++) {
            for (int j = i + 1; j < userIds.size(); j++) {
                String userId1 = userIds.get(i);
                String userId2 = userIds.get(j);
                
                Usuario usuario1 = usuarios.get(userId1);
                Usuario usuario2 = usuarios.get(userId2);
                
                double similitud = calcularSimilitudEntreUsuarios(usuario1, usuario2);
                
                // Solo agregar aristas si la similitud es significativa (> 0.1)
                if (similitud > 0.1) {
                    agregarArista(userId1, userId2, similitud);
                }
            }
        }
        
        System.out.println("Similitudes calculadas. Aristas creadas: " + numeroAristas);
    }
    
    /**
     * Calcula la similitud entre dos usuarios basada en sus gustos musicales.
     * 
     * @param usuario1 Primer usuario
     * @param usuario2 Segundo usuario
     * @return Valor de similitud entre 0.0 y 1.0
     */
    private double calcularSimilitudEntreUsuarios(Usuario usuario1, Usuario usuario2) {
        if (usuario1 == null || usuario2 == null) {
            return 0.0;
        }
        
        // Obtener canciones favoritas de ambos usuarios
        List<String> favoritas1 = usuario1.getCancionesFavoritas();
        List<String> favoritas2 = usuario2.getCancionesFavoritas();
        
        if (favoritas1.isEmpty() && favoritas2.isEmpty()) {
            return 0.0;
        }
        
        // Calcular similitud usando coeficiente de Jaccard
        double similitudFavoritas = calcularSimilitudJaccard(favoritas1, favoritas2);
        
        // Calcular similitud de géneros favoritos
        List<String> generos1 = usuario1.getGenerosFavoritos();
        List<String> generos2 = usuario2.getGenerosFavoritos();
        double similitudGeneros = calcularSimilitudJaccard(generos1, generos2);
        
        // Calcular similitud de artistas (basada en canciones favoritas)
        double similitudArtistas = calcularSimilitudArtistas(favoritas1, favoritas2);
        
        // Combinar diferentes tipos de similitudes con pesos
        double similitudTotal = (similitudFavoritas * 0.5) + 
                               (similitudGeneros * 0.3) + 
                               (similitudArtistas * 0.2);
        
        return Math.min(1.0, similitudTotal);
    }
    
    /**
     * Calcula el coeficiente de Jaccard entre dos listas.
     * 
     * @param lista1 Primera lista
     * @param lista2 Segunda lista
     * @return Coeficiente de Jaccard (0.0 a 1.0)
     */
    private double calcularSimilitudJaccard(List<String> lista1, List<String> lista2) {
        if (lista1.isEmpty() && lista2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> set1 = new HashSet<>(lista1);
        Set<String> set2 = new HashSet<>(lista2);
        
        // Intersección
        Set<String> interseccion = new HashSet<>(set1);
        interseccion.retainAll(set2);
        
        // Unión
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) interseccion.size() / union.size();
    }
    
    /**
     * Calcula similitud basada en artistas comunes.
     * 
     * @param favoritas1 Canciones favoritas del usuario 1
     * @param favoritas2 Canciones favoritas del usuario 2
     * @return Similitud de artistas (0.0 a 1.0)
     */
    private double calcularSimilitudArtistas(List<String> favoritas1, List<String> favoritas2) {
        Set<String> artistas1 = new HashSet<>();
        Set<String> artistas2 = new HashSet<>();
        
        // Extraer artistas de las canciones favoritas
        for (String cancionId : favoritas1) {
            Cancion cancion = canciones.get(cancionId);
            if (cancion != null) {
                artistas1.add(cancion.getArtista());
                artistas1.addAll(cancion.getArtistasColaboradores());
            }
        }
        
        for (String cancionId : favoritas2) {
            Cancion cancion = canciones.get(cancionId);
            if (cancion != null) {
                artistas2.add(cancion.getArtista());
                artistas2.addAll(cancion.getArtistasColaboradores());
            }
        }
        
        return calcularSimilitudJaccard(new ArrayList<>(artistas1), new ArrayList<>(artistas2));
    }
    
    /**
     * Agrega una arista bidireccional entre dos usuarios.
     * 
     * @param userId1 ID del primer usuario
     * @param userId2 ID del segundo usuario
     * @param peso Peso de la arista (similitud)
     */
    private void agregarArista(String userId1, String userId2, double peso) {
        if (!grafo.containsKey(userId1) || !grafo.containsKey(userId2)) {
            return;
        }
        
        // Agregar arista en ambas direcciones (grafo no dirigido)
        grafo.get(userId1).add(new Arista(userId2, peso));
        grafo.get(userId2).add(new Arista(userId1, peso));
        numeroAristas += 2; // Contar ambas direcciones
    }
    
    /**
     * Encuentra usuarios similares usando el algoritmo de Dijkstra.
     * 
     * @param usuarioId ID del usuario origen
     * @param limite Número máximo de usuarios similares a retornar
     * @return Lista de usuarios similares ordenada por similitud descendente
     */
    public List<UsuarioSimilar> encontrarUsuariosSimilares(String usuarioId, int limite) {
        if (!grafo.containsKey(usuarioId)) {
            return new ArrayList<>();
        }
        
        // Implementar Dijkstra modificado para encontrar usuarios con mayor similitud
        Map<String, Double> distancias = new HashMap<>();
        Set<String> visitados = new HashSet<>();
        PriorityQueue<NodoDijkstra> cola = new PriorityQueue<>();
        
        // Inicializar distancias
        for (String nodo : grafo.keySet()) {
            distancias.put(nodo, nodo.equals(usuarioId) ? 1.0 : 0.0);
        }
        
        cola.offer(new NodoDijkstra(usuarioId, 1.0));
        
        while (!cola.isEmpty()) {
            NodoDijkstra nodoActual = cola.poll();
            String nodoId = nodoActual.usuarioId;
            
            if (visitados.contains(nodoId)) {
                continue;
            }
            
            visitados.add(nodoId);
            
            // Explorar vecinos
            for (Arista arista : grafo.get(nodoId)) {
                String vecinoId = arista.destino;
                double nuevaSimilitud = Math.min(nodoActual.similitud, arista.peso);
                
                if (nuevaSimilitud > distancias.get(vecinoId)) {
                    distancias.put(vecinoId, nuevaSimilitud);
                    cola.offer(new NodoDijkstra(vecinoId, nuevaSimilitud));
                }
            }
        }
        
        // Convertir a lista de usuarios similares (excluyendo el usuario origen)
        List<UsuarioSimilar> usuariosSimilares = new ArrayList<>();
        
        for (Map.Entry<String, Double> entrada : distancias.entrySet()) {
            String userId = entrada.getKey();
            double similitud = entrada.getValue();
            
            if (!userId.equals(usuarioId) && similitud > 0.1) {
                Usuario usuario = usuarios.get(userId);
                if (usuario != null) {
                    usuariosSimilares.add(new UsuarioSimilar(usuario, similitud));
                }
            }
        }
        
        // Ordenar por similitud descendente y limitar resultados
        usuariosSimilares.sort((a, b) -> Double.compare(b.similitud, a.similitud));
        
        return usuariosSimilares.subList(0, Math.min(limite, usuariosSimilares.size()));
    }
    
    /**
     * Obtiene recomendaciones de canciones basadas en usuarios similares.
     * 
     * @param usuarioId ID del usuario
     * @param limite Número máximo de recomendaciones
     * @return Lista de canciones recomendadas
     */
    public List<Cancion> obtenerRecomendaciones(String usuarioId, int limite) {
        Usuario usuario = usuarios.get(usuarioId);
        if (usuario == null) {
            return new ArrayList<>();
        }
        
        // Obtener usuarios similares
        List<UsuarioSimilar> usuariosSimilares = encontrarUsuariosSimilares(usuarioId, 10);
        
        // Recopilar canciones favoritas de usuarios similares
        Map<String, Double> candidatas = new HashMap<>();
        Set<String> cancionesPropias = new HashSet<>(usuario.getCancionesFavoritas());
        
        for (UsuarioSimilar usuarioSimilar : usuariosSimilares) {
            List<String> favoritasDelSimilar = usuarioSimilar.usuario.getCancionesFavoritas();
            
            for (String cancionId : favoritasDelSimilar) {
                // No recomendar canciones que ya tiene en favoritas
                if (!cancionesPropias.contains(cancionId)) {
                    double puntaje = candidatas.getOrDefault(cancionId, 0.0) + usuarioSimilar.similitud;
                    candidatas.put(cancionId, puntaje);
                }
            }
        }
        
        // Convertir a lista de canciones y ordenar por puntaje
        List<CancionRecomendada> recomendaciones = new ArrayList<>();
        
        for (Map.Entry<String, Double> entrada : candidatas.entrySet()) {
            String cancionId = entrada.getKey();
            double puntaje = entrada.getValue();
            Cancion cancion = canciones.get(cancionId);
            
            if (cancion != null) {
                recomendaciones.add(new CancionRecomendada(cancion, puntaje));
            }
        }
        
        // Ordenar por puntaje descendente y limitar resultados
        recomendaciones.sort((a, b) -> Double.compare(b.puntaje, a.puntaje));
        
        List<Cancion> resultado = new ArrayList<>();
        for (int i = 0; i < Math.min(limite, recomendaciones.size()); i++) {
            resultado.add(recomendaciones.get(i).cancion);
        }
        
        return resultado;
    }
    
    /**
     * Obtiene estadísticas del grafo.
     * 
     * @return String con estadísticas del grafo
     */
    public String getEstadisticas() {
        double densidad = numeroNodos > 1 ? 
            (double) numeroAristas / (numeroNodos * (numeroNodos - 1)) : 0.0;
        
        return String.format(
            "=== Estadísticas del Grafo de Similitud ===\n" +
            "Número de usuarios: %d\n" +
            "Número de conexiones: %d\n" +
            "Densidad del grafo: %.3f\n" +
            "Canciones en el sistema: %d",
            numeroNodos, numeroAristas / 2, densidad, canciones.size()
        );
    }
    
    // Clases auxiliares
    
    /**
     * Clase para representar una arista en el grafo.
     */
    private static class Arista {
        String destino;
        double peso;
        
        Arista(String destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }
    }
    
    /**
     * Clase para el algoritmo de Dijkstra.
     */
    private static class NodoDijkstra implements Comparable<NodoDijkstra> {
        String usuarioId;
        double similitud;
        
        NodoDijkstra(String usuarioId, double similitud) {
            this.usuarioId = usuarioId;
            this.similitud = similitud;
        }
        
        @Override
        public int compareTo(NodoDijkstra otro) {
            // Orden descendente por similitud (mayor similitud = mayor prioridad)
            return Double.compare(otro.similitud, this.similitud);
        }
    }
    
    /**
     * Clase para representar un usuario con su similitud.
     */
    public static class UsuarioSimilar {
        public final Usuario usuario;
        public final double similitud;
        
        public UsuarioSimilar(Usuario usuario, double similitud) {
            this.usuario = usuario;
            this.similitud = similitud;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%.3f)", usuario.getUsername(), similitud);
        }
    }
    
    /**
     * Clase auxiliar para recomendaciones de canciones.
     */
    private static class CancionRecomendada {
        Cancion cancion;
        double puntaje;
        
        CancionRecomendada(Cancion cancion, double puntaje) {
            this.cancion = cancion;
            this.puntaje = puntaje;
        }
    }
    
    // Getters
    
    public int getNumeroNodos() {
        return numeroNodos;
    }
    
    public int getNumeroAristas() {
        return numeroAristas / 2; // Dividir por 2 porque las aristas son bidireccionales
    }
    
    public Set<String> getUsuarios() {
        return new HashSet<>(usuarios.keySet());
    }
    
    /**
     * Verifica si dos usuarios están conectados directamente.
     * 
     * @param userId1 ID del primer usuario
     * @param userId2 ID del segundo usuario
     * @return true si están conectados, false en caso contrario
     */
    public boolean estanConectados(String userId1, String userId2) {
        if (!grafo.containsKey(userId1)) {
            return false;
        }
        
        return grafo.get(userId1).stream()
            .anyMatch(arista -> arista.destino.equals(userId2));
    }
    
    /**
     * Obtiene el peso de la arista entre dos usuarios.
     * 
     * @param userId1 ID del primer usuario
     * @param userId2 ID del segundo usuario
     * @return Peso de la arista, 0.0 si no están conectados
     */
    public double obtenerPesoArista(String userId1, String userId2) {
        if (!grafo.containsKey(userId1)) {
            return 0.0;
        }
        
        return grafo.get(userId1).stream()
            .filter(arista -> arista.destino.equals(userId2))
            .mapToDouble(arista -> arista.peso)
            .findFirst()
            .orElse(0.0);
    }
}