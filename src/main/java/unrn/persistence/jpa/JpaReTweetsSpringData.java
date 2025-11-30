package unrn.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaReTweetsSpringData extends JpaRepository<ReTweet, Long> {

    List<ReTweet> findByAutorOrderByFechaCreacionDesc(Usuario autor);

    List<ReTweet> findByOriginal(Tweet original);

    /**
     * Verifica si existe un retweet de un autor específico sobre un tweet original.
     */
    boolean existsByAutorAndOriginal(Usuario autor, Tweet original);

    /**
     * Busca retweets hechos por una lista de autores, ordenados por fecha
     * descendente.
     * Similar a buscarTweetsDeAutores pero para retweets.
     */
    @Query("SELECT r FROM ReTweet r WHERE r.autor IN :autores ORDER BY r.fechaCreacion DESC")
    List<ReTweet> findByAutorInOrderByFechaCreacionDesc(@Param("autores") List<Usuario> autores, Pageable pageable);
}
