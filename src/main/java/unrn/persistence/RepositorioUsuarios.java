package unrn.persistence;

import unrn.model.Usuario;

public interface RepositorioUsuarios {

    Usuario buscarPorId(Long idUsuario);

    Usuario guardar(Usuario usuario);

    boolean existePorNombreUsuario(String nombreUsuario);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    boolean existePorKeycloakId(String keycloakId);

    Usuario buscarPorKeycloakId(String keycloakId);
}
