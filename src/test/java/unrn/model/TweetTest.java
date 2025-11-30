package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TweetTest {

        @Test
        @DisplayName("Crear tweet con datos válidos instancia correctamente")
        void crearTweet_datosValidos_instanciaCorrecta() {
                // Setup
                Usuario autor = new Usuario("kc-a", "juanito", "juan@example.com", LocalDateTime.now(), null, null);
                String contenido = "Mi primer tweet";
                LocalDateTime fecha = LocalDateTime.now();

                // Ejercitación
                Tweet tweet = new Tweet(autor, contenido, fecha, false);

                // Verificación
                assertNotNull(tweet);
                assertTrue(tweet.esDe(autor));
                assertFalse(tweet.estaEliminado());
        }

        @Test
        @DisplayName("Crear tweet con autor nulo lanza excepción")
        void crearTweet_autorNulo_lanzaExcepcion() {
                // Ejercitación & Verificación
                var ex = assertThrows(RuntimeException.class,
                                () -> new Tweet(null, "contenido", LocalDateTime.now(), false));
                assertEquals(Tweet.ERROR_AUTOR_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Crear tweet con contenido inválido lanza excepción")
        void crearTweet_contenidoInvalido_lanzaExcepcion() {
                Usuario autor = new Usuario("kc-a", "autor_test", "a@example.com", LocalDateTime.now(), null, null);

                var ex1 = assertThrows(RuntimeException.class,
                                () -> new Tweet(autor, null, LocalDateTime.now(), false));
                assertEquals(Tweet.ERROR_CONTENIDO_OBLIGATORIO, ex1.getMessage());

                var ex2 = assertThrows(RuntimeException.class,
                                () -> new Tweet(autor, "   ", LocalDateTime.now(), false));
                assertEquals(Tweet.ERROR_CONTENIDO_OBLIGATORIO, ex2.getMessage());
        }

        @Test
        @DisplayName("Crear tweet con contenido mayor a 280 caracteres lanza excepción")
        void crearTweet_contenidoMayorA280_lanzaExcepcion() {
                Usuario autor = new Usuario("kc-c", "maxuser", "max@example.com", LocalDateTime.now(), null, null);
                String contenido = "a".repeat(281);

                var ex = assertThrows(RuntimeException.class,
                                () -> new Tweet(autor, contenido, LocalDateTime.now(), false));
                assertEquals(Tweet.ERROR_CONTENIDO_LARGO, ex.getMessage());
        }

        @Test
        @DisplayName("Eliminar tweet cambia su estado a eliminado")
        void eliminar_tweetActivo_cambiaEstado() {
                Usuario autor = new Usuario("kc-d", "carlos", "carlos@example.com", LocalDateTime.now(), null, null);
                Tweet tweet = new Tweet(autor, "a", LocalDateTime.now(), false);
                assertFalse(tweet.estaEliminado());

                tweet.eliminar();

                assertTrue(tweet.estaEliminado());
        }

}