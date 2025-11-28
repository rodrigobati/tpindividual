package unrn.service;

import org.springframework.stereotype.Service;
import unrn.model.Like;
import unrn.model.RespuestaTweet;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioLikes;
import unrn.persistence.RepositorioRespuestas;
import unrn.persistence.RepositorioRetweets;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;

import java.util.List;

@Service
public class ServicioTweetsAplicacion implements ServicioTweets {

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioTweets repositorioTweets;
    private final RepositorioRetweets repositorioRetweets;
    private final RepositorioRespuestas repositorioRespuestas;
    private final RepositorioLikes repositorioLikes;

    public ServicioTweetsAplicacion(RepositorioUsuarios repositorioUsuarios,
            RepositorioTweets repositorioTweets,
            RepositorioRetweets repositorioRetweets,
            RepositorioRespuestas repositorioRespuestas,
            RepositorioLikes repositorioLikes) {

        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioTweets = repositorioTweets;
        this.repositorioRetweets = repositorioRetweets;
        this.repositorioRespuestas = repositorioRespuestas;
        this.repositorioLikes = repositorioLikes;
    }

    @Override
    public Tweet publicarTweet(String keycloakIdAutor, String contenido) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet tweet = autor.publicarTweet(contenido);
        return repositorioTweets.guardar(tweet);
    }

    @Override
    public ReTweet retweetear(String keycloakIdAutor, Long idTweetOriginal) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet original = repositorioTweets.buscarPorId(idTweetOriginal);
        ReTweet retweet = autor.retweet(original);
        return repositorioRetweets.guardar(retweet);
    }

    @Override
    public RespuestaTweet responderATweet(String keycloakIdAutor,
            Long idTweetOriginal,
            String contenido) {

        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet tweetRespondido = repositorioTweets.buscarPorId(idTweetOriginal);
        RespuestaTweet respuesta = autor.responder(tweetRespondido, contenido);
        return repositorioRespuestas.guardar(respuesta);
    }

    @Override
    public void eliminarTweet(String keycloakIdAutor, Long idTweet) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet tweet = repositorioTweets.buscarPorId(idTweet);

        if (!tweet.esDe(autor)) {
            throw new RuntimeException("No puede eliminar un tweet que no es suyo");
        }

        tweet.eliminar();
        repositorioTweets.guardar(tweet);
    }

    @Override
    public void eliminarRespuesta(String keycloakIdAutor, Long idRespuesta) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        RespuestaTweet respuesta = repositorioRespuestas.buscarPorId(idRespuesta);

        if (!respuesta.esDe(autor)) {
            throw new RuntimeException("No puede eliminar una respuesta que no es suya");
        }

        respuesta.eliminar();
        repositorioRespuestas.guardar(respuesta);
    }

    @Override
    public void darLike(String keycloakIdAutor, Long idTweet) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet tweet = repositorioTweets.buscarPorId(idTweet);

        if (repositorioLikes.existeLikeDeUsuarioSobreTweet(autor, tweet)) {
            // ya tiene like, no hacemos nada
            return;
        }

        Like like = autor.darLike(tweet);
        repositorioLikes.guardar(like);
    }

    @Override
    public void quitarLike(String keycloakIdAutor, Long idTweet) {
        Usuario autor = repositorioUsuarios.buscarPorKeycloakId(keycloakIdAutor);
        Tweet tweet = repositorioTweets.buscarPorId(idTweet);

        if (!repositorioLikes.existeLikeDeUsuarioSobreTweet(autor, tweet)) {
            return;
        }

        List<Like> likes = repositorioLikes.likesDeTweet(tweet);
        for (Like like : likes) {
            if (like.esDe(autor)) {
                repositorioLikes.eliminar(like);
                break;
            }
        }
    }

    @Override
    public List<Tweet> timeline(String keycloakIdUsuario, int limite) {
        Usuario usuario = repositorioUsuarios.buscarPorKeycloakId(keycloakIdUsuario);
        return repositorioTweets.timelineDeUsuario(usuario, limite);
    }

    @Override
    public List<RespuestaTweet> respuestasDeTweet(Long idTweet) {
        Tweet tweet = repositorioTweets.buscarPorId(idTweet);
        return repositorioRespuestas.respuestasDeTweet(tweet);
    }

    @Override
    public List<Like> likesDeTweet(Long idTweet) {
        Tweet tweet = repositorioTweets.buscarPorId(idTweet);
        return repositorioLikes.likesDeTweet(tweet);
    }
}
