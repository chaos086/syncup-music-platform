package com.syncup.services;

import com.syncup.models.Cancion;
import com.syncup.data.DataManager;
import com.syncup.structures.TrieAutocompletado;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Servicio de búsqueda avanzada con soporte para concurrencia y múltiples filtros.
 * RF-004: Búsquedas avanzadas con lógica AND/OR en múltiples atributos.
 * RF-030: Implementación con threading para búsquedas paralelas.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class SearchService {
    
    /** Gestor de datos del sistema */
    private DataManager dataManager;
    
    /** Pool de hilos para búsquedas concurrentes */
    private ExecutorService threadPool;
    
    /** Tamaño del pool de hilos */
    private static final int THREAD_POOL_SIZE = 4;
    
    /** Timeout para búsquedas en millisegundos */
    private static final long SEARCH_TIMEOUT_MS = 5000;
    
    /**
     * Constructor del servicio de búsqueda.
     */
    public SearchService() {
        this.dataManager = DataManager.getInstance();
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        System.out.println("SearchService inicializado con " + THREAD_POOL_SIZE + " hilos");
    }
    
    /**
     * RF-004: Realiza búsqueda avanzada con múltiples filtros y lógica AND/OR.
     * RF-030: Utiliza threading para búsquedas paralelas optimizadas.
     * 
     * @param criterios Criterios de búsqueda
     * @return Lista de canciones que cumplen los criterios
     */
    public List<Cancion> busquedaAvanzada(CriteriosBusqueda criterios) {
        if (criterios == null) {
            return new ArrayList<>();
        }
        
        System.out.println("Iniciando búsqueda avanzada con threading: " + criterios);
        long tiempoInicio = System.currentTimeMillis();
        
        try {
            // Crear tareas de búsqueda paralelas
            List<Future<List<Cancion>>> futures = new ArrayList<>();
            
            // Tarea 1: Búsqueda por título
            if (criterios.titulo != null && !criterios.titulo.trim().isEmpty()) {
                futures.add(threadPool.submit(new BusquedaTituloTask(criterios.titulo)));
            }
            
            // Tarea 2: Búsqueda por artista
            if (criterios.artista != null && !criterios.artista.trim().isEmpty()) {
                futures.add(threadPool.submit(new BusquedaArtistaTask(criterios.artista)));
            }
            
            // Tarea 3: Búsqueda por género
            if (criterios.genero != null && !criterios.genero.trim().isEmpty()) {
                futures.add(threadPool.submit(new BusquedaGeneroTask(criterios.genero)));
            }
            
            // Tarea 4: Búsqueda por año
            if (criterios.anioInicio > 0 || criterios.anioFin > 0) {
                futures.add(threadPool.submit(new BusquedaAnioTask(criterios.anioInicio, criterios.anioFin)));
            }
            
            // Recopilar resultados de todas las tareas
            List<List<Cancion>> resultadosPorTarea = new ArrayList<>();
            
            for (Future<List<Cancion>> future : futures) {
                try {
                    List<Cancion> resultado = future.get(SEARCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    if (!resultado.isEmpty()) {
                        resultadosPorTarea.add(resultado);
                    }
                } catch (TimeoutException e) {
                    System.err.println("Timeout en búsqueda paralela: " + e.getMessage());
                    future.cancel(true);
                } catch (Exception e) {
                    System.err.println("Error en búsqueda paralela: " + e.getMessage());
                }
            }
            
            // Combinar resultados según lógica AND/OR
            List<Cancion> resultadoFinal = combinarResultados(resultadosPorTarea, criterios.operadorLogico);
            
            long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
            System.out.println("Búsqueda avanzada completada en " + tiempoTranscurrido + "ms. " +
                             "Resultados: " + resultadoFinal.size());
            
            return resultadoFinal;
            
        } catch (Exception e) {
            System.err.println("Error en búsqueda avanzada: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * RF-003: Búsqueda con autocompletado de títulos usando Trie.
     * 
     * @param prefijo Prefijo del título a buscar
     * @param limite Número máximo de sugerencias
     * @return Lista de sugerencias de títulos
     */
    public List<String> autocompletarTitulos(String prefijo, int limite) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return dataManager.getSugerenciasTitulos(prefijo.trim());
    }
    
    /**
     * RF-003: Búsqueda con autocompletado de artistas usando Trie.
     * 
     * @param prefijo Prefijo del artista a buscar
     * @param limite Número máximo de sugerencias
     * @return Lista de sugerencias de artistas
     */
    public List<String> autocompletarArtistas(String prefijo, int limite) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return dataManager.getSugerenciasArtistas(prefijo.trim());
    }
    
    /**
     * RF-003: Búsqueda con autocompletado de géneros usando Trie.
     * 
     * @param prefijo Prefijo del género a buscar
     * @param limite Número máximo de sugerencias
     * @return Lista de sugerencias de géneros
     */
    public List<String> autocompletarGeneros(String prefijo, int limite) {
        if (prefijo == null || prefijo.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return dataManager.getSugerenciasGeneros(prefijo.trim());
    }
    
    /**
     * Búsqueda simple por término general.
     * 
     * @param termino Término de búsqueda
     * @return Lista de canciones que coinciden
     */
    public List<Cancion> busquedaSimple(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return dataManager.getAllCanciones();
        }
        
        return dataManager.getAllCanciones().stream()
            .filter(cancion -> cancion.coincideConBusqueda(termino))
            .sorted((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()))
            .collect(Collectors.toList());
    }
    
    /**
     * Combina los resultados de las búsquedas paralelas según la lógica especificada.
     * 
     * @param resultados Lista de listas de resultados
     * @param operador Lógica de combinación (AND u OR)
     * @return Lista combinada de canciones
     */
    private List<Cancion> combinarResultados(List<List<Cancion>> resultados, OperadorLogico operador) {
        if (resultados.isEmpty()) {
            return new ArrayList<>();
        }
        
        if (resultados.size() == 1) {
            return new ArrayList<>(resultados.get(0));
        }
        
        switch (operador) {
            case AND:
                return combinarConAND(resultados);
            case OR:
                return combinarConOR(resultados);
            default:
                return combinarConOR(resultados); // Por defecto OR
        }
    }
    
    /**
     * Combina resultados con lógica AND (intersección).
     * 
     * @param resultados Listas de resultados a combinar
     * @return Intersección de todas las listas
     */
    private List<Cancion> combinarConAND(List<List<Cancion>> resultados) {
        if (resultados.isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> idsComunes = new HashSet<>();
        Map<String, Cancion> mapaCancionesIds = new HashMap<>();
        
        // Inicializar con la primera lista
        List<Cancion> primeraLista = resultados.get(0);
        for (Cancion cancion : primeraLista) {
            idsComunes.add(cancion.getId());
            mapaCancionesIds.put(cancion.getId(), cancion);
        }
        
        // Hacer intersección con las demás listas
        for (int i = 1; i < resultados.size(); i++) {
            Set<String> idsActuales = new HashSet<>();
            for (Cancion cancion : resultados.get(i)) {
                idsActuales.add(cancion.getId());
            }
            idsComunes.retainAll(idsActuales);
        }
        
        // Convertir IDs comunes a lista de canciones
        return idsComunes.stream()
            .map(mapaCancionesIds::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * Combina resultados con lógica OR (unión).
     * 
     * @param resultados Listas de resultados a combinar
     * @return Unión de todas las listas sin duplicados
     */
    private List<Cancion> combinarConOR(List<List<Cancion>> resultados) {
        Set<String> idsUnicos = new HashSet<>();
        List<Cancion> unionResultados = new ArrayList<>();
        
        for (List<Cancion> lista : resultados) {
            for (Cancion cancion : lista) {
                if (!idsUnicos.contains(cancion.getId())) {
                    idsUnicos.add(cancion.getId());
                    unionResultados.add(cancion);
                }
            }
        }
        
        // Ordenar por popularidad
        unionResultados.sort((a, b) -> Double.compare(b.calcularPuntajePopularidad(), a.calcularPuntajePopularidad()));
        
        return unionResultados;
    }
    
    /**
     * Cierra el servicio de búsqueda y libera recursos.
     */
    public void cerrarServicio() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("SearchService cerrado correctamente");
        }
    }
    
    // Clases para tareas de búsqueda paralelas
    
    /**
     * Tarea de búsqueda por título.
     */
    private class BusquedaTituloTask implements Callable<List<Cancion>> {
        private String titulo;
        
        BusquedaTituloTask(String titulo) {
            this.titulo = titulo.toLowerCase().trim();
        }
        
        @Override
        public List<Cancion> call() {
            return dataManager.getAllCanciones().stream()
                .filter(cancion -> cancion.getTitulo().toLowerCase().contains(titulo))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Tarea de búsqueda por artista.
     */
    private class BusquedaArtistaTask implements Callable<List<Cancion>> {
        private String artista;
        
        BusquedaArtistaTask(String artista) {
            this.artista = artista.toLowerCase().trim();
        }
        
        @Override
        public List<Cancion> call() {
            return dataManager.getAllCanciones().stream()
                .filter(cancion -> 
                    cancion.getArtista().toLowerCase().contains(artista) ||
                    cancion.getArtistasColaboradores().stream()
                        .anyMatch(colaborador -> colaborador.toLowerCase().contains(artista))
                )
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Tarea de búsqueda por género.
     */
    private class BusquedaGeneroTask implements Callable<List<Cancion>> {
        private String genero;
        
        BusquedaGeneroTask(String genero) {
            this.genero = genero.toLowerCase().trim();
        }
        
        @Override
        public List<Cancion> call() {
            return dataManager.getAllCanciones().stream()
                .filter(cancion -> cancion.getGenero().toLowerCase().contains(genero))
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Tarea de búsqueda por año.
     */
    private class BusquedaAnioTask implements Callable<List<Cancion>> {
        private int anioInicio;
        private int anioFin;
        
        BusquedaAnioTask(int anioInicio, int anioFin) {
            this.anioInicio = anioInicio > 0 ? anioInicio : 1900;
            this.anioFin = anioFin > 0 ? anioFin : 2030;
        }
        
        @Override
        public List<Cancion> call() {
            return dataManager.getAllCanciones().stream()
                .filter(cancion -> cancion.getAnio() >= anioInicio && cancion.getAnio() <= anioFin)
                .collect(Collectors.toList());
        }
    }
    
    // Clases auxiliares
    
    /**
     * Clase para encapsular criterios de búsqueda avanzada.
     */
    public static class CriteriosBusqueda {
        public String titulo;
        public String artista;
        public String genero;
        public int anioInicio;
        public int anioFin;
        public OperadorLogico operadorLogico;
        
        /**
         * Constructor con criterios completos.
         */
        public CriteriosBusqueda(String titulo, String artista, String genero, 
                               int anioInicio, int anioFin, OperadorLogico operadorLogico) {
            this.titulo = titulo;
            this.artista = artista;
            this.genero = genero;
            this.anioInicio = anioInicio;
            this.anioFin = anioFin;
            this.operadorLogico = operadorLogico != null ? operadorLogico : OperadorLogico.OR;
        }
        
        /**
         * Constructor simplificado.
         */
        public CriteriosBusqueda() {
            this("", "", "", 0, 0, OperadorLogico.OR);
        }
        
        /**
         * Verifica si hay criterios de búsqueda definidos.
         * 
         * @return true si hay al menos un criterio no vacío
         */
        public boolean tieneCriterios() {
            return (titulo != null && !titulo.trim().isEmpty()) ||
                   (artista != null && !artista.trim().isEmpty()) ||
                   (genero != null && !genero.trim().isEmpty()) ||
                   anioInicio > 0 || anioFin > 0;
        }
        
        @Override
        public String toString() {
            List<String> criteriosActivos = new ArrayList<>();
            if (titulo != null && !titulo.trim().isEmpty()) criteriosActivos.add("titulo='" + titulo + "'");
            if (artista != null && !artista.trim().isEmpty()) criteriosActivos.add("artista='" + artista + "'");
            if (genero != null && !genero.trim().isEmpty()) criteriosActivos.add("genero='" + genero + "'");
            if (anioInicio > 0 || anioFin > 0) {
                criteriosActivos.add("año=" + anioInicio + "-" + anioFin);
            }
            
            return "CriteriosBusqueda{" + String.join(", ", criteriosActivos) + 
                   ", operador=" + operadorLogico + "}";
        }
    }
    
    /**
     * Enumeración para operadores lógicos en búsquedas.
     */
    public enum OperadorLogico {
        AND, OR
    }
    
    /**
     * Builder para facilitar la creación de criterios de búsqueda.
     */
    public static class CriteriosBusquedaBuilder {
        private CriteriosBusqueda criterios;
        
        public CriteriosBusquedaBuilder() {
            this.criterios = new CriteriosBusqueda();
        }
        
        public CriteriosBusquedaBuilder titulo(String titulo) {
            criterios.titulo = titulo;
            return this;
        }
        
        public CriteriosBusquedaBuilder artista(String artista) {
            criterios.artista = artista;
            return this;
        }
        
        public CriteriosBusquedaBuilder genero(String genero) {
            criterios.genero = genero;
            return this;
        }
        
        public CriteriosBusquedaBuilder anio(int anioInicio, int anioFin) {
            criterios.anioInicio = anioInicio;
            criterios.anioFin = anioFin;
            return this;
        }
        
        public CriteriosBusquedaBuilder operador(OperadorLogico operador) {
            criterios.operadorLogico = operador;
            return this;
        }
        
        public CriteriosBusqueda build() {
            return criterios;
        }
    }
    
    /**
     * Obtiene estadísticas del servicio de búsqueda.
     * 
     * @return String con estadísticas del servicio
     */
    public String getEstadisticas() {
        return String.format(
            "=== SearchService - Estadísticas ===\n" +
            "Pool de hilos: %d hilos activos\n" +
            "Timeout de búsqueda: %d ms\n" +
            "Estado del pool: %s\n" +
            "Funcionalidades: Autocompletado (Trie), Búsqueda Paralela, Lógica AND/OR",
            THREAD_POOL_SIZE,
            SEARCH_TIMEOUT_MS,
            threadPool.isShutdown() ? "Cerrado" : "Activo"
        );
    }
}