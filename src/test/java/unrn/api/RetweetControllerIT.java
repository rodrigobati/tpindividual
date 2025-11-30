package unrn.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests para la funcionalidad de retweet.
 * Verifica el endpoint POST /api/tweets/{id}/retweets con diferentes
 * escenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RetweetControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RepositorioUsuarios repositorioUsuarios;

    @Autowired
    RepositorioTweets repositorioTweets;

    @Test
    @DisplayName("POST /api/tweets/{id}/retweets retorna 200 con RetweetResponse cuando es exitoso")
    void retweet_conUsuarioYTweetValidos_retorna200ConRetweetResponse() throws Exception {
        // Arrange: crear dos usuarios y un tweet
        String keycloakIdAutor = "keycloak-autor-rt-1";
        String keycloakIdRetweeter = "keycloak-retweeter-rt-1";

        Usuario autor = new Usuario(
                keycloakIdAutor,
                "autorOriginal",
                "autor@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(autor);

        Usuario retweeter = new Usuario(
                keycloakIdRetweeter,
                "retweeterUser",
                "retweeter@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(retweeter);

        // Crear el tweet original
        Tweet tweetOriginal = new Tweet(
                autor,
                "Este es el tweet original a retwittear",
                LocalDateTime.now(),
                false);
        repositorioTweets.guardar(tweetOriginal);
        Long idTweet = tweetOriginal.id();

        // Act & Assert: hacer el retweet
        mockMvc.perform(post("/api/tweets/" + idTweet + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdRetweeter)
                        .claim("preferred_username", "retweeterUser"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.autorRetweet").value("retweeterUser"))
                .andExpect(jsonPath("$.fechaRetweet").exists())
                .andExpect(jsonPath("$.tweetOriginal").exists())
                .andExpect(jsonPath("$.tweetOriginal.id").value(idTweet))
                .andExpect(jsonPath("$.tweetOriginal.autor").value("autorOriginal"))
                .andExpect(jsonPath("$.tweetOriginal.contenido")
                        .value("Este es el tweet original a retwittear"));
    }

    @Test
    @DisplayName("POST /api/tweets/{id}/retweets es idempotente: retorna mismo retweet si ya existe")
    void retweet_duplicado_esIdempotente() throws Exception {
        // Arrange
        String keycloakIdAutor = "keycloak-autor-rt-2";
        String keycloakIdRetweeter = "keycloak-retweeter-rt-2";

        Usuario autor = new Usuario(
                keycloakIdAutor,
                "autorTweet",
                "autor2@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(autor);

        Usuario retweeter = new Usuario(
                keycloakIdRetweeter,
                "retweeterDuplicado",
                "retweeter2@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(retweeter);

        Tweet tweet = new Tweet(
                autor,
                "Tweet para test de idempotencia",
                LocalDateTime.now(),
                false);
        repositorioTweets.guardar(tweet);
        Long idTweet = tweet.id();

        // Act: hacer retweet por primera vez
        var primerRetweet = mockMvc.perform(post("/api/tweets/" + idTweet + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdRetweeter)
                        .claim("preferred_username", "retweeterDuplicado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String primerRetweetJson = primerRetweet.getResponse().getContentAsString();
        Long primerRetweetId = objectMapper.readTree(primerRetweetJson).get("id").asLong();

        // Act: hacer retweet por segunda vez (duplicado)
        mockMvc.perform(post("/api/tweets/" + idTweet + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdRetweeter)
                        .claim("preferred_username", "retweeterDuplicado"))))
                // Assert: debe retornar el mismo ID (idempotencia)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(primerRetweetId))
                .andExpect(jsonPath("$.autorRetweet").value("retweeterDuplicado"));
    }

    @Test
    @DisplayName("POST /api/tweets/{id}/retweets sin JWT retorna 401 Unauthorized")
    void retweet_sinAutenticacion_retorna401() throws Exception {
        // Arrange
        String keycloakIdAutor = "keycloak-autor-rt-3";
        Usuario autor = new Usuario(
                keycloakIdAutor,
                "autorSinAuth",
                "autor3@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(autor);

        Tweet tweet = new Tweet(
                autor,
                "Tweet para test sin autenticación",
                LocalDateTime.now(),
                false);
        repositorioTweets.guardar(tweet);
        Long idTweet = tweet.id();

        // Act & Assert: intentar retweet sin JWT
        mockMvc.perform(post("/api/tweets/" + idTweet + "/retweets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/tweets/{id}/retweets permite retweet de tweet propio")
    void retweet_deTweetPropio_permitido() throws Exception {
        // Arrange
        String keycloakId = "keycloak-user-rt-4";
        Usuario usuario = new Usuario(
                keycloakId,
                "usuarioAutorretweet",
                "autor4@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(usuario);

        Tweet tweetPropio = new Tweet(
                usuario,
                "Mi propio tweet que voy a retwittear",
                LocalDateTime.now(),
                false);
        repositorioTweets.guardar(tweetPropio);
        Long idTweet = tweetPropio.id();

        // Act & Assert: hacer retweet de mi propio tweet
        mockMvc.perform(post("/api/tweets/" + idTweet + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakId)
                        .claim("preferred_username", "usuarioAutorretweet"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autorRetweet").value("usuarioAutorretweet"))
                .andExpect(jsonPath("$.tweetOriginal.autor").value("usuarioAutorretweet"));
    }

    @Test
    @DisplayName("POST /api/tweets/{id}/retweets con tweet inexistente retorna 400 Bad Request")
    void retweet_tweetInexistente_retorna400() throws Exception {
        // Arrange
        String keycloakId = "keycloak-user-rt-5";
        Usuario usuario = new Usuario(
                keycloakId,
                "usuarioTest",
                "test@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(usuario);

        Long idTweetInexistente = 999999L;

        // Act & Assert
        mockMvc.perform(post("/api/tweets/" + idTweetInexistente + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakId)
                        .claim("preferred_username", "usuarioTest"))))
                .andExpect(status().isBadRequest());
    }
}