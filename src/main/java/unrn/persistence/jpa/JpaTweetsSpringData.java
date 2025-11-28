package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaTweetsSpringData extends JpaRepository<Tweet, Long> {

    // Todos los tweets de un usuario, más nuevos primero
    List<Tweet> findByAutorOrderByFechaCreacionDesc(Usuario autor);
}
