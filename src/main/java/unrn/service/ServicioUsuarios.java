package unrn.service;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Servicio de gestión de usuarios
 * 
 * Responsabilidad: Operaciones relacionadas con la gestión del ciclo de vida
 * de usuarios, incluyendo la creación automática desde JWT y desactivación.
 */
public interface ServicioUsuarios {

    /**
     * Asegura que el usuario autenticado existe en la base de datos.
     * Si no existe, lo crea automáticamente con datos del JWT.
     * 
     * Este método es idempotente y thread-safe.
     * 
     * @param jwt Token JWT del usuario autenticado
     */
    void asegurarUsuarioExiste(Jwt jwt);

    /**
     * Desactiva un usuario y marca todos sus tweets como eliminados.
     * Mantiene la invariante de dominio:
     * "Los tweets de un usuario deben eliminarse cuando el usuario es eliminado."
     * 
     * Operación transaccional y atómica.
     * 
     * @param keycloakId ID de Keycloak del usuario a desactivar
     */
    void desactivarUsuario(String keycloakId);
}
