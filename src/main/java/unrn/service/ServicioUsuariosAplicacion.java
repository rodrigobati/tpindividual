package unrn.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Usuario;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;

/**
 * Servicio de gestión de usuarios.
 * Responsable del ciclo de vida de usuarios autenticados vía Keycloak.
 */
@Service
public class ServicioUsuariosAplicacion implements ServicioUsuarios {

    private final RepositorioUsuarios repositorioUsuarios;

    public ServicioUsuariosAplicacion(RepositorioUsuarios repositorioUsuarios) {
        this.repositorioUsuarios = repositorioUsuarios;
    }

    /**
     * Asegura que el usuario autenticado existe en la base de datos.
     * Si no existe, lo crea automáticamente a partir de los datos del JWT.
     * 
     * Es idempotente: puede llamarse múltiples veces sin efectos secundarios.
     * Es thread-safe: maneja race conditions donde múltiples threads intentan
     * crear el mismo usuario simultáneamente.
     */
    @Override
    @Transactional
    public void asegurarUsuarioExiste(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        // Si ya existe, retornar inmediatamente
        if (repositorioUsuarios.existePorKeycloakId(keycloakId)) {
            return;
        }

        // Extraer información del JWT
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        // Si no hay email en el token, generar uno por defecto
        if (email == null || email.isBlank()) {
            email = username + "@minitwitter.local";
        }

        try {
            // Crear y guardar el nuevo usuario
            Usuario nuevoUsuario = new Usuario(
                    keycloakId,
                    username,
                    email,
                    LocalDateTime.now(),
                    null, // biografía vacía
                    null // avatar vacío
            );

            repositorioUsuarios.guardar(nuevoUsuario);

        } catch (DataIntegrityViolationException e) {
            // Race condition: otro thread creó el usuario entre la verificación y el
            // guardado
            // Verificar si ahora existe por keycloakId
            if (repositorioUsuarios.existePorKeycloakId(keycloakId)) {
                // OK, el usuario existe → ignorar el error
                return;
            }

            // Si no existe por keycloakId, entonces es un constraint de username/email
            // duplicado
            throw new RuntimeException(
                    "No se pudo crear el usuario: el nombre de usuario '" + username +
                            "' o email '" + email + "' ya están en uso",
                    e);
        }
    }
}
