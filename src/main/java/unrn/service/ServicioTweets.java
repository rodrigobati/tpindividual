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

    /**
     * Timeline del usuario: tweets propios + tweets de seguidos + retweets de
     * seguidos.
     * Retorna una lista unificada de items ordenados por fecha.
     * 
     * @param keycloakIdUsuario Identificador de Keycloak del usuario
     * @param limite            Cantidad máxima de items a retornar
     * @return Lista de items del timeline (tweets y retweets mezclados)
     */
    List<TimelineItem> timeline(String keycloakIdUsuario, int limite);

    /**
     * Obtiene TODOS los tweets del sistema (sin filtrar por seguimiento).
     * Solo retorna tweets originales, NO incluye retweets.
     * Ordenados por fecha de creación descendente (más reciente primero).
     * 
     * @param limite Cantidad máxima de tweets a retornar
     * @return Lista de tweets originales ordenados por fecha
     */
    List<Tweet> obtenerTodosTweets(int limite);

    /**
     * Obtiene todos los tweets y retweets de un usuario específico.
     * Retorna tweets originales del usuario Y retweets hechos por ese usuario.
     * 
     * @param idUsuario ID del usuario
     * @param limite    Cantidad máxima de items a retornar
     * @return Lista de items (tweets y retweets) ordenados por fecha
     */
    List<TimelineItem> tweetsDeUsuario(Long idUsuario, int limite);

    List<RespuestaTweet> respuestasDeTweet(Long idTweet);

    List<Like> likesDeTweet(Long idTweet);
}
