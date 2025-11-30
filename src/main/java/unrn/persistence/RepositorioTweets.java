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

    /**
     * Busca tweets de una lista de autores, ordenados por fecha descendente.
     * 
     * @param autores Lista de usuarios cuyos tweets se quieren obtener
     * @param limite  Máximo número de tweets a retornar
     * @return Lista de tweets ordenados por fecha de creación descendente
     */
    List<Tweet> buscarTweetsDeAutores(List<Usuario> autores, int limite);
}
