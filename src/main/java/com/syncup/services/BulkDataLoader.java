package com.syncup.services;

import com.syncup.models.Usuario;
import com.syncup.models.Cancion;
import com.syncup.data.DataManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Servicio para carga masiva de canciones desde archivos de texto.
 * RF-012: Carga masiva de canciones desde archivo .txt o .tsv
 */
public class BulkDataLoader {
    
    private DataManager dataManager;
    private static final String DEFAULT_SONGS_FILE = "bulk-data/sample-songs.txt";
    
    public BulkDataLoader() {
        dataManager = DataManager.getInstance();
        crearArchivoEjemplo();
    }
    
    /**
     * Valida el formato de un archivo para carga masiva.
     */
    public ResultadoValidacion validarArchivoFormato(String rutaArchivo) {
        try {
            List<String> lineas = Files.readAllLines(Paths.get(rutaArchivo));
            if (lineas.isEmpty()) {
                return new ResultadoValidacion(false, "Archivo vacío");
            }
            
            int lineasValidas = 0;
            List<String> errores = new ArrayList<>();
            
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;
                
                String[] campos = linea.split("\t");
                if (campos.length < 4) {
                    errores.add("Línea " + (i+1) + ": Faltan campos (mínimo 4)");
                    continue;
                }
                
                try {
                    Integer.parseInt(campos[3].trim()); // Año debe ser número
                    lineasValidas++;
                } catch (NumberFormatException e) {
                    errores.add("Línea " + (i+1) + ": Año inválido");
                }
            }
            
            if (lineasValidas == 0) {
                return new ResultadoValidacion(false, "No se encontraron líneas válidas");
            }
            
            String mensaje = "Archivo válido: " + lineasValidas + " líneas procesables";
            if (!errores.isEmpty()) {
                mensaje += "\nErrores encontrados: " + errores.size();
            }
            
            return new ResultadoValidacion(true, mensaje);
            
        } catch (IOException e) {
            return new ResultadoValidacion(false, "Error leyendo archivo: " + e.getMessage());
        }
    }
    
    /**
     * RF-012: Realiza carga masiva de canciones desde archivo.
     */
    public ResultadoCargaMasiva cargarCancionesMasivas(String rutaArchivo, String adminId) {
        // Verificar permisos de administrador
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado: usuario no es administrador para carga masiva.");
            return new ResultadoCargaMasiva(false, "Acceso denegado", 0, 0);
        }
        
        // Si no se especifica archivo, usar archivo de ejemplo
        String archivo = (rutaArchivo == null || rutaArchivo.trim().isEmpty()) ? 
                        DEFAULT_SONGS_FILE : rutaArchivo;
        
        try {
            List<String> lineas = Files.readAllLines(Paths.get(archivo));
            
            int lineasProcesadas = 0;
            int cancionesAgregadas = 0;
            List<String> errores = new ArrayList<>();
            
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;
                
                try {
                    Cancion cancion = parsearLineaCancion(linea);
                    if (cancion != null) {
                        boolean agregada = dataManager.addCancion(cancion);
                        if (agregada) {
                            cancionesAgregadas++;
                        }
                    }
                    lineasProcesadas++;
                    
                } catch (Exception e) {
                    errores.add("Línea " + (i+1) + ": " + e.getMessage());
                }
            }
            
            String mensaje = String.format("Carga completada: %d canciones agregadas de %d líneas procesadas",
                                          cancionesAgregadas, lineasProcesadas);
            
            if (!errores.isEmpty()) {
                mensaje += "\nErrores: " + errores.size();
            }
            
            return new ResultadoCargaMasiva(true, mensaje, lineasProcesadas, cancionesAgregadas);
            
        } catch (IOException e) {
            return new ResultadoCargaMasiva(false, "Error leyendo archivo: " + e.getMessage(), 0, 0);
        }
    }
    
    /**
     * Parsea una línea del archivo y crea una canción.
     * Formato esperado: titulo\tartista\talbum\tgenero\taño[\tduracion]
     */
    private Cancion parsearLineaCancion(String linea) throws Exception {
        String[] campos = linea.split("\t");
        
        if (campos.length < 4) {
            throw new Exception("Formato incorrecto, mínimo 4 campos requeridos");
        }
        
        String titulo = campos[0].trim();
        String artista = campos[1].trim();
        String album = campos.length > 2 ? campos[2].trim() : "";
        String genero = campos[3].trim();
        
        if (titulo.isEmpty() || artista.isEmpty() || genero.isEmpty()) {
            throw new Exception("Campos obligatorios vacíos");
        }
        
        int anio = 2024; // Año por defecto
        if (campos.length > 4 && !campos[4].trim().isEmpty()) {
            try {
                anio = Integer.parseInt(campos[4].trim());
            } catch (NumberFormatException e) {
                throw new Exception("Año inválido: " + campos[4]);
            }
        }
        
        Cancion cancion = new Cancion(titulo, artista, genero, anio);
        cancion.setAlbum(album);
        
        // Duración opcional
        if (campos.length > 5 && !campos[5].trim().isEmpty()) {
            try {
                int duracion = Integer.parseInt(campos[5].trim());
                cancion.setDuracionSegundos(duracion);
            } catch (NumberFormatException e) {
                // Ignorar duración inválida, usar por defecto
            }
        }
        
        return cancion;
    }
    
    /**
     * Crea un archivo de ejemplo para carga masiva.
     */
    private void crearArchivoEjemplo() {
        File bulkDataDir = new File("bulk-data");
        if (!bulkDataDir.exists()) {
            bulkDataDir.mkdirs();
        }
        
        File archivoEjemplo = new File(DEFAULT_SONGS_FILE);
        if (!archivoEjemplo.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(archivoEjemplo))) {
                writer.println("# Ejemplo de archivo para carga masiva");
                writer.println("# Formato: titulo\tartista\talbum\tgenero\taño\tduracion_segundos");
                writer.println("Stairway to Heaven\tLed Zeppelin\tLed Zeppelin IV\tRock\t1971\t482");
                writer.println("Thriller\tMichael Jackson\tThriller\tPop\t1982\t357");
                writer.println("Like a Rolling Stone\tBob Dylan\tHighway 61 Revisited\tFolk Rock\t1965\t369");
                writer.println("Purple Haze\tJimi Hendrix\tAre You Experienced\tRock\t1967\t170");
                writer.println("Good Vibrations\tThe Beach Boys\tSmiley Smile\tPop\t1966\t218");
                
                System.out.println("Archivo de ejemplo creado: " + DEFAULT_SONGS_FILE);
            } catch (IOException e) {
                System.err.println("Error creando archivo de ejemplo: " + e.getMessage());
            }
        }
    }
    
    /**
     * Resultado de validación de archivo.
     */
    public static class ResultadoValidacion {
        public final boolean valido;
        public final String mensaje;
        
        public ResultadoValidacion(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }
    }
    
    /**
     * Resultado de carga masiva.
     */
    public static class ResultadoCargaMasiva {
        public final boolean exito;
        public final String mensaje;
        public final int lineasProcesadas;
        public final int cancionesAgregadas;
        
        public ResultadoCargaMasiva(boolean exito, String mensaje, int lineasProcesadas, int cancionesAgregadas) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.lineasProcesadas = lineasProcesadas;
            this.cancionesAgregadas = cancionesAgregadas;
        }
    }
}