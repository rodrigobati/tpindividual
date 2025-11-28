package unrn.service;

import unrn.model.Usuario;

import java.util.List;

/**
 * Casos de uso relacionados con relaciones sociales:
 * seguir / dejar de seguir, seguidores, seguidos.
 */
public interface ServicioSocial {

    void seguir(String keycloakIdSeguidor, Long idSeguido);

    void dejarDeSeguir(String keycloakIdSeguidor, Long idSeguido);

    List<Usuario> seguidosDelActual(String keycloakIdSeguidor);

    List<Usuario> seguidoresDe(Long idUsuario);
}
