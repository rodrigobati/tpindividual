package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaReTweetsSpringData extends JpaRepository<ReTweet, Long> {

    List<ReTweet> findByAutorOrderByFechaCreacionDesc(Usuario autor);

    List<ReTweet> findByOriginal(Tweet original);
}
