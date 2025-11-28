package unrn.persistence;

import org.springframework.stereotype.Repository;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaUsuariosSpringData;

@Repository
public class RepositorioUsuariosJpa implements RepositorioUsuarios {

    private final JpaUsuariosSpringData jpa;

    public RepositorioUsuariosJpa(JpaUsuariosSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public Usuario buscarPorId(Long idUsuario) {
        return jpa.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return jpa.save(usuario);
    }

    @Override
    public boolean existePorNombreUsuario(String nombreUsuario) {
        return jpa.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        return jpa.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + nombreUsuario));
    }

    @Override
    public boolean existePorKeycloakId(String keycloakId) {
        return jpa.existsByKeycloakId(keycloakId);
    }

    @Override
    public Usuario buscarPorKeycloakId(String keycloakId) {
        return jpa.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para keycloakId: " + keycloakId));
    }
}
