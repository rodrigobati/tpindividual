package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import unrn.api.dto.*;
import unrn.model.Like;
import unrn.model.ReTweet;
import unrn.model.RespuestaTweet;
import unrn.model.Tweet;
import unrn.service.ServicioTweets;
import unrn.service.ServicioUsuarios;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tweets")
public class TweetsController {

    private final ServicioTweets servicioTweets;
    private final ServicioUsuarios servicioUsuarios;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TweetsController(ServicioTweets servicioTweets, ServicioUsuarios servicioUsuarios) {
        this.servicioTweets = servicioTweets;
        this.servicioUsuarios = servicioUsuarios;
    }

    // ----------- Endpoints -----------

    @PostMapping
    public TweetResponse publicarTweet(@AuthenticationPrincipal Jwt jwt,
            @RequestBody PublicarTweetRequest request) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();

        Tweet tweet = servicioTweets.publicarTweet(keycloakId, request.contenido());
        return toTweetResponse(tweet);
    }

    @PostMapping("/{idTweet}/retweets")
    public RetweetResponse retweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        ReTweet retweet = servicioTweets.retweetear(keycloakId, idTweet);
        return toRetweetResponse(retweet);
    }

    @PostMapping("/{idTweet}/respuestas")
    public RespuestaTweetResponse responder(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet,
            @RequestBody ResponderTweetRequest request) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        RespuestaTweet respuesta = servicioTweets.responderATweet(keycloakId, idTweet, request.contenido());
        return toRespuestaTweetResponse(respuesta);
    }

    @DeleteMapping("/{idTweet}")
    public void eliminarTweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarTweet(keycloakId, idTweet);
    }

    @DeleteMapping("/respuestas/{idRespuesta}")
    public void eliminarRespuesta(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idRespuesta) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarRespuesta(keycloakId, idRespuesta);
    }

    @PostMapping("/{idTweet}/likes")
    public void darLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.darLike(keycloakId, idTweet);
    }

    @DeleteMapping("/{idTweet}/likes")
    public void quitarLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.quitarLike(keycloakId, idTweet);
    }

    /**
     * Obtiene el timeline personalizado del usuario autenticado
     * (tweets de personas que sigue + retweets)
     */
    @GetMapping("/timeline")
    public TimelineResponse timeline(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "50") int limite) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        List<unrn.service.TimelineItem> items = servicioTweets.timeline(keycloakId, limite);

        List<TweetResponse> respuesta = items.stream()
                .map(this::timelineItemToTweetResponse)
                .toList();
        return new TimelineResponse(respuesta);
    }

    /**
     * Obtiene TODOS los tweets del sistema (sin filtrar por seguimiento)
     * Usado para la vista "Ver todos" en Home Page
     * Solo devuelve tweets originales, sin retweets
     */
    @GetMapping
    public TimelineResponse todosTweets(@RequestParam(defaultValue = "100") int limite) {
        List<Tweet> tweets = servicioTweets.obtenerTodosTweets(limite);

        List<TweetResponse> respuesta = tweets.stream()
                .map(this::toTweetResponse)
                .toList();
        return new TimelineResponse(respuesta);
    }

    @GetMapping("/{idTweet}/respuestas")
    public List<RespuestaTweetResponse> respuestas(@PathVariable Long idTweet) {
        List<RespuestaTweet> respuestas = servicioTweets.respuestasDeTweet(idTweet);
        return respuestas.stream()
                .map(this::toRespuestaTweetResponse)
                .toList();
    }

    @GetMapping("/{idTweet}/likes")
    public List<LikeResponse> likes(@PathVariable Long idTweet) {
        List<Like> likes = servicioTweets.likesDeTweet(idTweet);
        return likes.stream()
                .map(this::toLikeResponse)
                .toList();
    }

    // ----------- Mapeo dominio -> DTO -----------

    /**
     * Convierte un TimelineItem (puede ser tweet o retweet) a TweetResponse.
     * Si es retweet, marca esRetweet=true y retweeteadoPor con el autor del
     * retweet.
     */
    private TweetResponse timelineItemToTweetResponse(unrn.service.TimelineItem item) {
        Tweet tweetOriginal = item.getTweetOriginal();
        String autor = tweetOriginal.autor().nombreUsuario();
        String contenido = tweetOriginal.contenido();
        String fecha = tweetOriginal.fechaCreacion().format(formatter);

        if (item.esRetweet()) {
            // Es un retweet
            String retweeteadoPor = item.getRetweet().autor().nombreUsuario();
            return new TweetResponse(
                    tweetOriginal.id(),
                    autor, // Autor del tweet original
                    contenido,
                    fecha,
                    tweetOriginal.estaEliminado(),
                    true, // esRetweet
                    retweeteadoPor); // quien hizo el retweet
        } else {
            // Es un tweet original
            return new TweetResponse(
                    tweetOriginal.id(),
                    autor,
                    contenido,
                    fecha,
                    tweetOriginal.estaEliminado(),
                    false, // no es retweet
                    null);
        }
    }

    private TweetResponse toTweetResponse(Tweet tweet) {
        // Asumo que tu modelo expone estos métodos de lectura:
        String autor = tweet.autor().nombreUsuario();
        String contenido = tweet.contenido();
        String fecha = tweet.fechaCreacion().format(formatter);

        return new TweetResponse(
                tweet.id(), // idem: método de lectura o getter simple
                autor,
                contenido,
                fecha,
                tweet.estaEliminado(),
                false, // esRetweet: por defecto false (para tweets originales)
                null); // retweeteadoPor: null para tweets originales
    }

    private RespuestaTweetResponse toRespuestaTweetResponse(RespuestaTweet respuesta) {
        String autor = respuesta.autor().nombreUsuario();
        String contenido = respuesta.contenido();
        String fecha = respuesta.fechaCreacion().format(formatter);

        return new RespuestaTweetResponse(
                respuesta.id(),
                autor,
                contenido,
                fecha,
                respuesta.estaEliminado());
    }

    private LikeResponse toLikeResponse(Like like) {
        String autor = like.autor().nombreUsuario();
        String fecha = like.fechaCreacion().format(formatter);

        return new LikeResponse(
                like.id(),
                autor,
                fecha);
    }

    private RetweetResponse toRetweetResponse(unrn.model.ReTweet retweet) {
        String autorRetweet = retweet.autor().nombreUsuario();
        String fechaRetweet = retweet.fechaCreacion().format(formatter);
        TweetResponse tweetOriginal = toTweetResponse(retweet.original());

        return new RetweetResponse(
                retweet.id(),
                autorRetweet,
                tweetOriginal,
                fechaRetweet);
    }
}
