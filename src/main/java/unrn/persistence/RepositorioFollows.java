package unrn.persistence;

import unrn.model.Follow;
import unrn.model.Usuario;

import java.util.List;

public interface RepositorioFollows {

    Follow guardar(Follow follow);

    void eliminar(Follow follow);

    boolean existeFollowEntre(Usuario seguidor, Usuario seguido);

    List<Usuario> seguidosDe(Usuario seguidor);

    List<Usuario> seguidoresDe(Usuario seguido);

    void eliminarFollowEntre(Usuario seguidor, Usuario seguido);
}
