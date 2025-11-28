package unrn.persistence;

import java.util.List;

import unrn.model.Tweet;
import unrn.model.Usuario;

public interface RepositorioTweets {
    Tweet buscarPorId(Long idTweet);

    Tweet guardar(Tweet tweet);

    void eliminar(Long idTweet);

    List<Tweet> tweetsDeUsuario(Usuario autor);

    List<Tweet> timelineDeUsuario(Usuario usuario, int limite);
}
