package com.syncup.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Clase que representa un administrador del sistema SyncUp.
 * Extiende Usuario con funcionalidades administrativas específicas.
 * 
 * @author Alejandro Marín Hernández
 * @version 1.0
 * @since 2025-11-01
 */
public class Admin extends Usuario {
    
    /** Nivel de acceso del administrador (1-5, siendo 5 el máximo) */
    private int nivelAcceso;
    
    /** Fecha de último acceso administrativo */
    private LocalDateTime ultimoAccesoAdmin;
    
    /** Lista de acciones administrativas realizadas */
    private List<AccionAdministrativa> historialAcciones;
    
    /** Estadísticas de gestión */
    private Map<String, Object> estadisticasGestion;
    
    /** Departamento o área del administrador */
    private String departamento;
    
    /** Indica si puede gestionar usuarios */
    private boolean puedeGestionarUsuarios;
    
    /** Indica si puede gestionar catálogo de música */
    private boolean puedeGestionarCatalogo;
    
    /** Indica si puede ver reportes del sistema */
    private boolean puedeVerReportes;
    
    /** Indica si puede realizar carga masiva */
    private boolean puedeCargaMasiva;
    
    /**
     * Constructor completo para crear un administrador.
     * 
     * @param id Identificador único
     * @param username Nombre de usuario
     * @param password Contraseña
     * @param nombreCompleto Nombre completo
     * @param email Correo electrónico
     * @param nivelAcceso Nivel de acceso (1-5)
     */
    public Admin(String id, String username, String password, String nombreCompleto, String email, int nivelAcceso) {
        super(id, username, password, nombreCompleto, email);
        this.nivelAcceso = Math.max(1, Math.min(5, nivelAcceso)); // Asegurar rango 1-5
        this.ultimoAccesoAdmin = LocalDateTime.now();
        this.historialAcciones = new ArrayList<>();
        this.estadisticasGestion = new HashMap<>();
        this.departamento = "Administración General";
        
        // Configurar permisos basados en nivel de acceso
        configurarPermisos();
        
        // Marcar como administrador
        setEsAdmin(true);
        
        // Inicializar estadísticas
        inicializarEstadisticas();
    }
    
    /**
     * Constructor simplificado para crear administrador.
     * 
     * @param username Nombre de usuario
     * @param password Contraseña
     * @param nivelAcceso Nivel de acceso
     */
    public Admin(String username, String password, int nivelAcceso) {
        this(generateAdminId(username), username, password, "Administrador", "", nivelAcceso);
    }
    
    /**
     * Genera un ID único para administrador.
     * 
     * @param username Nombre de usuario
     * @return ID único generado
     */
    private static String generateAdminId(String username) {
        return "admin_" + username + "_" + System.currentTimeMillis();
    }
    
    /**
     * Configura permisos basados en el nivel de acceso.
     */
    private void configurarPermisos() {
        switch (nivelAcceso) {
            case 5: // Super Administrador
                puedeGestionarUsuarios = true;
                puedeGestionarCatalogo = true;
                puedeVerReportes = true;
                puedeCargaMasiva = true;
                departamento = "Administración Ejecutiva";
                break;
                
            case 4: // Administrador Senior
                puedeGestionarUsuarios = true;
                puedeGestionarCatalogo = true;
                puedeVerReportes = true;
                puedeCargaMasiva = true;
                departamento = "Administración Senior";
                break;
                
            case 3: // Administrador de Contenido
                puedeGestionarUsuarios = false;
                puedeGestionarCatalogo = true;
                puedeVerReportes = true;
                puedeCargaMasiva = true;
                departamento = "Gestión de Contenido";
                break;
                
            case 2: // Moderador
                puedeGestionarUsuarios = true;
                puedeGestionarCatalogo = false;
                puedeVerReportes = false;
                puedeCargaMasiva = false;
                departamento = "Moderación";
                break;
                
            case 1: // Asistente
                puedeGestionarUsuarios = false;
                puedeGestionarCatalogo = false;
                puedeVerReportes = true;
                puedeCargaMasiva = false;
                departamento = "Soporte";
                break;
                
            default:
                configurarPermisosDefault();
                break;
        }
    }
    
    /**
     * Configura permisos por defecto para nivel no reconocido.
     */
    private void configurarPermisosDefault() {
        puedeGestionarUsuarios = false;
        puedeGestionarCatalogo = false;
        puedeVerReportes = false;
        puedeCargaMasiva = false;
        departamento = "Sin Asignar";
    }
    
