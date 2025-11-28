package unrn.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import unrn.model.Usuario;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RepositorioUsuarios repositorioUsuarios;

    @Test
    void seguir_y_listar_seguidos() throws Exception {
        // arrange: usuario actual (desde Keycloak) y otro usuario a seguir
        String keycloakIdSeguidor = "keycloak-user-abc";
        Usuario seguidor = new Usuario(
                keycloakIdSeguidor,
                "juan",
                "juan@example.com",
                LocalDateTime.now(),
                "bio juan",
                null);
        repositorioUsuarios.guardar(seguidor);

        Usuario seguido = new Usuario(
                "otro-keycloak",
                "pedro",
                "pedro@example.com",
                LocalDateTime.now(),
                "bio pedro",
                null);
        seguido = repositorioUsuarios.guardar(seguido);

        // act: POST /api/social/usuarios/{idSeguido}/seguir
        mockMvc.perform(post("/api/social/usuarios/{idSeguido}/seguir", seguido.id())
                .with(jwt().jwt(jwt -> jwt.subject(keycloakIdSeguidor))))
                .andExpect(status().isOk());

        // assert: GET /api/social/seguidos incluye a "pedro"
        mockMvc.perform(get("/api/social/seguidos")
                .with(jwt().jwt(jwt -> jwt.subject(keycloakIdSeguidor))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("pedro"));
    }

    @Test
    void dejar_de_seguir_quita_el_usuario_de_los_seguidos() throws Exception {
        String keycloakIdSeguidor = "keycloak-user-def";
        Usuario seguidor = new Usuario(
                keycloakIdSeguidor,
                "maria",
                "maria@example.com",
                LocalDateTime.now(),
                "bio maria",
                null);
        repositorioUsuarios.guardar(seguidor);

        Usuario seguido = new Usuario(
                "otro-keycloak-2",
                "lucas",
                "lucas@example.com",
                LocalDateTime.now(),
                "bio lucas",
                null);
        seguido = repositorioUsuarios.guardar(seguido);

        // primero seguir
        mockMvc.perform(post("/api/social/usuarios/{idSeguido}/seguir", seguido.id())
                .with(jwt().jwt(jwt -> jwt.subject(keycloakIdSeguidor))))
                .andExpect(status().isOk());

        // luego dejar de seguir
        mockMvc.perform(delete("/api/social/usuarios/{idSeguido}/seguir", seguido.id())
                .with(jwt().jwt(jwt -> jwt.subject(keycloakIdSeguidor))))
                .andExpect(status().isOk());

        // y verificar que la lista de seguidos queda vacía
        mockMvc.perform(get("/api/social/seguidos")
                .with(jwt().jwt(jwt -> jwt.subject(keycloakIdSeguidor))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
