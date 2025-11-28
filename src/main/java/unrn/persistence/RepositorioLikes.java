package unrn.persistence;

import unrn.model.Like;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface RepositorioLikes {

    Like guardar(Like like);

    void eliminar(Like like);

    List<Like> likesDeTweet(Tweet tweet);

    boolean existeLikeDeUsuarioSobreTweet(Usuario usuario, Tweet tweet);
}