    /**
     * Inicializa las estadísticas de gestión.
     */
    private void inicializarEstadisticas() {
        estadisticasGestion.put("usuariosCreados", 0);
        estadisticasGestion.put("usuariosEditados", 0);
        estadisticasGestion.put("usuariosEliminados", 0);
        estadisticasGestion.put("cancionesAgregadas", 0);
        estadisticasGestion.put("cancionesEditadas", 0);
        estadisticasGestion.put("cancionesEliminadas", 0);
        estadisticasGestion.put("reportesGenerados", 0);
        estadisticasGestion.put("cargasMasivasRealizadas", 0);
        estadisticasGestion.put("ultimaActividad", LocalDateTime.now());
    }
    
    /**
     * Registra una acción administrativa.
     * 
     * @param tipoAccion Tipo de acción realizada
     * @param descripcion Descripción de la acción
     * @param objetoAfectado Objeto o entidad afectada
     */
    public void registrarAccion(TipoAccionAdmin tipoAccion, String descripcion, String objetoAfectado) {
        AccionAdministrativa accion = new AccionAdministrativa(
            tipoAccion, descripcion, objetoAfectado, LocalDateTime.now()
        );
        
        historialAcciones.add(accion);
        ultimoAccesoAdmin = LocalDateTime.now();
        estadisticasGestion.put("ultimaActividad", ultimoAccesoAdmin);
        
        // Actualizar estadísticas específicas
        actualizarEstadisticas(tipoAccion);
        
        System.out.println("[ADMIN] " + getUsername() + " - " + tipoAccion + ": " + descripcion);
    }
    
    /**
     * Actualiza las estadísticas basadas en el tipo de acción.
     * 
     * @param tipoAccion Tipo de acción realizada
     */
    private void actualizarEstadisticas(TipoAccionAdmin tipoAccion) {
        switch (tipoAccion) {
            case CREAR_USUARIO:
                incrementarEstadistica("usuariosCreados");
                break;
            case EDITAR_USUARIO:
                incrementarEstadistica("usuariosEditados");
                break;
            case ELIMINAR_USUARIO:
                incrementarEstadistica("usuariosEliminados");
                break;
            case AGREGAR_CANCION:
                incrementarEstadistica("cancionesAgregadas");
                break;
            case EDITAR_CANCION:
                incrementarEstadistica("cancionesEditadas");
                break;
            case ELIMINAR_CANCION:
                incrementarEstadistica("cancionesEliminadas");
                break;
            case GENERAR_REPORTE:
                incrementarEstadistica("reportesGenerados");
                break;
            case CARGA_MASIVA:
                incrementarEstadistica("cargasMasivasRealizadas");
                break;
        }
    }
    
    /**
     * Incrementa una estadística específica.
     * 
     * @param clave Clave de la estadística
     */
    private void incrementarEstadistica(String clave) {
        int valorActual = (Integer) estadisticasGestion.getOrDefault(clave, 0);
        estadisticasGestion.put(clave, valorActual + 1);
    }
    
    /**
     * Obtiene el historial de acciones recientes.
     * 
     * @param limite Número máximo de acciones a retornar
     * @return Lista de acciones recientes
     */
    public List<AccionAdministrativa> getAccionesRecientes(int limite) {
        int inicio = Math.max(0, historialAcciones.size() - limite);
        return new ArrayList<>(historialAcciones.subList(inicio, historialAcciones.size()));
    }
    
    /**
     * Obtiene un resumen de las estadísticas de gestión.
     * 
     * @return String con el resumen de estadísticas
     */
    public String getResumenEstadisticas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Estadísticas de Administración ===\n");
        sb.append("Administrador: ").append(getNombreCompleto()).append("\n");
        sb.append("Nivel de Acceso: ").append(nivelAcceso).append("/5\n");
        sb.append("Departamento: ").append(departamento).append("\n");
        sb.append("Último Acceso: ").append(ultimoAccesoAdmin).append("\n\n");
        
        sb.append("=== Actividad ===\n");
        sb.append("Usuarios Creados: ").append(estadisticasGestion.get("usuariosCreados")).append("\n");
        sb.append("Usuarios Editados: ").append(estadisticasGestion.get("usuariosEditados")).append("\n");
        sb.append("Canciones Agregadas: ").append(estadisticasGestion.get("cancionesAgregadas")).append("\n");
        sb.append("Reportes Generados: ").append(estadisticasGestion.get("reportesGenerados")).append("\n");
        sb.append("Cargas Masivas: ").append(estadisticasGestion.get("cargasMasivasRealizadas")).append("\n");
        
        sb.append("\n=== Permisos ===\n");
        sb.append("Gestionar Usuarios: ").append(puedeGestionarUsuarios ? "Sí" : "No").append("\n");
        sb.append("Gestionar Catálogo: ").append(puedeGestionarCatalogo ? "Sí" : "No").append("\n");
        sb.append("Ver Reportes: ").append(puedeVerReportes ? "Sí" : "No").append("\n");
        sb.append("Carga Masiva: ").append(puedeCargaMasiva ? "Sí" : "No").append("\n");
        
