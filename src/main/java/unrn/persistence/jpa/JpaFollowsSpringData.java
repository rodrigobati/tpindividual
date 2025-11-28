package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.Follow;
import unrn.model.Usuario;

import java.util.List;

public interface JpaFollowsSpringData extends JpaRepository<Follow, Long> {

    boolean existsBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    void deleteBySeguidorAndSeguido(Usuario seguidor, Usuario seguido);

    // Usuarios que sigue X (sus "seguidos")
    List<Follow> findBySeguidor(Usuario seguidor);

    // Usuarios que siguen a X (sus "seguidores")
    List<Follow> findBySeguido(Usuario seguido);
}
