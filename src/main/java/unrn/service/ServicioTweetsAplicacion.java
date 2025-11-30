package unrn.service;

import org.springframework.stereotype.Service;
import unrn.api.exception.OperacionNoPermitidaException;
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
    private final ServicioSocial servicioSocial;

    public ServicioTweetsAplicacion(RepositorioUsuarios repositorioUsuarios,
            RepositorioTweets repositorioTweets,
            RepositorioRetweets repositorioRetweets,
            RepositorioRespuestas repositorioRespuestas,
            RepositorioLikes repositorioLikes,
            ServicioSocial servicioSocial) {

        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioTweets = repositorioTweets;
        this.repositorioRetweets = repositorioRetweets;
        this.repositorioRespuestas = repositorioRespuestas;
        this.repositorioLikes = repositorioLikes;
        this.servicioSocial = servicioSocial;
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

        // Regla de negocio: No permitir retweet de tweets propios
        if (original.esDe(autor)) {
            throw new OperacionNoPermitidaException("No se puede hacer retweet de un tweet propio");
        }

        // Regla de negocio: No permitir retweets duplicados (idempotencia)
        if (repositorioRetweets.existeRetweetDeUsuarioSobreTweet(autor, original)) {
            // Ya existe un retweet de este usuario sobre este tweet
            // Estrategia: idempotencia - devolver existente sin error
            List<ReTweet> retweets = repositorioRetweets.retweetsDeUsuario(autor);
            return retweets.stream()
                    .filter(rt -> rt.esSobre(original))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Inconsistencia: retweet existe pero no se encuentra"));
        }

        // Crear y guardar nuevo retweet
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
    public List<TimelineItem> timeline(String keycloakIdUsuario, int limite) {
        Usuario usuario = repositorioUsuarios.buscarPorKeycloakId(keycloakIdUsuario);

        // Obtener usuarios seguidos por el usuario actual
        List<Usuario> seguidos = servicioSocial.seguidosDelActual(keycloakIdUsuario);

        // Crear lista de autores: usuario actual + usuarios seguidos
        List<Usuario> autoresDelTimeline = new java.util.ArrayList<>();
        autoresDelTimeline.add(usuario); // El usuario ve sus propios tweets
        autoresDelTimeline.addAll(seguidos); // Más los tweets de quienes sigue

        // Buscar tweets originales de todos esos autores
        List<Tweet> tweets = repositorioTweets.buscarTweetsDeAutores(autoresDelTimeline, limite);

        // Buscar retweets hechos por esos autores
        List<ReTweet> retweets = repositorioRetweets.buscarRetweetsDeAutores(autoresDelTimeline, limite);

        // Combinar tweets y retweets en una lista unificada
        List<TimelineItem> items = new java.util.ArrayList<>();

        // Agregar tweets como TimelineItem
        for (Tweet tweet : tweets) {
            items.add(TimelineItem.deTweet(tweet));
        }

        // Agregar retweets como TimelineItem
        for (ReTweet retweet : retweets) {
            items.add(TimelineItem.deRetweet(retweet));
        }

        // Ordenar por fecha descendente (más recientes primero)
        items.sort((a, b) -> b.getFechaParaOrdenamiento().compareTo(a.getFechaParaOrdenamiento()));

        // Limitar al número solicitado
        if (items.size() > limite) {
            items = items.subList(0, limite);
        }

        return items;
    }

    @Override
    public List<Tweet> obtenerTodosTweets(int limite) {
        // Buscar TODOS los tweets del sistema sin filtrar por autor
        // Solo tweets originales, ordenados por fecha descendente
        return repositorioTweets.buscarTodosTweets(limite);
    }

    @Override
    public List<TimelineItem> tweetsDeUsuario(Long idUsuario, int limite) {
        Usuario usuario = repositorioUsuarios.buscarPorId(idUsuario);

        // Buscar tweets originales del usuario
        List<Tweet> tweets = repositorioTweets.buscarTweetsDeAutores(List.of(usuario), limite);

        // Buscar retweets hechos por el usuario
        List<ReTweet> retweets = repositorioRetweets.buscarRetweetsDeAutores(List.of(usuario), limite);

        // Combinar tweets y retweets en una lista unificada
        List<TimelineItem> items = new java.util.ArrayList<>();

        // Agregar tweets como TimelineItem
        for (Tweet tweet : tweets) {
            items.add(TimelineItem.deTweet(tweet));
        }

        // Agregar retweets como TimelineItem
        for (ReTweet retweet : retweets) {
            items.add(TimelineItem.deRetweet(retweet));
        }

        // Ordenar por fecha descendente (más recientes primero)
        items.sort((a, b) -> b.getFechaParaOrdenamiento().compareTo(a.getFechaParaOrdenamiento()));

        // Limitar al número solicitado
        if (items.size() > limite) {
            items = items.subList(0, limite);
        }

        return items;
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
