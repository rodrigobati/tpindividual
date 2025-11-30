package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unrn.api.dto.TimelineResponse;
import unrn.api.dto.TweetResponse;
import unrn.api.dto.UsuarioResponse;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioUsuarios;
import unrn.service.ServicioTweets;
import unrn.service.ServicioUsuarios;
import unrn.service.TimelineItem;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller para operaciones generales de usuarios
 * 
 * Expone endpoints relacionados con la gestión de usuarios del sistema,
 * como listar todos los usuarios disponibles.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {

    private final RepositorioUsuarios repositorioUsuarios;
    private final ServicioTweets servicioTweets;
    private final ServicioUsuarios servicioUsuarios;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public UsuariosController(RepositorioUsuarios repositorioUsuarios,
            ServicioTweets servicioTweets,
            ServicioUsuarios servicioUsuarios) {
        this.repositorioUsuarios = repositorioUsuarios;
        this.servicioTweets = servicioTweets;
        this.servicioUsuarios = servicioUsuarios;
    }

    /**
     * Obtiene la lista de todos los usuarios del sistema.
     * 
     * Este endpoint:
     * - Asegura que el usuario autenticado existe (creándolo si es necesario).
     * - Retorna TODOS los usuarios del sistema (incluido el usuario actual).
     * - El frontend puede filtrar al usuario actual si lo desea usando su username.
     * 
     * @param jwt Token JWT del usuario autenticado
     * @return Lista de todos los usuarios con su información básica
     */
    @GetMapping
    public List<UsuarioResponse> listarUsuarios(@AuthenticationPrincipal Jwt jwt) {
        servicioUsuarios.asegurarUsuarioExiste(jwt);

        // Obtener todos los usuarios
        List<Usuario> usuarios = repositorioUsuarios.listarTodos();

        // Mapear a DTO
        return usuarios.stream()
                .map(this::toUsuarioResponse)
                .toList();
    }

    /**
     * Obtiene todos los tweets y retweets de un usuario específico.
     * 
     * @param jwt       Token JWT del usuario autenticado
     * @param idUsuario ID del usuario cuyos tweets se quieren obtener
     * @param limite    Cantidad máxima de tweets a retornar (default: 50)
     * @return Timeline con tweets y retweets del usuario
     */
    @GetMapping("/{idUsuario}/tweets")
    public TimelineResponse tweetsDeUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idUsuario,
            @RequestParam(defaultValue = "50") int limite) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);

        // Obtener tweets y retweets del usuario
        List<TimelineItem> items = servicioTweets.tweetsDeUsuario(idUsuario, limite);

        // Mapear a TweetResponse
        List<TweetResponse> respuesta = items.stream()
                .map(this::timelineItemToTweetResponse)
                .toList();

        return new TimelineResponse(respuesta);
    }

    // ----------- Mapeo dominio -> DTO -----------

    /**
     * Convierte un TimelineItem (puede ser tweet o retweet) a TweetResponse.
     */
    private TweetResponse timelineItemToTweetResponse(TimelineItem item) {
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

    private UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.id(),
                usuario.nombreUsuario(),
                usuario.avatarUrl());
    }
}
