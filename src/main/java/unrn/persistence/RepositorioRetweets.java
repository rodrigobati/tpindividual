package unrn.persistence;

import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface RepositorioRetweets {

    ReTweet guardar(ReTweet retweet);

    List<ReTweet> retweetsDeUsuario(Usuario autor);

    List<ReTweet> retweetsDeTweet(Tweet original);

    ReTweet buscarPorId(Long idRetweet);

    /**
     * Verifica si ya existe un retweet de un usuario sobre un tweet específico.
     * Usado para evitar retweets duplicados.
     */
    boolean existeRetweetDeUsuarioSobreTweet(Usuario autor, Tweet original);

    /**
     * Obtiene los retweets hechos por una lista de usuarios.
     * Usado para construir el timeline: muestra los retweets de los usuarios
     * seguidos.
     * 
     * @param autores Lista de usuarios cuyos retweets queremos obtener
     * @param limite  Cantidad máxima de retweets a retornar
     * @return Lista de retweets ordenados por fecha de creación descendente
     */
    List<ReTweet> buscarRetweetsDeAutores(List<Usuario> autores, int limite);
}
