package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import unrn.api.dto.*;
import unrn.model.Like;
import unrn.model.RespuestaTweet;
import unrn.model.Tweet;
import unrn.service.ServicioTweets;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tweets")
public class TweetsController {

    private final ServicioTweets servicioTweets;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TweetsController(ServicioTweets servicioTweets) {
        this.servicioTweets = servicioTweets;
    }

    // ----------- Endpoints -----------

    @PostMapping
    public TweetResponse publicarTweet(@AuthenticationPrincipal Jwt jwt,
            @RequestBody PublicarTweetRequest request) {

        String keycloakId = jwt.getSubject();
        Tweet tweet = servicioTweets.publicarTweet(keycloakId, request.contenido());
        return toTweetResponse(tweet);
    }

    @PostMapping("/{idTweet}/retweets")
    public void retweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        String keycloakId = jwt.getSubject();
        servicioTweets.retweetear(keycloakId, idTweet);
    }

    @PostMapping("/{idTweet}/respuestas")
    public RespuestaTweetResponse responder(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet,
            @RequestBody ResponderTweetRequest request) {

        String keycloakId = jwt.getSubject();
        RespuestaTweet respuesta = servicioTweets.responderATweet(keycloakId, idTweet, request.contenido());
        return toRespuestaTweetResponse(respuesta);
    }

    @DeleteMapping("/{idTweet}")
    public void eliminarTweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarTweet(keycloakId, idTweet);
    }

    @DeleteMapping("/respuestas/{idRespuesta}")
    public void eliminarRespuesta(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idRespuesta) {

        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarRespuesta(keycloakId, idRespuesta);
    }

    @PostMapping("/{idTweet}/likes")
    public void darLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        String keycloakId = jwt.getSubject();
        servicioTweets.darLike(keycloakId, idTweet);
    }

    @DeleteMapping("/{idTweet}/likes")
    public void quitarLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        String keycloakId = jwt.getSubject();
        servicioTweets.quitarLike(keycloakId, idTweet);
    }

    @GetMapping("/timeline")
    public TimelineResponse timeline(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "50") int limite) {

        String keycloakId = jwt.getSubject();
        List<Tweet> tweets = servicioTweets.timeline(keycloakId, limite);
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
                tweet.estaEliminado());
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
}
