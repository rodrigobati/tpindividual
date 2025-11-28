package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.Usuario;

import java.util.Optional;

public interface JpaUsuariosSpringData extends JpaRepository<Usuario, Long> {

    boolean existsByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByKeycloakId(String keycloakId);

    Optional<Usuario> findByKeycloakId(String keycloakId);
}