        return sb.toString();
    }
    
    /**
     * Verifica si tiene un permiso específico.
     * 
     * @param permiso Tipo de permiso a verificar
     * @return true si tiene el permiso, false en caso contrario
     */
    public boolean tienePermiso(TipoPermisoAdmin permiso) {
        switch (permiso) {
            case GESTIONAR_USUARIOS:
                return puedeGestionarUsuarios;
            case GESTIONAR_CATALOGO:
                return puedeGestionarCatalogo;
            case VER_REPORTES:
                return puedeVerReportes;
            case CARGA_MASIVA:
                return puedeCargaMasiva;
            default:
                return false;
        }
    }
    
    /**
     * Calcula el nivel de actividad del administrador.
     * 
     * @return Nivel de actividad como porcentaje (0-100)
     */
    public double calcularNivelActividad() {
        int totalAcciones = historialAcciones.size();
        if (totalAcciones == 0) return 0.0;
        
        // Considerar acciones de los últimos 30 días
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        long accionesRecientes = historialAcciones.stream()
            .mapToLong(accion -> accion.getFechaHora().isAfter(hace30Dias) ? 1 : 0)
            .sum();
        
        // Calcular porcentaje basado en actividad esperada (ej: 1 acción por día)
        double actividadEsperada = 30.0; // 30 acciones en 30 días
        return Math.min(100.0, (accionesRecientes / actividadEsperada) * 100.0);
    }
    
    // Getters y Setters específicos
    
    public int getNivelAcceso() {
        return nivelAcceso;
    }
    
    public void setNivelAcceso(int nivelAcceso) {
        this.nivelAcceso = Math.max(1, Math.min(5, nivelAcceso));
        configurarPermisos();
    }
    
    public LocalDateTime getUltimoAccesoAdmin() {
        return ultimoAccesoAdmin;
    }
    
    public void setUltimoAccesoAdmin(LocalDateTime ultimoAccesoAdmin) {
        this.ultimoAccesoAdmin = ultimoAccesoAdmin;
    }
    
    public List<AccionAdministrativa> getHistorialAcciones() {
        return new ArrayList<>(historialAcciones);
    }
    
    public Map<String, Object> getEstadisticasGestion() {
        return new HashMap<>(estadisticasGestion);
    }
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
    
    public boolean isPuedeGestionarUsuarios() {
        return puedeGestionarUsuarios;
    }
    
    public boolean isPuedeGestionarCatalogo() {
        return puedeGestionarCatalogo;
    }
    
    public boolean isPuedeVerReportes() {
        return puedeVerReportes;
    }
    
    public boolean isPuedeCargaMasiva() {
        return puedeCargaMasiva;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "username='" + getUsername() + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", nivelAcceso=" + nivelAcceso +
                ", departamento='" + departamento + '\'' +
                ", ultimoAcceso=" + ultimoAccesoAdmin +
                ", totalAcciones=" + historialAcciones.size() +
                ", actividad=" + String.format("%.1f%%", calcularNivelActividad()) +
                '}';
    }
    
    /**
     * Clase interna para representar una acción administrativa.
     */
    public static class AccionAdministrativa {
        private TipoAccionAdmin tipo;
        private String descripcion;
        private String objetoAfectado;
        private LocalDateTime fechaHora;
        
        public AccionAdministrativa(TipoAccionAdmin tipo, String descripcion, String objetoAfectado, LocalDateTime fechaHora) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.objetoAfectado = objetoAfectado;
            this.fechaHora = fechaHora;
        }
        
        // Getters
        public TipoAccionAdmin getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
        public String getObjetoAfectado() { return objetoAfectado; }
        public LocalDateTime getFechaHora() { return fechaHora; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (%s)", 
                fechaHora.toString(), tipo, descripcion, objetoAfectado);
        }
    }
    
    /**
     * Enumeración para tipos de acciones administrativas.
     */
    public enum TipoAccionAdmin {
        CREAR_USUARIO("Crear Usuario"),
        EDITAR_USUARIO("Editar Usuario"),
        ELIMINAR_USUARIO("Eliminar Usuario"),
        AGREGAR_CANCION("Agregar Canción"),
        EDITAR_CANCION("Editar Canción"),
        ELIMINAR_CANCION("Eliminar Canción"),
        GENERAR_REPORTE("Generar Reporte"),
        CARGA_MASIVA("Carga Masiva"),
        VER_METRICAS("Ver Métricas"),
        CONFIGURAR_SISTEMA("Configurar Sistema");
        
        private final String descripcion;
        
        TipoAccionAdmin(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Enumeración para tipos de permisos administrativos.
     */
    public enum TipoPermisoAdmin {
        GESTIONAR_USUARIOS,
        GESTIONAR_CATALOGO,
        VER_REPORTES,
        CARGA_MASIVA,
        CONFIGURAR_SISTEMA
    }
}