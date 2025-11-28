package unrn.service;

import unrn.model.Like;
import unrn.model.RespuestaTweet;
import unrn.model.ReTweet;
import unrn.model.Tweet;

import java.util.List;

/**
 * Casos de uso relacionados con tweets (crear, responder, retuitear, eliminar).
 */
public interface ServicioTweets {

    Tweet publicarTweet(String keycloakIdAutor, String contenido);

    ReTweet retweetear(String keycloakIdAutor, Long idTweetOriginal);

    RespuestaTweet responderATweet(String keycloakIdAutor, Long idTweetOriginal, String contenido);

    void eliminarTweet(String keycloakIdAutor, Long idTweet);

    void eliminarRespuesta(String keycloakIdAutor, Long idRespuesta);

    void darLike(String keycloakIdAutor, Long idTweet);

    void quitarLike(String keycloakIdAutor, Long idTweet);

    List<Tweet> timeline(String keycloakIdUsuario, int limite);

    List<RespuestaTweet> respuestasDeTweet(Long idTweet);

    List<Like> likesDeTweet(Long idTweet);
}
