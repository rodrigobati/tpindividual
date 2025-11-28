package unrn.persistence;

import org.springframework.stereotype.Repository;
import unrn.model.Like;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaLikesSpringData;

import java.util.List;

@Repository
public class RepositorioLikesJpa implements RepositorioLikes {

    private final JpaLikesSpringData jpa;

    public RepositorioLikesJpa(JpaLikesSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public Like guardar(Like like) {
        return jpa.save(like);
    }

    @Override
    public void eliminar(Like like) {
        jpa.delete(like);
    }

    @Override
    public List<Like> likesDeTweet(Tweet tweet) {
        return jpa.findByTweet(tweet);
    }

    @Override
    public boolean existeLikeDeUsuarioSobreTweet(Usuario usuario, Tweet tweet) {
        return jpa.existsByAutorAndTweet(usuario, tweet);
    }
}
