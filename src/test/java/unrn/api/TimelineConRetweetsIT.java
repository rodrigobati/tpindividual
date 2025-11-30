package unrn.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;
import unrn.service.ServicioSocial;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración completo para el escenario:
 * - agustingui publica un tweet
 * - niqui hace retweet de ese tweet
 * - patito sigue a niqui
 * → El timeline de patito DEBE mostrar el retweet de niqui
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TimelineConRetweetsIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RepositorioUsuarios repositorioUsuarios;

    @Autowired
    RepositorioTweets repositorioTweets;

    @Autowired
    ServicioSocial servicioSocial;

    @Test
    @DisplayName("El timeline de un usuario muestra retweets de usuarios seguidos")
    void timeline_muestraRetweetsDeSeguidos() throws Exception {
        // Arrange: Crear tres usuarios
        String keycloakIdAgustin = "keycloak-agustin";
        String keycloakIdNiqui = "keycloak-niqui";
        String keycloakIdPatito = "keycloak-patito";

        Usuario agustin = new Usuario(
                keycloakIdAgustin,
                "agustingui",
                "agustin@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(agustin);

        Usuario niqui = new Usuario(
                keycloakIdNiqui,
                "niqui",
                "niqui@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(niqui);

        Usuario patito = new Usuario(
                keycloakIdPatito,
                "patito",
                "patito@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(patito);

        // Step 1: Agustin publica un tweet
        Tweet tweetDeAgustin = new Tweet(
                agustin,
                "Este es el tweet original de Agustin",
                LocalDateTime.now(),
                false);
        repositorioTweets.guardar(tweetDeAgustin);
        Long idTweetAgustin = tweetDeAgustin.id();

        // Step 2: Niqui hace retweet del tweet de Agustin
        mockMvc.perform(post("/api/tweets/" + idTweetAgustin + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdNiqui)
                        .claim("preferred_username", "niqui"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autorRetweet").value("niqui"));

        // Step 3: Patito sigue a Niqui
        mockMvc.perform(post("/api/social/usuarios/" + niqui.id() + "/seguir")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdPatito)
                        .claim("preferred_username", "patito"))))
                .andExpect(status().isOk()); // Act: Obtener el timeline de Patito
        MvcResult result = mockMvc.perform(get("/api/tweets/timeline")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdPatito)
                        .claim("preferred_username", "patito")))
                .param("limite", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("Timeline de patito: " + jsonResponse);

        // Assert: El timeline debe contener el retweet de Niqui
        mockMvc.perform(get("/api/tweets/timeline")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdPatito)
                        .claim("preferred_username", "patito")))
                .param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets").isArray())
                .andExpect(jsonPath("$.tweets[0].esRetweet").value(true))
                .andExpect(jsonPath("$.tweets[0].retweeteadoPor").value("niqui"))
                .andExpect(jsonPath("$.tweets[0].autor").value("agustingui"))
                .andExpect(jsonPath("$.tweets[0].contenido").value("Este es el tweet original de Agustin"));
    }

    @Test
    @DisplayName("El timeline muestra tweets originales y retweets mezclados y ordenados")
    void timeline_muestraTweetsYRetweetsMezclados() throws Exception {
        // Arrange: Crear dos usuarios
        String keycloakIdAutor = "keycloak-autor-mix";
        String keycloakIdSeguidor = "keycloak-seguidor-mix";

        Usuario autor = new Usuario(
                keycloakIdAutor,
                "autorMix",
                "autor@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(autor);

        Usuario seguidor = new Usuario(
                keycloakIdSeguidor,
                "seguidorMix",
                "seguidor@example.com",
                LocalDateTime.now(),
                null,
                null);
        repositorioUsuarios.guardar(seguidor);

        // Autor publica un tweet
        Tweet tweetOriginal = new Tweet(
                autor,
                "Tweet original del autor",
                LocalDateTime.now().minusMinutes(10),
                false);
        repositorioTweets.guardar(tweetOriginal);

        // Autor publica otro tweet
        Tweet tweetOriginal2 = new Tweet(
                autor,
                "Segundo tweet del autor",
                LocalDateTime.now().minusMinutes(5),
                false);
        repositorioTweets.guardar(tweetOriginal2);

        // Seguidor hace retweet del primer tweet
        mockMvc.perform(post("/api/tweets/" + tweetOriginal.id() + "/retweets")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdSeguidor)
                        .claim("preferred_username", "seguidorMix"))))
                .andExpect(status().isOk());

        // Seguidor sigue al autor
        mockMvc.perform(post("/api/social/usuarios/" + autor.id() + "/seguir")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdSeguidor)
                        .claim("preferred_username", "seguidorMix"))))
                .andExpect(status().isOk()); // Act & Assert: Timeline del seguidor debe mostrar:
        // - Su propio retweet (más reciente)
        // - Los dos tweets del autor (ordenados por fecha)
        mockMvc.perform(get("/api/tweets/timeline")
                .with(jwt().jwt(jwt -> jwt
                        .subject(keycloakIdSeguidor)
                        .claim("preferred_username", "seguidorMix")))
                .param("limite", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweets").isArray())
                .andExpect(jsonPath("$.tweets.length()").value(3)); // 1 retweet + 2 tweets originales
    }
}
