package unrn.persistence;

import org.springframework.stereotype.Repository;
import unrn.model.Follow;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaFollowsSpringData;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RepositorioFollowsJpa implements RepositorioFollows {

    private final JpaFollowsSpringData jpa;

    public RepositorioFollowsJpa(JpaFollowsSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public Follow guardar(Follow follow) {
        return jpa.save(follow);
    }

    @Override
    public void eliminar(Follow follow) {
        jpa.delete(follow);
    }

    @Override
    public boolean existeFollowEntre(Usuario seguidor, Usuario seguido) {
        return jpa.existsBySeguidorAndSeguido(seguidor, seguido);
    }

    @Override
    public List<Usuario> seguidosDe(Usuario seguidor) {
        return jpa.findBySeguidor(seguidor).stream()
                .map(f -> f.seguido())
                .collect(Collectors.toList());
    }

    @Override
    public List<Usuario> seguidoresDe(Usuario seguido) {
        return jpa.findBySeguido(seguido).stream()
                .map(f -> f.seguidor())
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarFollowEntre(Usuario seguidor, Usuario seguido) {
        jpa.deleteBySeguidorAndSeguido(seguidor, seguido);
    }

}
