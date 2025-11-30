package unrn.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaTweetsSpringData extends JpaRepository<Tweet, Long> {

    // Todos los tweets de un usuario, más nuevos primero
    List<Tweet> findByAutorOrderByFechaCreacionDesc(Usuario autor);

    /**
     * Busca tweets de una lista de autores, ordenados por fecha descendente.
     * Usa Pageable para limitar resultados.
     */
    @Query("SELECT t FROM Tweet t WHERE t.autor IN :autores ORDER BY t.fechaCreacion DESC")
    List<Tweet> findByAutorInOrderByFechaCreacionDesc(@Param("autores") List<Usuario> autores, Pageable pageable);
}
