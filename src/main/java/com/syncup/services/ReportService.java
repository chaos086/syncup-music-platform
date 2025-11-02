package com.syncup.services;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes CSV del sistema.
 * RF-009: Exportación de favoritos de usuario
 * RF-013: Reportes administrativos
 */
public class ReportService {
    
    private DataManager dataManager;
    private static final String REPORTS_DIR = "reports/";
    
    public ReportService() {
        dataManager = DataManager.getInstance();
        createReportsDirectory();
    }
    
    private void createReportsDirectory() {
        File dir = new File(REPORTS_DIR);
        if (!dir.exists()) dir.mkdirs();
    }
    
    /**
     * RF-009: Exporta las canciones favoritas de un usuario a CSV.
     */
    public String exportarFavoritasUsuario(String usuarioId) {
        Usuario usuario = dataManager.getUsuarioById(usuarioId);
        if (usuario == null) return null;
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = REPORTS_DIR + "favoritos_" + usuario.getUsername() + "_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Titulo,Artista,Genero,Año,Album,Duracion");
            
            for (String cancionId : usuario.getCancionesFavoritas()) {
                Cancion cancion = dataManager.getCancionById(cancionId);
                if (cancion != null) {
                    writer.println(String.format("%s,%s,%s,%d,%s,%d",
                        escapeCsv(cancion.getTitulo()),
                        escapeCsv(cancion.getArtista()),
                        escapeCsv(cancion.getGenero()),
                        cancion.getAnio(),
                        escapeCsv(cancion.getAlbum()),
                        cancion.getDuracionSegundos()));
                }
            }
            return filename;
        } catch (IOException e) {
            System.err.println("Error exportando favoritos: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * RF-013: Genera reporte completo del catálogo.
     */
    public String exportarCatalogoCompleto() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = REPORTS_DIR + "catalogo_completo_" + timestamp + ".csv";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("ID,Titulo,Artista,Genero,Año,Album,Duracion,Reproducciones,Favoritos");
            
            for (Cancion cancion : dataManager.getAllCanciones()) {
                writer.println(String.format("%s,%s,%s,%s,%d,%s,%d,%d,%d",
                    cancion.getId(),
                    escapeCsv(cancion.getTitulo()),
                    escapeCsv(cancion.getArtista()),
                    escapeCsv(cancion.getGenero()),
                    cancion.getAnio(),
                    escapeCsv(cancion.getAlbum()),
                    cancion.getDuracionSegundos(),
                    cancion.getReproducciones(),
                    cancion.getNumeroFavoritos()));
            }
            return filename;
        } catch (IOException e) {
            System.err.println("Error exportando catálogo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Genera análisis de géneros más populares - Java 11 compatible.
     */
    public Map<String, Integer> analizarGeneros() {
        Map<String, Integer> generos = new java.util.HashMap<>();
        
        for (Cancion cancion : dataManager.getAllCanciones()) {
            String genero = cancion.getGenero();
            generos.put(genero, generos.getOrDefault(genero, 0) + 1);
        }
        
        // Java 11 compatible - usar LinkedHashMap constructor
        Map<String, Integer> resultado = new LinkedHashMap<>();
        generos.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> resultado.put(entry.getKey(), entry.getValue()));
        
        return resultado;
    }
    
    /**
     * Analiza los artistas más populares por número de canciones - Java 11 compatible.
     */
    public Map<String, Integer> analizarArtistasPopulares(int limite) {
        Map<String, Integer> artistas = new java.util.HashMap<>();
        
        for (Cancion cancion : dataManager.getAllCanciones()) {
            String artista = cancion.getArtista();
            artistas.put(artista, artistas.getOrDefault(artista, 0) + 1);
        }
        
        // Java 11 compatible - usar LinkedHashMap constructor  
        Map<String, Integer> resultado = new LinkedHashMap<>();
        artistas.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limite)
            .forEach(entry -> resultado.put(entry.getKey(), entry.getValue()));
        
        return resultado;
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}