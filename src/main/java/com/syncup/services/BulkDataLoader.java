package com.syncup.services;

import com.syncup.models.Cancion;
import com.syncup.models.Admin;
import com.syncup.data.DataManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para carga masiva de canciones desde archivo de texto plano.
 * RF-012: Funcionalidad de carga masiva de canciones para administradores.
 * Soporta el formato especificado en el PDF del proyecto.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class BulkDataLoader {
    
    /** Gestor de datos del sistema */
    private DataManager dataManager;
    
    /** Directorio base para archivos de carga masiva */
    private static final String BULK_DATA_DIR = "bulk-data/";
    
    /** Archivo por defecto de canciones para carga masiva */
    private static final String DEFAULT_SONGS_FILE = BULK_DATA_DIR + "canciones_masivas.txt";
    
    /** Separador esperado en el archivo de texto */
    private static final String FIELD_SEPARATOR = "\t"; // Tab-separated
    
    /** Número de campos esperados por línea */
    private static final int EXPECTED_FIELDS = 6; // titulo, artista, album, genero, anio, duracion
    
    /**
     * Constructor del servicio de carga masiva.
     */
    public BulkDataLoader() {
        this.dataManager = DataManager.getInstance();
        crearDirectorioCargaMasiva();
        
        System.out.println("BulkDataLoader inicializado. Directorio: " + BULK_DATA_DIR);
    }
    
    /**
     * Crea el directorio de carga masiva si no existe.
     */
    private void crearDirectorioCargaMasiva() {
        File bulkDir = new File(BULK_DATA_DIR);
        if (!bulkDir.exists()) {
            boolean created = bulkDir.mkdirs();
            if (created) {
                System.out.println("Directorio de carga masiva creado: " + BULK_DATA_DIR);
                crearArchivoEjemplo();
            } else {
                System.err.println("No se pudo crear el directorio: " + BULK_DATA_DIR);
            }
        }
    }
    
    /**
     * Crea un archivo de ejemplo con el formato esperado.
     */
    private void crearArchivoEjemplo() {
        File ejemploFile = new File(BULK_DATA_DIR + "ejemplo_formato.txt");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ejemploFile))) {
            writer.println("# Archivo de ejemplo para carga masiva de canciones");
            writer.println("# Formato: titulo\tartista\talbum\tgenero\tanio\tduracion_segundos");
            writer.println("# Líneas que empiecen con # son comentarios y serán ignoradas");
            writer.println();
            writer.println("Stairway to Heaven\tLed Zeppelin\tLed Zeppelin IV\tRock\t1971\t482");
            writer.println("Bohemian Rhapsody\tQueen\tA Night at the Opera\tRock\t1975\t355");
            writer.println("Hotel California\tEagles\tHotel California\tRock\t1976\t391");
            writer.println("Imagine\tJohn Lennon\tImagine\tRock\t1971\t183");
            writer.println("Smells Like Teen Spirit\tNirvana\tNevermind\tGrunge\t1991\t301");
            writer.println("Billie Jean\tMichael Jackson\tThriller\tPop\t1983\t294");
            writer.println("Like a Rolling Stone\tBob Dylan\tHighway 61 Revisited\tFolk Rock\t1965\t369");
            writer.println("Hey Jude\tThe Beatles\tThe Beatles 1967-1970\tPop Rock\t1968\t431");
            writer.println("Purple Haze\tJimi Hendrix\tAre You Experienced\tPsychedelic Rock\t1967\t169");
            writer.println("Good Vibrations\tThe Beach Boys\tSmiley Smile\tPop\t1966\t217");
            
            System.out.println("Archivo de ejemplo creado: " + ejemploFile.getName());
            
        } catch (IOException e) {
            System.err.println("Error creando archivo de ejemplo: " + e.getMessage());
        }
    }
    
    /**
     * RF-012: Realiza carga masiva de canciones desde archivo de texto plano.
     * 
     * @param rutaArchivo Ruta del archivo a cargar
     * @param adminId ID del administrador que realiza la carga
     * @return Resultado de la carga masiva
     */
    public ResultadoCargaMasiva cargarCancionesMasivas(String rutaArchivo, String adminId) {
        // Verificar permisos de administrador
        Usuario usuario = dataManager.getUsuarioById(adminId);
        if (usuario == null || !usuario.isEsAdmin()) {
            System.err.println("Acceso denegado. Solo administradores pueden realizar carga masiva.");
            return new ResultadoCargaMasiva(false, "Acceso denegado", 0, 0, new ArrayList<>());
        }
        
        // Si no se especifica archivo, usar el por defecto
        String archivo = (rutaArchivo != null && !rutaArchivo.trim().isEmpty()) ? 
                        rutaArchivo : DEFAULT_SONGS_FILE;
        
        System.out.println("Iniciando carga masiva desde: " + archivo);
        
        File file = new File(archivo);
        if (!file.exists()) {
            String mensaje = "Archivo no encontrado: " + archivo;
            System.err.println(mensaje);
            return new ResultadoCargaMasiva(false, mensaje, 0, 0, new ArrayList<>());
        }
        
        AtomicInteger lineasProcesadas = new AtomicInteger(0);
        AtomicInteger cancionesAgregadas = new AtomicInteger(0);
        AtomicInteger errores = new AtomicInteger(0);
        List<String> mensajesError = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            int numeroLinea = 0;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                lineasProcesadas.incrementAndGet();
                
                // Ignorar líneas de comentario y vacías
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }
                
                try {
                    Cancion cancion = parsearLineaCancion(linea);
                    if (cancion != null) {
                        boolean agregada = dataManager.addCancion(cancion);
                        if (agregada) {
                            cancionesAgregadas.incrementAndGet();
                        } else {
                            mensajesError.add("Línea " + numeroLinea + ": Canción duplicada (" + cancion.getTitulo() + ")");
                        }
                    } else {
                        errores.incrementAndGet();
                        mensajesError.add("Línea " + numeroLinea + ": Formato inválido");
                    }
                } catch (Exception e) {
                    errores.incrementAndGet();
                    mensajesError.add("Línea " + numeroLinea + ": Error procesando - " + e.getMessage());
                }
            }
            
            // Registrar acción administrativa
            if (usuario instanceof Admin) {
                ((Admin) usuario).registrarAccion(
                    Admin.TipoAccionAdmin.CARGA_MASIVA,
                    String.format("Cargadas %d canciones de %d líneas", 
                                cancionesAgregadas.get(), lineasProcesadas.get()),
                    archivo
                );
            }
            
            // Actualizar índices de búsqueda después de la carga
            if (cancionesAgregadas.get() > 0) {
                System.out.println("Actualizando índices de búsqueda...");
                // El DataManager se actualizará automáticamente al agregar canciones
            }
            
            boolean exito = errores.get() == 0;
            String mensaje = String.format("Carga masiva completada. Procesadas: %d líneas, " +
                                         "Agregadas: %d canciones, Errores: %d",
                                         lineasProcesadas.get(), cancionesAgregadas.get(), errores.get());
            
            System.out.println(mensaje);
            
            return new ResultadoCargaMasiva(exito, mensaje, lineasProcesadas.get(), 
                                          cancionesAgregadas.get(), mensajesError);
            
        } catch (IOException e) {
            String mensajeError = "Error leyendo archivo: " + e.getMessage();
            System.err.println(mensajeError);
            return new ResultadoCargaMasiva(false, mensajeError, 0, 0, 
                                          Arrays.asList(mensajeError));
        }
    }
    
    /**
     * Parsea una línea del archivo y crea una instancia de Cancion.
     * Formato esperado: titulo\tartista\talbum\tgenero\tanio\tduracion_segundos
     * 
     * @param linea Línea del archivo a parsear
     * @return Instancia de Cancion o null si hay error
     */
    private Cancion parsearLineaCancion(String linea) {
        if (linea == null || linea.trim().isEmpty()) {
            return null;
        }
        
        String[] campos = linea.split(FIELD_SEPARATOR);
        if (campos.length < EXPECTED_FIELDS) {
            System.err.println("Línea con formato incorrecto (esperados " + EXPECTED_FIELDS + 
                             " campos): " + linea);
            return null;
        }
        
        try {
            String titulo = campos[0].trim();
            String artista = campos[1].trim();
            String album = campos[2].trim();
            String genero = campos[3].trim();
            int anio = Integer.parseInt(campos[4].trim());
            int duracionSegundos = Integer.parseInt(campos[5].trim());
            
            // Validaciones básicas
            if (titulo.isEmpty() || artista.isEmpty()) {
                System.err.println("Título y artista son obligatorios: " + linea);
                return null;
            }
            
            if (anio < 1900 || anio > 2030) {
                System.err.println("Año fuera de rango válido (1900-2030): " + anio);
                return null;
            }
            
            if (duracionSegundos <= 0 || duracionSegundos > 3600) { // Max 1 hora
                System.err.println("Duración inválida (debe estar entre 1-3600 segundos): " + duracionSegundos);
                return null;
            }
            
            // Crear canción
            Cancion cancion = new Cancion(titulo, artista, genero, anio);
            cancion.setAlbum(album);
            cancion.setDuracionSegundos(duracionSegundos);
            
            // Inicializar con datos aleatorios para simular métricas
            cancion.setReproducciones((long) (Math.random() * 10000));
            cancion.setNumeroFavoritos((long) (Math.random() * 1000));
            cancion.agregarCalificacion((int) (Math.random() * 5) + 1);
            
            return cancion;
            
        } catch (NumberFormatException e) {
            System.err.println("Error parseando números en línea: " + linea + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error inesperado parseando línea: " + linea + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Crea un archivo de canciones de muestra para pruebas de carga masiva.
     * 
     * @return Ruta del archivo creado
     */
    public String crearArchivoMuestraCargaMasiva() {
        File archivoMuestra = new File(DEFAULT_SONGS_FILE);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(archivoMuestra))) {
            writer.println("# Archivo de muestra para carga masiva de canciones");
            writer.println("# Formato: titulo\tartista\talbum\tgenero\tanio\tduracion_segundos");
            writer.println("# Este archivo contiene 50 canciones de ejemplo");
            writer.println();
            
            // Canciones de muestra para carga masiva
            String[][] cancionesMuestra = {
                {"Thriller", "Michael Jackson", "Thriller", "Pop", "1982", "358"},
                {"Back in Black", "AC/DC", "Back in Black", "Hard Rock", "1980", "255"},
                {"The Dark Side of the Moon", "Pink Floyd", "The Dark Side of the Moon", "Progressive Rock", "1973", "2580"},
                {"Their Greatest Hits", "Eagles", "Their Greatest Hits (1971-1975)", "Rock", "1976", "2640"},
                {"Saturday Night Fever", "Bee Gees", "Saturday Night Fever", "Disco", "1977", "2280"},
                {"Rumours", "Fleetwood Mac", "Rumours", "Rock", "1977", "2379"},
                {"Come On Over", "Shania Twain", "Come On Over", "Country Pop", "1997", "4080"},
                {"The Bodyguard", "Whitney Houston", "The Bodyguard Soundtrack", "R&B", "1992", "3420"},
                {"Bat Out of Hell", "Meat Loaf", "Bat Out of Hell", "Hard Rock", "1977", "2760"},
                {"Dirty Dancing", "Various Artists", "Dirty Dancing Soundtrack", "Pop", "1987", "2880"},
                {"Let's Talk About Love", "Celine Dion", "Let's Talk About Love", "Pop", "1997", "4200"},
                {"The Wall", "Pink Floyd", "The Wall", "Progressive Rock", "1979", "4860"},
                {"Double Live", "Garth Brooks", "Double Live", "Country", "1998", "7200"},
                {"Supernatural", "Santana", "Supernatural", "Latin Rock", "1999", "4440"},
                {"Millennium", "Backstreet Boys", "Millennium", "Pop", "1999", "3600"},
                {"The Eminem Show", "Eminem", "The Eminem Show", "Hip Hop", "2002", "4560"},
                {"Marshall Mathers LP", "Eminem", "The Marshall Mathers LP", "Hip Hop", "2000", "4320"},
                {"...Baby One More Time", "Britney Spears", "...Baby One More Time", "Pop", "1999", "2520"},
                {"Hybrid Theory", "Linkin Park", "Hybrid Theory", "Nu Metal", "2000", "2340"},
                {"Meteora", "Linkin Park", "Meteora", "Nu Metal", "2003", "2160"},
                {"American Idiot", "Green Day", "American Idiot", "Punk Rock", "2004", "3420"},
                {"Confessions on a Dance Floor", "Madonna", "Confessions on a Dance Floor", "Dance Pop", "2005", "3480"},
                {"X&Y", "Coldplay", "X&Y", "Alternative Rock", "2005", "3840"},
                {"Stadium Arcadium", "Red Hot Chili Peppers", "Stadium Arcadium", "Alternative Rock", "2006", "7680"},
                {"Minutes to Midnight", "Linkin Park", "Minutes to Midnight", "Alternative Rock", "2007", "2580"},
                {"21", "Adele", "21", "Soul", "2011", "2880"},
                {"25", "Adele", "25", "Soul", "2015", "2760"},
                {"Purpose", "Justin Bieber", "Purpose", "Pop", "2015", "2820"},
                {"÷ (Divide)", "Ed Sheeran", "÷ (Divide)", "Pop", "2017", "3540"},
                {"Scorpion", "Drake", "Scorpion", "Hip Hop", "2018", "5400"},
                {"folklore", "Taylor Swift", "folklore", "Indie Folk", "2020", "3840"},
                {"evermore", "Taylor Swift", "evermore", "Alternative", "2020", "3660"},
                {"Sour", "Olivia Rodrigo", "Sour", "Pop", "2021", "2100"},
                {"Planet Her", "Doja Cat", "Planet Her", "Pop Rap", "2021", "2640"},
                {"Dawn FM", "The Weeknd", "Dawn FM", "Synthpop", "2022", "3120"},
                {"Harry's House", "Harry Styles", "Harry's House", "Pop Rock", "2022", "2580"},
                {"Renaissance", "Beyoncé", "Renaissance", "Dance", "2022", "3720"},
                {"Bad Bunny Un Verano Sin Ti", "Bad Bunny", "Un Verano Sin Ti", "Reggaeton", "2022", "6900"},
                {"Midnights", "Taylor Swift", "Midnights", "Synth Pop", "2022", "2640"},
                {"SOS", "SZA", "SOS", "R&B", "2022", "4080"},
                {"The Car", "Arctic Monkeys", "The Car", "Indie Rock", "2022", "2220"},
                {"Music of the Spheres", "Coldplay", "Music of the Spheres", "Pop Rock", "2021", "2520"},
                {"Happier Than Ever", "Billie Eilish", "Happier Than Ever", "Alternative Pop", "2021", "3360"},
                {"Donda", "Kanye West", "Donda", "Hip Hop", "2021", "6480"},
                {"Certified Lover Boy", "Drake", "Certified Lover Boy", "Hip Hop", "2021", "5160"},
                {"30", "Adele", "30", "Soul", "2021", "3600"},
                {"Justice", "Justin Bieber", "Justice", "Pop", "2021", "3000"},
                {"Future Nostalgia", "Dua Lipa", "Future Nostalgia", "Disco Pop", "2020", "2220"},
                {"After Hours", "The Weeknd", "After Hours", "Synthpop", "2020", "3360"},
                {"Fine Line", "Harry Styles", "Fine Line", "Pop Rock", "2019", "2760"},
                {"When We All Fall Asleep", "Billie Eilish", "When We All Fall Asleep, Where Do We Go?", "Electropop", "2019", "2580"}
            };
            
            for (String[] datos : cancionesMuestra) {
                writer.println(String.join("\t", datos));
            }
            
            System.out.println("Archivo de muestra para carga masiva creado: " + DEFAULT_SONGS_FILE);
            System.out.println("Contiene " + cancionesMuestra.length + " canciones de ejemplo");
            
            return DEFAULT_SONGS_FILE;
            
        } catch (IOException e) {
            System.err.println("Error creando archivo de muestra: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Valida el formato de un archivo antes de la carga masiva.
     * 
     * @param rutaArchivo Ruta del archivo a validar
     * @return Resultado de la validación
     */
    public ResultadoValidacion validarArchivoFormato(String rutaArchivo) {
        File file = new File(rutaArchivo);
        if (!file.exists()) {
            return new ResultadoValidacion(false, "Archivo no encontrado: " + rutaArchivo, 0, new ArrayList<>());
        }
        
        List<String> errores = new ArrayList<>();
        int lineasValidas = 0;
        int numeroLinea = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            
            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                
                if (linea.trim().isEmpty() || linea.trim().startsWith("#")) {
                    continue;
                }
                
                String[] campos = linea.split(FIELD_SEPARATOR);
                if (campos.length < EXPECTED_FIELDS) {
                    errores.add("Línea " + numeroLinea + ": Campos insuficientes (" + 
                              campos.length + "/" + EXPECTED_FIELDS + ")");
                    continue;
                }
                
                // Validar tipos de datos
                try {
                    Integer.parseInt(campos[4].trim()); // año
                    Integer.parseInt(campos[5].trim()); // duración
                    lineasValidas++;
                } catch (NumberFormatException e) {
                    errores.add("Línea " + numeroLinea + ": Formato numérico inválido en año o duración");
                }
            }
            
            boolean esValido = errores.size() < (numeroLinea * 0.1); // Tolerar hasta 10% de errores
            String mensaje = String.format("Validación completada. Líneas válidas: %d/%d, Errores: %d",
                                          lineasValidas, numeroLinea, errores.size());
            
            return new ResultadoValidacion(esValido, mensaje, lineasValidas, errores);
            
        } catch (IOException e) {
            return new ResultadoValidacion(false, "Error leyendo archivo: " + e.getMessage(), 0, 
                                         Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Obtiene estadísticas del servicio de carga masiva.
     * 
     * @return String con estadísticas
     */
    public String getEstadisticas() {
        File bulkDir = new File(BULK_DATA_DIR);
        int archivosDisponibles = 0;
        
        if (bulkDir.exists() && bulkDir.isDirectory()) {
            File[] archivos = bulkDir.listFiles((dir, name) -> name.endsWith(".txt"));
            archivosDisponibles = archivos != null ? archivos.length : 0;
        }
        
        return String.format(
            "=== BulkDataLoader - Estadísticas ===\n" +
            "Directorio de carga: %s\n" +
            "Archivos disponibles: %d\n" +
            "Formato esperado: TSV (Tab-separated)\n" +
            "Campos requeridos: %d\n" +
            "Validaciones: Año (1900-2030), Duración (1-3600s)\n" +
            "Archivo por defecto: %s",
            BULK_DATA_DIR,
            archivosDisponibles,
            EXPECTED_FIELDS,
            DEFAULT_SONGS_FILE
        );
    }
    
    // Clases auxiliares
    
    /**
     * Clase para encapsular el resultado de una carga masiva.
     */
    public static class ResultadoCargaMasiva {
        public final boolean exito;
        public final String mensaje;
        public final int lineasProcesadas;
        public final int cancionesAgregadas;
        public final List<String> errores;
        
        public ResultadoCargaMasiva(boolean exito, String mensaje, int lineasProcesadas, 
                                   int cancionesAgregadas, List<String> errores) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.lineasProcesadas = lineasProcesadas;
            this.cancionesAgregadas = cancionesAgregadas;
            this.errores = new ArrayList<>(errores);
        }
        
        @Override
        public String toString() {
            return String.format("ResultadoCargaMasiva{exito=%s, lineasProcesadas=%d, " +
                               "cancionesAgregadas=%d, errores=%d}",
                               exito, lineasProcesadas, cancionesAgregadas, errores.size());
        }
    }
    
    /**
     * Clase para resultado de validación de archivo.
     */
    public static class ResultadoValidacion {
        public final boolean valido;
        public final String mensaje;
        public final int lineasValidas;
        public final List<String> errores;
        
        public ResultadoValidacion(boolean valido, String mensaje, int lineasValidas, List<String> errores) {
            this.valido = valido;
            this.mensaje = mensaje;
            this.lineasValidas = lineasValidas;
            this.errores = new ArrayList<>(errores);
        }
        
        @Override
        public String toString() {
            return String.format("ResultadoValidacion{valido=%s, lineasValidas=%d, errores=%d}",
                               valido, lineasValidas, errores.size());
        }
    }
}