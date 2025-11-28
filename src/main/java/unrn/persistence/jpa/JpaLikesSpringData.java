package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.Like;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaLikesSpringData extends JpaRepository<Like, Long> {

    List<Like> findByTweet(Tweet tweet);

    boolean existsByAutorAndTweet(Usuario autor, Tweet tweet);

    void deleteByAutorAndTweet(Usuario autor, Tweet tweet);
}
