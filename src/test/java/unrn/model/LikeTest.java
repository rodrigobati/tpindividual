package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LikeTest {

        @Test
        @DisplayName("Crear like con datos válidos instancia correctamente")
        void crearLike_datosValidos_instanciaCorrecta() {
                Usuario autorTweet = new Usuario("kc-a", "autor", "autor@example.com", LocalDateTime.now(), null, null);
                Usuario autorLike = new Usuario("kc-b", "likero", "likero@example.com", LocalDateTime.now(), null,
                                null);
                Tweet tweet = new Tweet(autorTweet, "Tweet a dar like", LocalDateTime.now(), false);

                Like like = new Like(autorLike, tweet, LocalDateTime.now());

                assertNotNull(like);
                assertTrue(like.esDe(autorLike));
                assertTrue(like.esSobre(tweet));
        }

        @Test
        @DisplayName("Crear like con autor nulo lanza excepción")
        void crearLike_autorNulo_lanzaExcepcion() {
                Tweet tweet = new Tweet(new Usuario("kc-c", "autor", "a@x.com", LocalDateTime.now(), null, null), "T",
                                LocalDateTime.now(), false);

                var ex = assertThrows(RuntimeException.class, () -> new Like(null, tweet, LocalDateTime.now()));
                assertEquals(Like.ERROR_AUTOR_LIKE_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Crear like con tweet nulo lanza excepción")
        void crearLike_tweetNulo_lanzaExcepcion() {
                Usuario autorLike = new Usuario("kc-d", "likero", "l@x.com", LocalDateTime.now(), null, null);

                var ex = assertThrows(RuntimeException.class, () -> new Like(autorLike, null, LocalDateTime.now()));
                assertEquals(Like.ERROR_TWEET_LIKE_OBLIGATORIO, ex.getMessage());
        }

        @Test
        @DisplayName("Like fue creado antes de fecha específica")
        void fueCreadoAntesDe_funciona() {
                Usuario autorTweet = new Usuario("kc-e", "autor", "a@x.com", LocalDateTime.now(), null, null);
                Usuario autorLike = new Usuario("kc-f", "likero", "l@x.com", LocalDateTime.now(), null, null);
                Tweet tweet = new Tweet(autorTweet, "T", LocalDateTime.now(), false);
                LocalDateTime fechaLike = LocalDateTime.of(2025, 1, 1, 10, 0);
                Like like = new Like(autorLike, tweet, fechaLike);

                assertTrue(like.fueCreadoAntesDe(LocalDateTime.of(2025, 12, 1, 10, 0)));
                assertFalse(like.fueCreadoAntesDe(LocalDateTime.of(2024, 12, 1, 10, 0)));
        }

}
