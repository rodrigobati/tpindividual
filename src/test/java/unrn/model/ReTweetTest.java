package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReTweetTest {

        @Test
        @DisplayName("Crear retweet con datos válidos instancia correctamente")
        void crearReTweet_datosValidos_instanciaCorrecta() {
                Usuario autor = new Usuario("kc-a", "usuario1", "u1@example.com", LocalDateTime.now(), null, null);
                Usuario retweeter = new Usuario("kc-b", "usuario2", "u2@example.com", LocalDateTime.now(), null, null);
                Tweet tweetOriginal = new Tweet(autor, "Mensaje original", LocalDateTime.now(), false);

                ReTweet reTweet = new ReTweet(retweeter, tweetOriginal, LocalDateTime.now());

                assertNotNull(reTweet);
                assertTrue(reTweet.esDe(retweeter));
                assertTrue(reTweet.esSobre(tweetOriginal));
        }

        @Test
        @DisplayName("Crear retweet con autor nulo lanza excepción")
        void crearReTweet_autorNulo_lanzaExcepcion() {
                Tweet original = new Tweet(
                                new Usuario("kc-c", "autor_tweet", "a@x.com", LocalDateTime.now(), null, null), "x",
                                LocalDateTime.now(), false);

                var ex = assertThrows(RuntimeException.class, () -> new ReTweet(null, original, LocalDateTime.now()));
                assertEquals(ReTweet.ERROR_AUTOR_RETWEET_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Crear retweet con tweet original nulo lanza excepción")
        void crearReTweet_tweetOriginalNulo_lanzaExcepcion() {
                Usuario retweeter = new Usuario("kc-d", "retweeter1", "r@x.com", LocalDateTime.now(), null, null);

                var ex = assertThrows(RuntimeException.class, () -> new ReTweet(retweeter, null, LocalDateTime.now()));
                assertEquals(ReTweet.ERROR_TWEET_ORIGINAL_OBLIGATORIO, ex.getMessage());
        }

}
