package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

        @Test
        @DisplayName("Usuario constructor crea instancia valida")
        void constructor_usuario_valido() {
                // Setup
                var fecha = LocalDateTime.now();

                // Ejercitación
                var u = new Usuario("kc-1", "rodrigo", "r@x.com", fecha, "bio", "avatar");

                // Verificación
                assertEquals("rodrigo", u.nombreUsuario(), "El nombre de usuario debe conservarse");
                assertEquals("kc-1", u.keycloakId(), "El keycloakId debe conservarse");
                assertTrue(u.estaActivo(), "El usuario nuevo debe estar activo por defecto");
        }

        @Test
        @DisplayName("Usuario constructor lanza si keycloakId nulo")
        void constructor_keycloakId_nulo_lanza() {
                // Setup
                var fecha = LocalDateTime.now();

                // Ejercitación & Verificación
                var ex = assertThrows(RuntimeException.class,
                                () -> new Usuario(null, "rodrigo", "r@x.com", fecha, "bio", "avatar"));
                assertEquals(Usuario.ERROR_KEYCLOAK_ID_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Usuario puede publicar tweet con contenido valido")
        void publicarTweet_creaTweet_valido() {
                // Setup
                var u = new Usuario("kc-2", "ana", "a@x.com", LocalDateTime.now(), "bio", null);

                // Ejercitación
                var tweet = u.publicarTweet("Hola mundo");

                // Verificación
                assertEquals("Hola mundo", tweet.contenido());
                assertTrue(tweet.esDe(u));
                assertFalse(tweet.estaEliminado());
        }

}