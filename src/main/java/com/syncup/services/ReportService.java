package com.syncup.services;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.models.Admin;
import com.syncup.data.DataManager;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes y exportación de datos en formato CSV.
 * RF-009: Exportación de reportes CSV para usuarios.
 * RF-029: Generador de reportes completo para administradores.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class ReportService {
    
    /** Gestor de datos del sistema */
    private DataManager dataManager;
    
    /** Directorio base para reportes */
    private static final String REPORTS_DIR = "reports/";
    
    /** Formato de fecha para nombres de archivos */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    /**
     * Constructor del servicio de reportes.
     */
    public ReportService() {
        this.dataManager = DataManager.getInstance();
        crearDirectorioReportes();
        
        System.out.println("ReportService inicializado. Directorio: " + REPORTS_DIR);
    }
    
    /**
     * Crea el directorio de reportes si no existe.
     */
    private void crearDirectorioReportes() {
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            boolean created = reportsDir.mkdirs();
            if (created) {
                System.out.println("Directorio de reportes creado: " + REPORTS_DIR);
            } else {
                System.err.println("No se pudo crear el directorio de reportes: " + REPORTS_DIR);
            }
        }
    }
    
    /**
     * RF-009: Exporta las canciones favoritas de un usuario a CSV.
     * 
     * @param usuarioId ID del usuario
     * @return Ruta del archivo generado, null si hay error
     */
    public String exportarFavoritasUsuario(String usuarioId) {
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) {
            System.err.println("Usuario no encontrado: " + usuarioId);
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORTS_DIR + "favoritas_" + usuario.getUsername() + "_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Escribir encabezados CSV
            writer.println("Titulo,Artista,Album,Genero,Anio,Duracion,Reproducciones,Favoritos,Calificacion");
            
            // Escribir datos de canciones favoritas
            for (String cancionId : usuario.getCancionesFavoritas()) {
                Cancion cancion = dataManager.getCancionById(cancionId);
                if (cancion != null) {
                    writer.println(formatearCancionCSV(cancion));
                }
            }
            
            System.out.println("Reporte de favoritas exportado: " + filename);
            return filename;
            
        } catch (IOException e) {
            System.err.println("Error exportando favoritas: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * RF-029: Exporta reporte completo del catálogo de canciones (solo admin).
     * 
     * @param adminId ID del administrador que solicita el reporte
     * @return Ruta del archivo generado, null si hay error o no tiene permisos
     */
    public String exportarCatalogoCompleto(String adminId) {
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado. Solo administradores pueden generar este reporte.");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORTS_DIR + "catalogo_completo_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Escribir encabezados CSV
            writer.println("ID,Titulo,Artista,Album,Genero,Anio,Duracion_Segundos,Duracion_Formateada," +
                          "Reproducciones,Favoritos,Calificacion_Promedio,Num_Calificaciones," +
                          "Disponible,Contenido_Explicito,Fecha_Agregado,Artistas_Colaboradores");
            
            // Escribir todas las canciones
            List<Cancion> todasLasCanciones = dataManager.getAllCanciones();
            for (Cancion cancion : todasLasCanciones) {
                writer.println(formatearCancionCompletaCSV(cancion));
            }
            
            // Registrar acción si es Admin
            if (usuario instanceof Admin) {
                ((Admin) usuario).registrarAccion(
                    Admin.TipoAccionAdmin.GENERAR_REPORTE,
                    "Reporte completo del catálogo",
                    filename
                );
            }
            
            System.out.println("Reporte completo del catálogo exportado: " + filename);
            return filename;
            
        } catch (IOException e) {
            System.err.println("Error exportando catálogo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * RF-029: Exporta reporte de usuarios del sistema (solo admin).
     * 
     * @param adminId ID del administrador
     * @return Ruta del archivo generado
     */
    public String exportarReporteUsuarios(String adminId) {
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado. Solo administradores pueden generar este reporte.");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORTS_DIR + "usuarios_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Escribir encabezados CSV
            writer.println("ID,Username,Nombre_Completo,Email,Fecha_Registro,Activo,Es_Admin," +
                          "Num_Favoritas,Num_Seguidos,Num_Seguidores,Num_Playlists,Generos_Favoritos");
            
            // Escribir datos de usuarios
            List<Usuario> todosUsuarios = dataManager.getAllUsuarios();
            for (Usuario usr : todosUsuarios) {
                writer.println(formatearUsuarioCSV(usr));
            }
            
            // Registrar acción si es Admin
            if (usuario instanceof Admin) {
                ((Admin) usuario).registrarAccion(
                    Admin.TipoAccionAdmin.GENERAR_REPORTE,
                    "Reporte de usuarios del sistema",
                    filename
                );
            }
            
            System.out.println("Reporte de usuarios exportado: " + filename);
            return filename;
            
        } catch (IOException e) {
            System.err.println("Error exportando usuarios: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * RF-029: Genera reporte de estadísticas del sistema (solo admin).
     * 
     * @param adminId ID del administrador
     * @return Ruta del archivo generado
     */
    public String generarReporteEstadisticas(String adminId) {
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado. Solo administradores pueden generar este reporte.");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORTS_DIR + "estadisticas_sistema_" + timestamp + ".txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("=== REPORTE DE ESTADISTICAS DEL SISTEMA SYNCUP ===");
            writer.println("Fecha de generación: " + LocalDateTime.now());
            writer.println("Generado por: " + usuario.getUsername());
            writer.println();
            
            // Estadísticas generales
            writer.println(dataManager.getSystemStats());
            writer.println();
            
            // Análisis de géneros
            writer.println("=== ANÁLISIS DE GÉNEROS ===");
            Map<String, Integer> generos = analizarGeneros();
            for (Map.Entry<String, Integer> entrada : generos.entrySet()) {
                writer.println(entrada.getKey() + ": " + entrada.getValue() + " canciones");
            }
            writer.println();
            
            // Análisis de artistas
            writer.println("=== ARTISTAS MÁS POPULARES ===");
            Map<String, Integer> artistas = analizarArtistasPopulares(10);
            for (Map.Entry<String, Integer> entrada : artistas.entrySet()) {
                writer.println(entrada.getKey() + ": " + entrada.getValue() + " canciones");
            }
            writer.println();
            
            // Estadísticas de usuarios
            writer.println("=== ESTADÍSTICAS DE USUARIOS ===");
            writer.println("Total usuarios: " + dataManager.getAllUsuarios().size());
            writer.println("Usuarios administradores: " + 
                dataManager.getAllUsuarios().stream().mapToLong(u -> u.isEsAdmin() ? 1 : 0).sum());
            writer.println("Promedio de favoritas por usuario: " + calcularPromedioFavoritas());
            
            // Registrar acción si es Admin
            if (usuario instanceof Admin) {
                ((Admin) usuario).registrarAccion(
                    Admin.TipoAccionAdmin.GENERAR_REPORTE,
                    "Reporte de estadísticas del sistema",
                    filename
                );
            }
            
            System.out.println("Reporte de estadísticas generado: " + filename);
            return filename;
            
        } catch (IOException e) {
            System.err.println("Error generando reporte de estadísticas: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera reporte personalizado con filtros específicos.
     * 
     * @param adminId ID del administrador
     * @param filtros Filtros a aplicar
     * @param nombreReporte Nombre personalizado del reporte
     * @return Ruta del archivo generado
     */
    public String generarReportePersonalizado(String adminId, FiltrosReporte filtros, String nombreReporte) {
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado. Solo administradores pueden generar reportes personalizados.");
            return null;
        }
        
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String filename = REPORTS_DIR + nombreReporte + "_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<Cancion> cancionesFiltradas = aplicarFiltros(dataManager.getAllCanciones(), filtros);
            
            // Escribir encabezados
            writer.println("Titulo,Artista,Album,Genero,Anio,Duracion,Reproducciones,Favoritos,Calificacion");
            
            // Escribir canciones filtradas
            for (Cancion cancion : cancionesFiltradas) {
                writer.println(formatearCancionCSV(cancion));
            }
            
            // Registrar acción si es Admin
            if (usuario instanceof Admin) {
                ((Admin) usuario).registrarAccion(
                    Admin.TipoAccionAdmin.GENERAR_REPORTE,
                    "Reporte personalizado: " + nombreReporte,
                    filename
                );
            }
            
            System.out.println("Reporte personalizado generado: " + filename);
            return filename;
            
        } catch (IOException e) {
            System.err.println("Error generando reporte personalizado: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Aplica filtros a una lista de canciones.
     * 
     * @param canciones Lista de canciones original
     * @param filtros Filtros a aplicar
     * @return Lista de canciones filtradas
     */
    private List<Cancion> aplicarFiltros(List<Cancion> canciones, FiltrosReporte filtros) {
        return canciones.stream()
            .filter(cancion -> {
                // Filtro por género
                if (filtros.genero != null && !filtros.genero.trim().isEmpty()) {
                    if (!cancion.getGenero().toLowerCase().contains(filtros.genero.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro por año
                if (filtros.anioInicio > 0 && cancion.getAnio() < filtros.anioInicio) {
                    return false;
                }
                if (filtros.anioFin > 0 && cancion.getAnio() > filtros.anioFin) {
                    return false;
                }
                
                // Filtro por mínimo de reproducciones
                if (filtros.minimoReproducciones > 0 && cancion.getReproducciones() < filtros.minimoReproducciones) {
                    return false;
                }
                
                // Filtro por calificación mínima
                if (filtros.calificacionMinima > 0 && cancion.getCalificacionPromedio() < filtros.calificacionMinima) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Analiza la distribución de géneros en el catálogo.
     * 
     * @return Mapa con géneros y su frecuencia
     */
    private Map<String, Integer> analizarGeneros() {
        Map<String, Integer> generos = new HashMap<>();
        
        for (Cancion cancion : dataManager.getAllCanciones()) {
            String genero = cancion.getGenero();
            generos.put(genero, generos.getOrDefault(genero, 0) + 1);
        }
        
        // Ordenar por frecuencia descendente
        return generos.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toLinkedHashMap());
    }
    
    /**
     * Analiza los artistas más populares por número de canciones.
     * 
     * @param limite Número máximo de artistas a incluir
     * @return Mapa con artistas y su frecuencia
     */
    private Map<String, Integer> analizarArtistasPopulares(int limite) {
        Map<String, Integer> artistas = new HashMap<>();
        
        for (Cancion cancion : dataManager.getAllCanciones()) {
            String artista = cancion.getArtista();
            artistas.put(artista, artistas.getOrDefault(artista, 0) + 1);
        }
        
        // Ordenar y limitar
        return artistas.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limite)
            .collect(Collectors.toLinkedHashMap());
    }
    
    /**
     * Calcula el promedio de canciones favoritas por usuario.
     * 
     * @return Promedio de favoritas
     */
    private double calcularPromedioFavoritas() {
        List<Usuario> usuarios = dataManager.getAllUsuarios();
        if (usuarios.isEmpty()) {
            return 0.0;
        }
        
        double totalFavoritas = usuarios.stream()
            .mapToInt(Usuario::getNumeroCancionesFavoritas)
            .sum();
        
        return totalFavoritas / usuarios.size();
    }
    
    /**
     * Formatea una canción para exportación CSV básica.
     * 
     * @param cancion Canción a formatear
     * @return Línea CSV formateada
     */
    private String formatearCancionCSV(Cancion cancion) {
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\",%d,%d,%.2f",
            escaparCSV(cancion.getTitulo()),
            escaparCSV(cancion.getArtista()),
            escaparCSV(cancion.getAlbum()),
            escaparCSV(cancion.getGenero()),
            cancion.getAnio(),
            cancion.getDuracionFormateada(),
            cancion.getReproducciones(),
            cancion.getNumeroFavoritos(),
            cancion.getCalificacionPromedio()
        );
    }
    
    /**
     * Formatea una canción para exportación CSV completa.
     * 
     * @param cancion Canción a formatear
     * @return Línea CSV formateada
     */
    private String formatearCancionCompletaCSV(Cancion cancion) {
        String colaboradores = cancion.getArtistasColaboradores().isEmpty() ? 
            "" : String.join(";", cancion.getArtistasColaboradores());
        
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",%d,%d,%.2f,%d,%s,%s,\"%s\",\"%s\"",
            escaparCSV(cancion.getId()),
            escaparCSV(cancion.getTitulo()),
            escaparCSV(cancion.getArtista()),
            escaparCSV(cancion.getAlbum()),
            escaparCSV(cancion.getGenero()),
            cancion.getAnio(),
            cancion.getDuracionSegundos(),
            cancion.getDuracionFormateada(),
            cancion.getReproducciones(),
            cancion.getNumeroFavoritos(),
            cancion.getCalificacionPromedio(),
            cancion.getNumeroCalificaciones(),
            cancion.isDisponible(),
            cancion.isContenidoExplicito(),
            cancion.getFechaAgregado().toString(),
            escaparCSV(colaboradores)
        );
    }
    
    /**
     * Formatea un usuario para exportación CSV.
     * 
     * @param usuario Usuario a formatear
     * @return Línea CSV formateada
     */
    private String formatearUsuarioCSV(Usuario usuario) {
        String generosFavoritos = usuario.getGenerosFavoritos().isEmpty() ? 
            "" : String.join(";", usuario.getGenerosFavoritos());
        
        return String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s,%s,%d,%d,%d,%d,\"%s\"",
            escaparCSV(usuario.getId()),
            escaparCSV(usuario.getUsername()),
            escaparCSV(usuario.getNombreCompleto()),
            escaparCSV(usuario.getEmail()),
            usuario.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            usuario.isActivo(),
            usuario.isEsAdmin(),
            usuario.getNumeroCancionesFavoritas(),
            usuario.getNumeroSeguidos(),
            usuario.getNumeroSeguidores(),
            usuario.getPlaylists().size(),
            escaparCSV(generosFavoritos)
        );
    }
    
    /**
     * Escapa caracteres especiales para formato CSV.
     * 
     * @param texto Texto a escapar
     * @return Texto escapado
     */
    private String escaparCSV(String texto) {
        if (texto == null) {
            return "";
        }
        
        // Escapar comillas dobles y comas
        return texto.replace("\"", "\\\"").replace(",", "\\,");
    }
    
    /**
     * Obtiene la lista de archivos de reportes generados.
     * 
     * @return Lista de nombres de archivos de reportes
     */
    public List<String> listarReportesGenerados() {
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists() || !reportsDir.isDirectory()) {
            return new ArrayList<>();
        }
        
        File[] archivos = reportsDir.listFiles();
        if (archivos == null) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(archivos)
            .filter(File::isFile)
            .map(File::getName)
            .sorted(Collections.reverseOrder()) // Más recientes primero
            .collect(Collectors.toList());
    }
    
    /**
     * Elimina reportes antiguos (más de 30 días).
     * 
     * @return Número de archivos eliminados
     */
    public int limpiarReportesAntiguos() {
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            return 0;
        }
        
        File[] archivos = reportsDir.listFiles();
        if (archivos == null) {
            return 0;
        }
        
        long tiempoLimite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 días
        int archivosEliminados = 0;
        
        for (File archivo : archivos) {
            if (archivo.isFile() && archivo.lastModified() < tiempoLimite) {
                if (archivo.delete()) {
                    archivosEliminados++;
                }
            }
        }
        
        System.out.println("Reportes antiguos eliminados: " + archivosEliminados);
        return archivosEliminados;
    }
    
    /**
     * Clase para filtros de reporte personalizado.
     */
    public static class FiltrosReporte {
        public String genero;
        public int anioInicio;
        public int anioFin;
        public long minimoReproducciones;
        public double calificacionMinima;
        
        public FiltrosReporte() {
            this.genero = "";
            this.anioInicio = 0;
            this.anioFin = 0;
            this.minimoReproducciones = 0;
            this.calificacionMinima = 0.0;
        }
    }
}