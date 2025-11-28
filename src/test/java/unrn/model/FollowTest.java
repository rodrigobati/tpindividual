package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FollowTest {

        @Test
        @DisplayName("Crear follow con datos válidos instancia correctamente")
        void crearFollow_datosValidos_instanciaCorrecta() {
                Usuario seguidor = new Usuario("kc-s", "seguidor", "s@x.com", LocalDateTime.now(), null, null);
                Usuario seguido = new Usuario("kc-d", "seguido", "d@x.com", LocalDateTime.now(), null, null);

                Follow follow = new Follow(seguidor, seguido, LocalDateTime.now());

                assertNotNull(follow);
                assertTrue(follow.esSeguidor(seguidor));
                assertTrue(follow.esSeguido(seguido));
                assertTrue(follow.esEntre(seguidor, seguido));
        }

        @Test
        @DisplayName("Crear follow con seguidor nulo lanza excepción")
        void crearFollow_seguidorNulo_lanzaExcepcion() {
                Usuario seguido = new Usuario("kc-x", "seguido", "d@x.com", LocalDateTime.now(), null, null);

                var ex = assertThrows(RuntimeException.class, () -> new Follow(null, seguido, LocalDateTime.now()));
                assertEquals(Follow.ERROR_SEGUIDOR_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Seguir a sí mismo lanza excepción")
        void seguir_aSiMismo_lanzaExcepcion() {
                Usuario usuario = new Usuario("kc-self", "usuario", "u@x.com", LocalDateTime.now(), null, null);

                var ex = assertThrows(RuntimeException.class, () -> new Follow(usuario, usuario, LocalDateTime.now()));
                assertEquals(Follow.ERROR_SEGUIR_A_SI_MISMO, ex.getMessage());
        }

}
