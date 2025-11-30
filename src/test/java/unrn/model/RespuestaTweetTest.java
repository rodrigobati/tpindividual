package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RespuestaTweetTest {

        @Test
        @DisplayName("Crear respuesta con datos válidos instancia correctamente")
        void crearRespuesta_datosValidos_instanciaCorrecta() {
                Usuario autorTweet = new Usuario("kc-o", "original", "o@x.com", LocalDateTime.now(), null, null);
                Usuario autorRespuesta = new Usuario("kc-r", "respondedor", "r@x.com", LocalDateTime.now(), null, null);
                Tweet tweetOriginal = new Tweet(autorTweet, "Tweet original", LocalDateTime.now(), false);

                RespuestaTweet respuesta = new RespuestaTweet(autorRespuesta, tweetOriginal, "Esta es mi respuesta",
                                LocalDateTime.now(), false);

                assertNotNull(respuesta);
                assertTrue(respuesta.esDe(autorRespuesta));
                assertTrue(respuesta.respondeA(tweetOriginal));
                assertFalse(respuesta.estaEliminado());
        }

        @Test
        @DisplayName("Crear respuesta con autor nulo lanza excepción")
        void crearRespuesta_autorNulo_lanzaExcepcion() {
                Tweet tweetOriginal = new Tweet(
                                new Usuario("kc-x", "autor", "a@x.com", LocalDateTime.now(), null, null), "T",
                                LocalDateTime.now(), false);

                var ex = assertThrows(RuntimeException.class,
                                () -> new RespuestaTweet(null, tweetOriginal, "R", LocalDateTime.now(), false));
                assertEquals(RespuestaTweet.ERROR_AUTOR_RESPUESTA_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Crear respuesta con contenido nulo o vacío lanza excepción")
        void crearRespuesta_contenidoInvalido_lanzaExcepcion() {
                Usuario autorRespuesta = new Usuario("kc-y", "respondedor", "r@x.com", LocalDateTime.now(), null, null);
                Tweet tweetOriginal = new Tweet(
                                new Usuario("kc-z", "autor", "a@x.com", LocalDateTime.now(), null, null), "T",
                                LocalDateTime.now(), false);

                var ex1 = assertThrows(RuntimeException.class, () -> new RespuestaTweet(autorRespuesta, tweetOriginal,
                                null, LocalDateTime.now(), false));
                assertEquals(RespuestaTweet.ERROR_CONTENIDO_RESPUESTA_OBLIGATORIO, ex1.getMessage());

                var ex2 = assertThrows(RuntimeException.class, () -> new RespuestaTweet(autorRespuesta, tweetOriginal,
                                "   ", LocalDateTime.now(), false));
                assertEquals(RespuestaTweet.ERROR_CONTENIDO_RESPUESTA_OBLIGATORIO, ex2.getMessage());
        }

        @Test
        @DisplayName("Eliminar respuesta cambia su estado a eliminado")
        void eliminar_respuestaActiva_cambiaEstado() {
                Usuario autorTweet = new Usuario("kc-a1", "autor_tweet1", "a@x.com", LocalDateTime.now(), null, null);
                Usuario autorRespuesta = new Usuario("kc-b1", "autor_resp1", "r@x.com", LocalDateTime.now(), null,
                                null);
                Tweet tweetOriginal = new Tweet(autorTweet, "T", LocalDateTime.now(), false);
                RespuestaTweet respuesta = new RespuestaTweet(autorRespuesta, tweetOriginal, "Respuesta",
                                LocalDateTime.now(), false);

                assertFalse(respuesta.estaEliminado());
                respuesta.eliminar();
                assertTrue(respuesta.estaEliminado());
        }

}
