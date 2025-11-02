package com.syncup.structures;

import com.syncup.models.Usuario;
import java.util.*;

/**
 * Implementación de Grafo No Dirigido para la red social de usuarios en SyncUp.
 * RF-023, RF-024: Utiliza BFS para encontrar conexiones sociales y sugerir usuarios.
 * Permite seguimiento entre usuarios y descubrimiento de "amigos de amigos".
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class GrafoSocial {
    
    /** Mapa de adyacencias que representa las conexiones sociales */
    private HashMap<String, List<String>> grafo;
    
    /** Mapa para acceso rápido a usuarios por ID */
    private HashMap<String, Usuario> usuarios;
    
    /** Número total de nodos (usuarios) en el grafo */
    private int numeroNodos;
    
    /** Número total de conexiones sociales */
    private int numeroConexiones;
    
    /**
     * Constructor que inicializa el grafo social vacío.
     */
    public GrafoSocial() {
        this.grafo = new HashMap<>();
        this.usuarios = new HashMap<>();
        this.numeroNodos = 0;
        this.numeroConexiones = 0;
    }
    
    /**
     * Agrega un usuario al grafo social.
     * RF-023: Añadir usuarios al grafo no dirigido.
     * 
     * @param usuario Usuario a agregar
     * @return true si se agregó exitosamente, false si ya existía
     */
    public boolean agregarUsuario(Usuario usuario) {
        if (usuario == null || usuarios.containsKey(usuario.getId())) {
            return false;
        }
        
        usuarios.put(usuario.getId(), usuario);
        grafo.put(usuario.getId(), new ArrayList<>());
        numeroNodos++;
        
        System.out.println("Usuario agregado al grafo social: " + usuario.getUsername());
        return true;
    }
    
    /**
     * Crea una conexión bidireccional entre dos usuarios.
     * RF-023: Conexiones no dirigidas en el grafo social.
     * 
     * @param usuarioId1 ID del primer usuario
     * @param usuarioId2 ID del segundo usuario
     * @return true si se creó la conexión, false si ya existía o hay error
     */
    public boolean conectarUsuarios(String usuarioId1, String usuarioId2) {
        if (usuarioId1 == null || usuarioId2 == null || usuarioId1.equals(usuarioId2)) {
            return false;
        }
        
        if (!grafo.containsKey(usuarioId1) || !grafo.containsKey(usuarioId2)) {
            return false;
        }
        
        // Verificar si ya están conectados
        if (estanConectados(usuarioId1, usuarioId2)) {
            return false;
        }
        
        // Crear conexión bidireccional
        grafo.get(usuarioId1).add(usuarioId2);
        grafo.get(usuarioId2).add(usuarioId1);
        numeroConexiones++;
        
        return true;
    }
    
    /**
     * Elimina la conexión entre dos usuarios.
     * 
     * @param usuarioId1 ID del primer usuario
     * @param usuarioId2 ID del segundo usuario
     * @return true si se eliminó la conexión, false si no existía
     */
    public boolean desconectarUsuarios(String usuarioId1, String usuarioId2) {
        if (!estanConectados(usuarioId1, usuarioId2)) {
            return false;
        }
        
        grafo.get(usuarioId1).remove(usuarioId2);
        grafo.get(usuarioId2).remove(usuarioId1);
        numeroConexiones--;
        
        return true;
    }
    
    /**
     * Verifica si dos usuarios están directamente conectados.
     * 
     * @param usuarioId1 ID del primer usuario
     * @param usuarioId2 ID del segundo usuario
     * @return true si están conectados, false en caso contrario
     */
    public boolean estanConectados(String usuarioId1, String usuarioId2) {
        if (!grafo.containsKey(usuarioId1)) {
            return false;
        }
        return grafo.get(usuarioId1).contains(usuarioId2);
    }
    
    /**
     * RF-024: Obtiene sugerencias de usuarios usando BFS para encontrar "amigos de amigos".
     * Encuentra usuarios a distancia 2 (amigos de amigos) como sugerencias.
     * 
     * @param usuarioId ID del usuario base
     * @param limite Número máximo de sugerencias
     * @return Lista de usuarios sugeridos
     */
    public List<Usuario> obtenerSugerenciasUsuarios(String usuarioId, int limite) {
        if (!grafo.containsKey(usuarioId)) {
            return new ArrayList<>();
        }
        
        List<Usuario> sugerencias = new ArrayList<>();
        Set<String> visitados = new HashSet<>();
        Set<String> amigosDirectos = new HashSet<>(grafo.get(usuarioId));
        Queue<NodoBFS> cola = new LinkedList<>();
        
        // Inicializar BFS desde el usuario base
        cola.offer(new NodoBFS(usuarioId, 0));
        visitados.add(usuarioId);
        
        while (!cola.isEmpty() && sugerencias.size() < limite) {
            NodoBFS nodoActual = cola.poll();
            
            // Explorar vecinos
            for (String vecinoId : grafo.get(nodoActual.usuarioId)) {
                if (!visitados.contains(vecinoId)) {
                    visitados.add(vecinoId);
                    
                    // Si está a distancia 2 (amigo de amigo) y no es amigo directo, es sugerencia
                    if (nodoActual.distancia == 1 && !amigosDirectos.contains(vecinoId)) {
                        Usuario usuarioSugerido = usuarios.get(vecinoId);
                        if (usuarioSugerido != null) {
                            sugerencias.add(usuarioSugerido);
                        }
                    }
                    
                    // Continuar BFS solo hasta distancia 2
                    if (nodoActual.distancia < 2) {
                        cola.offer(new NodoBFS(vecinoId, nodoActual.distancia + 1));
                    }
                }
            }
        }
        
        return sugerencias;
    }
    
    /**
     * RF-024: Encuentra el camino más corto entre dos usuarios usando BFS.
     * 
     * @param usuarioOrigenId ID del usuario origen
     * @param usuarioDestinoId ID del usuario destino
     * @return Lista con el camino más corto (incluye origen y destino)
     */
    public List<Usuario> encontrarCaminoMasCorto(String usuarioOrigenId, String usuarioDestinoId) {
        if (!grafo.containsKey(usuarioOrigenId) || !grafo.containsKey(usuarioDestinoId)) {
            return new ArrayList<>();
        }
        
        if (usuarioOrigenId.equals(usuarioDestinoId)) {
            List<Usuario> camino = new ArrayList<>();
            camino.add(usuarios.get(usuarioOrigenId));
            return camino;
        }
        
        Queue<String> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        HashMap<String, String> predecesores = new HashMap<>();
        
        // Inicializar BFS
        cola.offer(usuarioOrigenId);
        visitados.add(usuarioOrigenId);
        predecesores.put(usuarioOrigenId, null);
        
        boolean encontrado = false;
        
        // Realizar BFS
        while (!cola.isEmpty() && !encontrado) {
            String usuarioActual = cola.poll();
            
            for (String vecino : grafo.get(usuarioActual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    predecesores.put(vecino, usuarioActual);
                    cola.offer(vecino);
                    
                    if (vecino.equals(usuarioDestinoId)) {
                        encontrado = true;
                        break;
                    }
                }
            }
        }
        
        // Reconstruir camino si se encontró
        if (encontrado) {
            return reconstruirCamino(predecesores, usuarioOrigenId, usuarioDestinoId);
        }
        
        return new ArrayList<>(); // No hay camino
    }
    
    /**
     * RF-024: Obtiene todos los usuarios a una distancia específica usando BFS.
     * 
     * @param usuarioId ID del usuario centro
     * @param distancia Distancia exacta a buscar
     * @return Lista de usuarios a la distancia especificada
     */
    public List<Usuario> obtenerUsuariosADistancia(String usuarioId, int distancia) {
        if (!grafo.containsKey(usuarioId) || distancia < 0) {
            return new ArrayList<>();
        }
        
        if (distancia == 0) {
            List<Usuario> resultado = new ArrayList<>();
            resultado.add(usuarios.get(usuarioId));
            return resultado;
        }
        
        List<Usuario> usuariosADistancia = new ArrayList<>();
        Queue<NodoBFS> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        
        cola.offer(new NodoBFS(usuarioId, 0));
        visitados.add(usuarioId);
        
        while (!cola.isEmpty()) {
            NodoBFS nodoActual = cola.poll();
            
            if (nodoActual.distancia == distancia) {
                Usuario usuario = usuarios.get(nodoActual.usuarioId);
                if (usuario != null) {
                    usuariosADistancia.add(usuario);
                }
                continue; // No explorar más desde este nodo
            }
            
            if (nodoActual.distancia < distancia) {
                for (String vecinoId : grafo.get(nodoActual.usuarioId)) {
                    if (!visitados.contains(vecinoId)) {
                        visitados.add(vecinoId);
                        cola.offer(new NodoBFS(vecinoId, nodoActual.distancia + 1));
                    }
                }
            }
        }
        
        return usuariosADistancia;
    }
    
    /**
     * Calcula la distancia más corta entre dos usuarios usando BFS.
     * 
     * @param usuarioId1 ID del primer usuario
     * @param usuarioId2 ID del segundo usuario
     * @return Distancia entre usuarios, -1 si no están conectados
     */
    public int calcularDistancia(String usuarioId1, String usuarioId2) {
        if (!grafo.containsKey(usuarioId1) || !grafo.containsKey(usuarioId2)) {
            return -1;
        }
        
        if (usuarioId1.equals(usuarioId2)) {
            return 0;
        }
        
        Queue<NodoBFS> cola = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        
        cola.offer(new NodoBFS(usuarioId1, 0));
        visitados.add(usuarioId1);
        
        while (!cola.isEmpty()) {
            NodoBFS nodoActual = cola.poll();
            
            for (String vecino : grafo.get(nodoActual.usuarioId)) {
                if (!visitados.contains(vecino)) {
                    if (vecino.equals(usuarioId2)) {
                        return nodoActual.distancia + 1;
                    }
                    
                    visitados.add(vecino);
                    cola.offer(new NodoBFS(vecino, nodoActual.distancia + 1));
                }
            }
        }
        
        return -1; // No conectados
    }
    
    /**
     * Obtiene todos los amigos directos de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de amigos directos
     */
    public List<Usuario> obtenerAmigosDirectos(String usuarioId) {
        if (!grafo.containsKey(usuarioId)) {
            return new ArrayList<>();
        }
        
        List<Usuario> amigos = new ArrayList<>();
        for (String amigoId : grafo.get(usuarioId)) {
            Usuario amigo = usuarios.get(amigoId);
            if (amigo != null) {
                amigos.add(amigo);
            }
        }
        
        return amigos;
    }
    
    /**
     * RF-024: Encuentra componentes conexas del grafo usando BFS.
     * Útil para análisis de comunidades de usuarios.
     * 
     * @return Lista de componentes conexas (cada una es lista de usuarios)
     */
    public List<List<Usuario>> encontrarComponentesConexas() {
        List<List<Usuario>> componentes = new ArrayList<>();
        Set<String> visitadosGlobal = new HashSet<>();
        
        for (String usuarioId : grafo.keySet()) {
            if (!visitadosGlobal.contains(usuarioId)) {
                // Nueva componente conexa
                List<Usuario> componente = new ArrayList<>();
                Queue<String> cola = new LinkedList<>();
                Set<String> visitadosLocal = new HashSet<>();
                
                cola.offer(usuarioId);
                visitadosLocal.add(usuarioId);
                visitadosGlobal.add(usuarioId);
                
                // BFS para encontrar toda la componente
                while (!cola.isEmpty()) {
                    String actual = cola.poll();
                    Usuario usuario = usuarios.get(actual);
                    if (usuario != null) {
                        componente.add(usuario);
                    }
                    
                    for (String vecino : grafo.get(actual)) {
                        if (!visitadosLocal.contains(vecino)) {
                            visitadosLocal.add(vecino);
                            visitadosGlobal.add(vecino);
                            cola.offer(vecino);
                        }
                    }
                }
                
                componentes.add(componente);
            }
        }
        
        return componentes;
    }
    
    /**
     * RF-024: Obtiene usuarios populares basado en número de conexiones (centralidad de grado).
     * 
     * @param limite Número máximo de usuarios populares
     * @return Lista de usuarios ordenados por popularidad
     */
    public List<UsuarioPopular> obtenerUsuariosPopulares(int limite) {
        List<UsuarioPopular> usuariosPopulares = new ArrayList<>();
        
        for (String usuarioId : grafo.keySet()) {
            Usuario usuario = usuarios.get(usuarioId);
            if (usuario != null) {
                int numeroConexiones = grafo.get(usuarioId).size();
                usuariosPopulares.add(new UsuarioPopular(usuario, numeroConexiones));
            }
        }
        
        // Ordenar por número de conexiones descendente
        usuariosPopulares.sort((a, b) -> Integer.compare(b.numeroConexiones, a.numeroConexiones));
        
        return usuariosPopulares.subList(0, Math.min(limite, usuariosPopulares.size()));
    }
    
    /**
     * Reconstruye el camino desde el mapa de predecesores.
     * 
     * @param predecesores Mapa de predecesores del BFS
     * @param origen Usuario origen
     * @param destino Usuario destino
     * @return Lista con el camino reconstruido
     */
    private List<Usuario> reconstruirCamino(HashMap<String, String> predecesores, String origen, String destino) {
        List<String> caminoIds = new ArrayList<>();
        String actual = destino;
        
        while (actual != null) {
            caminoIds.add(actual);
            actual = predecesores.get(actual);
        }
        
        Collections.reverse(caminoIds);
        
        List<Usuario> camino = new ArrayList<>();
        for (String id : caminoIds) {
            Usuario usuario = usuarios.get(id);
            if (usuario != null) {
                camino.add(usuario);
            }
        }
        
        return camino;
    }
    
    /**
     * Obtiene el grado (número de conexiones) de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Número de conexiones del usuario
     */
    public int obtenerGrado(String usuarioId) {
        if (!grafo.containsKey(usuarioId)) {
            return 0;
        }
        return grafo.get(usuarioId).size();
    }
    
    /**
     * Verifica si el grafo está conectado (existe camino entre cualquier par de nodos).
     * 
     * @return true si el grafo es conexo, false en caso contrario
     */
    public boolean esConexo() {
        if (numeroNodos <= 1) {
            return true;
        }
        
        // Usar BFS desde cualquier nodo para ver si alcanza todos los nodos
        String nodoInicial = grafo.keySet().iterator().next();
        Set<String> alcanzables = new HashSet<>();
        Queue<String> cola = new LinkedList<>();
        
        cola.offer(nodoInicial);
        alcanzables.add(nodoInicial);
        
        while (!cola.isEmpty()) {
            String actual = cola.poll();
            
            for (String vecino : grafo.get(actual)) {
                if (!alcanzables.contains(vecino)) {
                    alcanzables.add(vecino);
                    cola.offer(vecino);
                }
            }
        }
        
        return alcanzables.size() == numeroNodos;
    }
    
    /**
     * Obtiene estadísticas del grafo social.
     * 
     * @return String con estadísticas detalladas
     */
    public String getEstadisticas() {
        double densidad = numeroNodos > 1 ? 
            (2.0 * numeroConexiones) / (numeroNodos * (numeroNodos - 1)) : 0.0;
        
        List<List<Usuario>> componentes = encontrarComponentesConexas();
        
        return String.format(
            "=== Estadísticas del Grafo Social ===\n" +
            "Número de usuarios: %d\n" +
            "Número de conexiones: %d\n" +
            "Densidad del grafo: %.3f\n" +
            "Es conexo: %s\n" +
            "Componentes conexas: %d\n" +
            "Componente más grande: %d usuarios",
            numeroNodos, numeroConexiones, densidad, 
            esConexo() ? "Sí" : "No", componentes.size(),
            componentes.isEmpty() ? 0 : componentes.stream().mapToInt(List::size).max().orElse(0)
        );
    }
    
    /**
     * Sincroniza las conexiones sociales con los datos de seguimiento de usuarios.
     * Lee las listas de seguidos/seguidores de los usuarios y crea las conexiones.
     */
    public void sincronizarConUsuarios() {
        System.out.println("Sincronizando grafo social con datos de usuarios...");
        
        // Limpiar conexiones existentes
        for (List<String> conexiones : grafo.values()) {
            conexiones.clear();
        }
        numeroConexiones = 0;
        
        // Crear conexiones basadas en seguimiento mutuo
        for (Usuario usuario : usuarios.values()) {
            for (String seguidoId : usuario.getUsuariosSeguidos()) {
                Usuario seguido = usuarios.get(seguidoId);
                if (seguido != null && seguido.getUsuariosSeguidos().contains(usuario.getId())) {
                    // Conexión mutua - crear arista
                    conectarUsuarios(usuario.getId(), seguidoId);
                }
            }
        }
        
        System.out.println("Sincronización completada. Conexiones mutuas: " + numeroConexiones);
    }
    
    // Getters
    
    public int getNumeroNodos() {
        return numeroNodos;
    }
    
    public int getNumeroConexiones() {
        return numeroConexiones;
    }
    
    public Set<String> getUsuarios() {
        return new HashSet<>(usuarios.keySet());
    }
    
    // Clases auxiliares
    
    /**
     * Clase auxiliar para BFS con distancia.
     */
    private static class NodoBFS {
        String usuarioId;
        int distancia;
        
        NodoBFS(String usuarioId, int distancia) {
            this.usuarioId = usuarioId;
            this.distancia = distancia;
        }
    }
    
    /**
     * Clase para representar usuario con su popularidad social.
     */
    public static class UsuarioPopular {
        public final Usuario usuario;
        public final int numeroConexiones;
        
        public UsuarioPopular(Usuario usuario, int numeroConexiones) {
            this.usuario = usuario;
            this.numeroConexiones = numeroConexiones;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%d conexiones)", usuario.getUsername(), numeroConexiones);
        }
    }
}