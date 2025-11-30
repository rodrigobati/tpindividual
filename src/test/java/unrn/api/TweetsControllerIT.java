package unrn.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unrn.api.dto.PublicarTweetRequest;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TweetsControllerIT {

        @Autowired
        MockMvc mockMvc;

        @Autowired
        ObjectMapper objectMapper;

        @Autowired
        RepositorioUsuarios repositorioUsuarios;

        @Autowired
        RepositorioTweets repositorioTweets;

        @Test
        void publica_un_tweet_y_aparece_en_el_timeline() throws Exception {
                // arrange: creo un usuario en la BD de test con un keycloakId conocido
                String keycloakId = "keycloak-user-123";

                Usuario usuario = new Usuario(
                                keycloakId,
                                "rodrigo",
                                "rodrigo@example.com",
                                LocalDateTime.now(),
                                "bio",
                                null);
                repositorioUsuarios.guardar(usuario);

                // act: invoco al endpoint POST /api/tweets con un JWT que tiene ese keycloakId
                var request = new PublicarTweetRequest("hola mini twitter");

                mockMvc.perform(post("/api/tweets")
                                .with(jwt().jwt(jwt -> jwt
                                                .subject(keycloakId)
                                                .claim("preferred_username", "rodrigo")))
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                // assert: verifica que responde 200 y que la estructura básica es la esperada
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.autor").value("rodrigo"))
                                .andExpect(jsonPath("$.contenido").value("hola mini twitter"))
                                .andExpect(jsonPath("$.eliminado").value(false));

                // act + assert: pido el timeline, y debería venir al menos ese tweet
                mockMvc.perform(get("/api/tweets/timeline")
                                .with(jwt().jwt(jwt -> jwt.subject(keycloakId)))
                                .param("limite", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.tweets[0].autor").value("rodrigo"))
                                .andExpect(jsonPath("$.tweets[0].contenido").value("hola mini twitter"));
        }
}
