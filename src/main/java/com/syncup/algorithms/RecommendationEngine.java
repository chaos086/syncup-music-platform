package com.syncup.algorithms;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.structures.GrafoDeSimilitud;
import com.syncup.data.DataManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Motor de recomendaciones inteligentes para SyncUp.
 * Utiliza múltiples algoritmos y estructuras de datos para generar
 * recomendaciones personalizadas de música.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class RecommendationEngine {
    
    /** Grafo de similitud para recomendaciones colaborativas */
    private GrafoDeSimilitud grafoDeSimilitud;
    
    /** Gestor de datos del sistema */
    private DataManager dataManager;
    
    /** Cache de recomendaciones recientes */
    private HashMap<String, CacheRecomendaciones> cacheRecomendaciones;
    
    /** Tiempo de vida del cache en millisegundos (30 minutos) */
    private static final long CACHE_TTL = 30 * 60 * 1000;
    
    /** Número máximo de recomendaciones por defecto */
    private static final int MAX_RECOMENDACIONES_DEFAULT = 20;
    
    /** Número de usuarios similares a considerar */
    private static final int USUARIOS_SIMILARES_LIMITE = 15;
    
    /**
     * Constructor del motor de recomendaciones.
     */
    public RecommendationEngine() {
        this.grafoDeSimilitud = new GrafoDeSimilitud();
        this.dataManager = DataManager.getInstance();
        this.cacheRecomendaciones = new HashMap<>();
        
        inicializarSistema();
    }
    
    /**
     * Inicializa el sistema de recomendaciones cargando datos y construyendo índices.
     */
    private void inicializarSistema() {
        System.out.println("Inicializando motor de recomendaciones...");
        
        // Cargar usuarios en el grafo
        List<Usuario> usuarios = dataManager.getAllUsuarios();
        for (Usuario usuario : usuarios) {
            grafoDeSimilitud.agregarUsuario(usuario);
        }
        
        // Cargar canciones en el grafo
        List<Cancion> canciones = dataManager.getAllCanciones();
        for (Cancion cancion : canciones) {
            grafoDeSimilitud.agregarCancion(cancion);
        }
        
        // Calcular similitudes iniciales
        if (!usuarios.isEmpty()) {
            grafoDeSimilitud.calcularSimilitudes();
        }
        
        System.out.println("Motor de recomendaciones inicializado.");
        System.out.println(grafoDeSimilitud.getEstadisticas());
    }
    
    /**
     * Genera una playlist "Descubrimiento Semanal" personalizada para un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Lista de canciones recomendadas
     */
    public List<Cancion> generarDescubrimientoSemanal(String usuarioId) {
        return generarDescubrimientoSemanal(usuarioId, MAX_RECOMENDACIONES_DEFAULT);
    }
    
    /**
     * Genera una playlist "Descubrimiento Semanal" personalizada para un usuario.
     * 
     * @param usuarioId ID del usuario
     * @param limite Número máximo de canciones
     * @return Lista de canciones recomendadas
     */
    public List<Cancion> generarDescubrimientoSemanal(String usuarioId, int limite) {
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) {
            return new ArrayList<>();
        }
        
        // Verificar cache
        CacheRecomendaciones cache = cacheRecomendaciones.get(usuarioId + "_weekly");
        if (cache != null && !cache.isExpired()) {
            return cache.recomendaciones.subList(0, Math.min(limite, cache.recomendaciones.size()));
        }
        
        System.out.println("Generando Descubrimiento Semanal para: " + usuario.getUsername());
        
        List<Cancion> recomendaciones = new ArrayList<>();
        
        // 1. Recomendaciones basadas en filtrado colaborativo (60%)
        List<Cancion> colaborativas = obtenerRecomendacionesColaborativas(usuarioId, (int)(limite * 0.6));
        recomendaciones.addAll(colaborativas);
        
        // 2. Recomendaciones basadas en contenido (30%)
        List<Cancion> contenido = obtenerRecomendacionesBasadasEnContenido(usuarioId, (int)(limite * 0.3));
        agregarSinDuplicados(recomendaciones, contenido);
        
        // 3. Recomendaciones por popularidad y tendencias (10%)
        List<Cancion> populares = obtenerRecomendacionesPopulares(usuarioId, (int)(limite * 0.1));
        agregarSinDuplicados(recomendaciones, populares);
        
        // Completar con recomendaciones adicionales si es necesario
        if (recomendaciones.size() < limite) {
            List<Cancion> adicionales = obtenerRecomendacionesAdicionales(usuarioId, usuario, limite - recomendaciones.size());
            agregarSinDuplicados(recomendaciones, adicionales);
        }
        
        // Mezclar para variedad
        Collections.shuffle(recomendaciones);
        
        // Limitar al número solicitado
        if (recomendaciones.size() > limite) {
            recomendaciones = recomendaciones.subList(0, limite);
        }
        
        // Guardar en cache
        cacheRecomendaciones.put(usuarioId + "_weekly", new CacheRecomendaciones(new ArrayList<>(recomendaciones)));
        
        System.out.println("Descubrimiento Semanal generado: " + recomendaciones.size() + " canciones");
        return recomendaciones;
    }
    
    /**
     * Genera una radio personalizada basada en una canción semilla.
     * 
     * @param usuarioId ID del usuario
     * @param cancionSemilla Canción base para la radio
     * @param limite Número de canciones para la radio
     * @return Lista de canciones para la radio
     */
    public List<Cancion> generarRadioPersonalizada(String usuarioId, Cancion cancionSemilla, int limite) {
        if (cancionSemilla == null) {
            return generarDescubrimientoSemanal(usuarioId, limite);
        }
        
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) {
            return new ArrayList<>();
        }
        
        System.out.println("Generando radio personalizada basada en: " + cancionSemilla.getTitulo());
        
        List<Cancion> radioPlaylist = new ArrayList<>();
        Set<String> cancionesUsadas = new HashSet<>();
        cancionesUsadas.add(cancionSemilla.getId());
        
        // 1. Agregar canciones del mismo artista (20%)
        List<Cancion> delMismoArtista = encontrarCancionesDelMismoArtista(cancionSemilla, (int)(limite * 0.2));
        for (Cancion cancion : delMismoArtista) {
            if (!cancionesUsadas.contains(cancion.getId())) {
                radioPlaylist.add(cancion);
                cancionesUsadas.add(cancion.getId());
            }
        }
        
        // 2. Agregar canciones del mismo género (40%)
        List<Cancion> delMismoGenero = encontrarCancionesDelMismoGenero(cancionSemilla, usuario, (int)(limite * 0.4));
        for (Cancion cancion : delMismoGenero) {
            if (!cancionesUsadas.contains(cancion.getId()) && radioPlaylist.size() < limite) {
                radioPlaylist.add(cancion);
                cancionesUsadas.add(cancion.getId());
            }
        }
        
        // 3. Agregar recomendaciones colaborativas (30%)
        List<Cancion> colaborativas = obtenerRecomendacionesColaborativas(usuarioId, (int)(limite * 0.3));
        for (Cancion cancion : colaborativas) {
            if (!cancionesUsadas.contains(cancion.getId()) && radioPlaylist.size() < limite) {
                radioPlaylist.add(cancion);
                cancionesUsadas.add(cancion.getId());
            }
        }
        
        // 4. Completar con canciones populares si es necesario (10%)
        if (radioPlaylist.size() < limite) {
            List<Cancion> populares = obtenerCancionesPopulares(limite - radioPlaylist.size());
            for (Cancion cancion : populares) {
                if (!cancionesUsadas.contains(cancion.getId()) && radioPlaylist.size() < limite) {
                    radioPlaylist.add(cancion);
                    cancionesUsadas.add(cancion.getId());
                }
            }
        }
        
        // Mezclar manteniendo cierta coherencia
        Collections.shuffle(radioPlaylist);
        
        System.out.println("Radio personalizada generada: " + radioPlaylist.size() + " canciones");
        return radioPlaylist;
    }
    
    /**
     * Obtiene recomendaciones basadas en filtrado colaborativo.
     * 
     * @param usuarioId ID del usuario
     * @param limite Número máximo de recomendaciones
     * @return Lista de recomendaciones colaborativas
     */
    private List<Cancion> obtenerRecomendacionesColaborativas(String usuarioId, int limite) {
        return grafoDeSimilitud.obtenerRecomendaciones(usuarioId, limite);
    }
    
    /**
     * Obtiene recomendaciones basadas en el contenido de las canciones favoritas del usuario.
     * 
     * @param usuarioId ID del usuario
     * @param limite Número máximo de recomendaciones
     * @return Lista de recomendaciones basadas en contenido
     */
    private List<Cancion> obtenerRecomendacionesBasadasEnContenido(String usuarioId, int limite) {
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) {
            return new ArrayList<>();
        }
        
        List<Cancion> recomendaciones = new ArrayList<>();
        Set<String> cancionesPropias = new HashSet<>(usuario.getCancionesFavoritas());
        
        // Analizar patrones en canciones favoritas
        Map<String, Integer> generosPopulares = new HashMap<>();
        Map<String, Integer> artistasPopulares = new HashMap<>();
        
        for (String cancionId : usuario.getCancionesFavoritas()) {
            Cancion cancion = dataManager.getCancionById(cancionId);
            if (cancion != null) {
                generosPopulares.put(cancion.getGenero(), 
                    generosPopulares.getOrDefault(cancion.getGenero(), 0) + 1);
                artistasPopulares.put(cancion.getArtista(), 
                    artistasPopulares.getOrDefault(cancion.getArtista(), 0) + 1);
            }
        }
        
        // Encontrar canciones similares
        List<Cancion> todasLasCanciones = dataManager.getAllCanciones();
        List<CancionPuntuada> candidatas = new ArrayList<>();
        
        for (Cancion cancion : todasLasCanciones) {
            if (!cancionesPropias.contains(cancion.getId())) {
                double puntaje = calcularPuntajeContenido(cancion, generosPopulares, artistasPopulares);
                if (puntaje > 0) {
                    candidatas.add(new CancionPuntuada(cancion, puntaje));
                }
            }
        }
        
        // Ordenar por puntaje y tomar las mejores
        candidatas.sort((a, b) -> Double.compare(b.puntaje, a.puntaje));
        
        for (int i = 0; i < Math.min(limite, candidatas.size()); i++) {
            recomendaciones.add(candidatas.get(i).cancion);
        }
        
        return recomendaciones;
    }
    
    /**
     * Calcula el puntaje de contenido para una canción.
     * 
     * @param cancion Canción a evaluar
     * @param generosPopulares Mapa de géneros populares del usuario
     * @param artistasPopulares Mapa de artistas populares del usuario
     * @return Puntaje de la canción
     */
    private double calcularPuntajeContenido(Cancion cancion, Map<String, Integer> generosPopulares, 
                                           Map<String, Integer> artistasPopulares) {
        double puntaje = 0.0;
        
        // Puntaje por género
        Integer frecuenciaGenero = generosPopulares.get(cancion.getGenero());
        if (frecuenciaGenero != null) {
            puntaje += frecuenciaGenero * 2.0;
        }
        
        // Puntaje por artista
        Integer frecuenciaArtista = artistasPopulares.get(cancion.getArtista());
        if (frecuenciaArtista != null) {
            puntaje += frecuenciaArtista * 3.0;
        }
        
        // Puntaje por popularidad general
        puntaje += Math.log(1 + cancion.getReproducciones()) * 0.1;
        puntaje += Math.log(1 + cancion.getNumeroFavoritos()) * 0.2;
        puntaje += cancion.getCalificacionPromedio() * 0.5;
        
        return puntaje;
    }
    
    /**
     * Obtiene recomendaciones basadas en popularidad y tendencias.
     * 
     * @param usuarioId ID del usuario
     * @param limite Número máximo de recomendaciones
     * @return Lista de recomendaciones populares
     */
    private List<Cancion> obtenerRecomendacionesPopulares(String usuarioId, int limite) {
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) {
            return new ArrayList<>();
        }
        
        Set<String> cancionesPropias = new HashSet<>(usuario.getCancionesFavoritas());
        
        return dataManager.getAllCanciones().stream()
            .filter(cancion -> !cancionesPropias.contains(cancion.getId()))
            .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene recomendaciones adicionales para completar la lista.
     * 
     * @param usuarioId ID del usuario
     * @param usuario Objeto usuario
     * @param limite Número máximo de recomendaciones
     * @return Lista de recomendaciones adicionales
     */
    private List<Cancion> obtenerRecomendacionesAdicionales(String usuarioId, Usuario usuario, int limite) {
        Set<String> cancionesPropias = new HashSet<>(usuario.getCancionesFavoritas());
        List<String> generosFavoritos = usuario.getGenerosFavoritos();
        
        List<Cancion> adicionales = new ArrayList<>();
        
        // Buscar canciones de géneros favoritos
        for (String genero : generosFavoritos) {
            List<Cancion> delGenero = dataManager.getAllCanciones().stream()
                .filter(cancion -> !cancionesPropias.contains(cancion.getId()))
                .filter(cancion -> cancion.getGenero().equalsIgnoreCase(genero))
                .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
                .limit(limite / Math.max(1, generosFavoritos.size()))
                .collect(Collectors.toList());
            
            adicionales.addAll(delGenero);
            if (adicionales.size() >= limite) break;
        }
        
        return adicionales.subList(0, Math.min(limite, adicionales.size()));
    }
    
    /**
     * Encuentra canciones del mismo artista.
     * 
     * @param cancionSemilla Canción base
     * @param limite Número máximo de canciones
     * @return Lista de canciones del mismo artista
     */
    private List<Cancion> encontrarCancionesDelMismoArtista(Cancion cancionSemilla, int limite) {
        return dataManager.getAllCanciones().stream()
            .filter(cancion -> !cancion.getId().equals(cancionSemilla.getId()))
            .filter(cancion -> cancion.getArtista().equalsIgnoreCase(cancionSemilla.getArtista()) ||
                              cancion.getArtistasColaboradores().contains(cancionSemilla.getArtista()))
            .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    /**
     * Encuentra canciones del mismo género.
     * 
     * @param cancionSemilla Canción base
     * @param usuario Usuario para personalizar
     * @param limite Número máximo de canciones
     * @return Lista de canciones del mismo género
     */
    private List<Cancion> encontrarCancionesDelMismoGenero(Cancion cancionSemilla, Usuario usuario, int limite) {
        Set<String> cancionesPropias = new HashSet<>(usuario.getCancionesFavoritas());
        
        return dataManager.getAllCanciones().stream()
            .filter(cancion -> !cancion.getId().equals(cancionSemilla.getId()))
            .filter(cancion -> !cancionesPropias.contains(cancion.getId()))
            .filter(cancion -> cancion.getGenero().equalsIgnoreCase(cancionSemilla.getGenero()))
            .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene canciones populares generales.
     * 
     * @param limite Número máximo de canciones
     * @return Lista de canciones populares
     */
    private List<Cancion> obtenerCancionesPopulares(int limite) {
        return dataManager.getAllCanciones().stream()
            .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    /**
     * Agrega canciones a una lista sin duplicados.
     * 
     * @param destino Lista destino
     * @param fuente Lista fuente
     */
    private void agregarSinDuplicados(List<Cancion> destino, List<Cancion> fuente) {
        Set<String> idsExistentes = destino.stream()
            .map(Cancion::getId)
            .collect(Collectors.toSet());
        
        for (Cancion cancion : fuente) {
            if (!idsExistentes.contains(cancion.getId())) {
                destino.add(cancion);
                idsExistentes.add(cancion.getId());
            }
        }
    }
    
    /**
     * Actualiza el sistema de recomendaciones con nuevos datos.
     */
    public void actualizarSistema() {
        System.out.println("Actualizando sistema de recomendaciones...");
        
        // Limpiar cache
        cacheRecomendaciones.clear();
        
        // Reinicializar grafo
        grafoDeSimilitud = new GrafoDeSimilitud();
        inicializarSistema();
        
        System.out.println("Sistema de recomendaciones actualizado.");
    }
    
    /**
     * Limpia el cache de recomendaciones expirado.
     */
    public void limpiarCacheExpirado() {
        cacheRecomendaciones.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Obtiene estadísticas del motor de recomendaciones.
     * 
     * @return String con estadísticas
     */
    public String getEstadisticas() {
        return String.format(
            "=== Motor de Recomendaciones - Estadísticas ===\n" +
            "%s\n" +
            "Entradas en cache: %d\n" +
            "Algoritmos disponibles: Colaborativo, Contenido, Popularidad\n" +
            "Estado: Operativo",
            grafoDeSimilitud.getEstadisticas(),
            cacheRecomendaciones.size()
        );
    }
    
    // Clases auxiliares
    
    /**
     * Clase para almacenar recomendaciones en cache.
     */
    private static class CacheRecomendaciones {
        final List<Cancion> recomendaciones;
        final long timestamp;
        
        CacheRecomendaciones(List<Cancion> recomendaciones) {
            this.recomendaciones = recomendaciones;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > CACHE_TTL;
        }
    }
    
    /**
     * Clase auxiliar para canciones con puntaje.
     */
    private static class CancionPuntuada {
        final Cancion cancion;
        final double puntaje;
        
        CancionPuntuada(Cancion cancion, double puntaje) {
            this.cancion = cancion;
            this.puntaje = puntaje;
        }
    }
}