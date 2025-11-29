package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import unrn.api.dto.*;
import unrn.model.Like;
import unrn.model.RespuestaTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioUsuarios;
import unrn.service.ServicioTweets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tweets")
public class TweetsController {

    private final ServicioTweets servicioTweets;
    private final RepositorioUsuarios repositorioUsuarios;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public TweetsController(ServicioTweets servicioTweets, RepositorioUsuarios repositorioUsuarios) {
        this.servicioTweets = servicioTweets;
        this.repositorioUsuarios = repositorioUsuarios;
    }

    // ----------- Endpoints -----------

    @PostMapping
    public TweetResponse publicarTweet(@AuthenticationPrincipal Jwt jwt,
            @RequestBody PublicarTweetRequest request) {

        String keycloakId = jwt.getSubject();

        // Asegurar que el usuario existe en la base de datos
        asegurarUsuarioExiste(jwt);

        Tweet tweet = servicioTweets.publicarTweet(keycloakId, request.contenido());
        return toTweetResponse(tweet);
    }

    @PostMapping("/{idTweet}/retweets")
    public void retweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.retweetear(keycloakId, idTweet);
    }

    @PostMapping("/{idTweet}/respuestas")
    public RespuestaTweetResponse responder(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet,
            @RequestBody ResponderTweetRequest request) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        RespuestaTweet respuesta = servicioTweets.responderATweet(keycloakId, idTweet, request.contenido());
        return toRespuestaTweetResponse(respuesta);
    }

    @DeleteMapping("/{idTweet}")
    public void eliminarTweet(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarTweet(keycloakId, idTweet);
    }

    @DeleteMapping("/respuestas/{idRespuesta}")
    public void eliminarRespuesta(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idRespuesta) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.eliminarRespuesta(keycloakId, idRespuesta);
    }

    @PostMapping("/{idTweet}/likes")
    public void darLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.darLike(keycloakId, idTweet);
    }

    @DeleteMapping("/{idTweet}/likes")
    public void quitarLike(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idTweet) {

        asegurarUsuarioExiste(jwt);
        String keycloakId = jwt.getSubject();
        servicioTweets.quitarLike(keycloakId, idTweet);
    }

    @GetMapping("/timeline")
    public TimelineResponse timeline(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "50") int limite) {

        asegurarUsuarioExiste(jwt);
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

    // ----------- Gestión automática de usuarios -----------

    /**
     * Asegura que el usuario autenticado existe en la base de datos.
     * Si no existe, lo crea automáticamente con datos del JWT.
     */
    private void asegurarUsuarioExiste(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        // Si ya existe, no hacer nada
        if (repositorioUsuarios.existePorKeycloakId(keycloakId)) {
            return;
        }

        // Extraer información del JWT
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        // Si no hay email en el token, usar username@minitwitter.local
        if (email == null || email.isBlank()) {
            email = username + "@minitwitter.local";
        }

        // Crear el usuario
        Usuario nuevoUsuario = new Usuario(
                keycloakId,
                username,
                email,
                LocalDateTime.now(),
                null, // biografía vacía
                null // avatar vacío
        );

        repositorioUsuarios.guardar(nuevoUsuario);
    }
}
