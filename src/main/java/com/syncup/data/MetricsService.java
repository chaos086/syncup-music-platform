package com.syncup.data;

import com.syncup.models.Usuario;
import java.util.Optional;

public class MetricsService {
    private final DataManager dataManager;
    private final UserRepository userRepository;

    public MetricsService() {
        this.dataManager = DataManager.getInstance();
        this.userRepository = new UserRepository();
    }

    /**
     * Total de usuarios registrados (persistidos o en memoria)
     */
    public int getTotalUsers() {
        // Preferir persistencia cuando sea posible
        return Math.max(userRepositoryCountApprox(), dataManager.getAllUsuarios().size());
    }

    /**
     * Usuarios activos (con isActivo=true). Si no hay bandera de activo, asumir todos activos.
     */
    public int getActiveUsers() {
        return (int) dataManager.getAllUsuarios().stream().filter(Usuario::isActivo).count();
    }

    /**
     * Número de administradores (isEsAdmin=true)
     */
    public int getAdminUsers() {
        return (int) dataManager.getAllUsuarios().stream().filter(Usuario::isEsAdmin).count();
    }

    // Como el repositorio no expone listado completo, usamos aproximación basada en DataManager
    private int userRepositoryCountApprox() { return dataManager.getAllUsuarios().size(); }
}
