package unrn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Follow;
import unrn.model.Usuario;
import unrn.persistence.RepositorioFollows;
import unrn.persistence.RepositorioUsuarios;

import java.util.List;

@Service
public class ServicioSocialAplicacion implements ServicioSocial {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioFollows repositorioFollows;

    public ServicioSocialAplicacion(RepositorioUsuarios repositorioUsuarios,
            RepositorioFollows repositorioFollows) {

        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioFollows = repositorioFollows;
    }

    @Override
    public void seguir(String keycloakIdSeguidor, Long idSeguido) {
        Usuario seguidor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdSeguidor);
        Usuario seguido = repositorioUsuarios.buscarPorId(idSeguido);

        if (repositorioFollows.existeFollowEntre(seguidor, seguido)) {
            // ya lo sigue, no hacemos nada
            return;
        }

        Follow follow = seguidor.seguir(seguido);
        repositorioFollows.guardar(follow);
    }

    @Override
    @Transactional
    public void dejarDeSeguir(String keycloakIdSeguidor, Long idSeguido) {
        Usuario seguidor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdSeguidor);
        Usuario seguido = repositorioUsuarios.buscarPorId(idSeguido);

        if (!repositorioFollows.existeFollowEntre(seguidor, seguido)) {
            // no lo seguía, nada que hacer
            return;
        }

        // Intención expresada en el dominio:
        seguidor.dejarDeSeguir(seguido);
        // Efecto concreto en persistencia:
        repositorioFollows.eliminarFollowEntre(seguidor, seguido);
    }

    @Override
    public List<Usuario> seguidosDelActual(String keycloakIdSeguidor) {
        Usuario seguidor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdSeguidor);
        return repositorioFollows.seguidosDe(seguidor);
    }

    @Override
    public List<Usuario> seguidoresDe(Long idUsuario) {
        Usuario usuario = repositorioUsuarios.buscarPorId(idUsuario);
        return repositorioFollows.seguidoresDe(usuario);
    }
}
