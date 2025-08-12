package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TweetTest {

        @Test
        @DisplayName("Tweet con mensaje de 1 carácter se instancia correctamente")
        void tweet_mensajeMinimo_instanciaCorrecta() {
                // Setup: Preparar el escenario
                Usuario autor = new Usuario("Juanito");
                String mensaje = "a";
                // Ejercitación: Ejecutar la acción a probar
                Tweet tweet = new Tweet(mensaje, autor);
                // Verificación: Verificar el resultado esperado
                assertNotNull(tweet, "El tweet debería haberse creado correctamente");
                assertEquals(mensaje, tweet.mensaje(), "El mensaje del tweet debe coincidir con el esperado");
        }

        @Test
        @DisplayName("Tweet con mensaje de 280 caracteres se instancia correctamente")
        void tweet_mensajeMaximo_instanciaCorrecta() {
                // Setup: Preparar el escenario
                Usuario autor = new Usuario("MaxCharUser");
                String mensaje = "a".repeat(280);
                // Ejercitación: Ejecutar la acción a probar
                Tweet tweet = new Tweet(mensaje, autor);
                // Verificación: Verificar el resultado esperado
                assertNotNull(tweet, "El tweet debería haberse creado correctamente");
                assertEquals(mensaje, tweet.mensaje(), "El mensaje del tweet debe coincidir con el esperado");
        }

        @Test
        @DisplayName("Tweet con mensaje vacío lanza excepción")
        void tweet_mensajeVacio_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario autor = new Usuario("AnaMin");
                String mensaje = "";
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Tweet(mensaje, autor),
                                "Crear tweet con mensaje vacío debe lanzar excepción");
                assertEquals(Tweet.ERROR_MENSAJE_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Tweet con mensaje menor a 1 carácter lanza excepción")
        void tweet_mensajeMenorMinimo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario autor = new Usuario("ShortUser");
                String mensaje = "";
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Tweet(mensaje, autor),
                                "Crear tweet con mensaje menor a 1 carácter debe lanzar excepción");
                assertEquals(Tweet.ERROR_MENSAJE_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Tweet con mensaje mayor a 280 caracteres lanza excepción")
        void tweet_mensajeMayorMaximo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario autor = new Usuario("LongUser");
                String mensaje = "a".repeat(281);
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Tweet(mensaje, autor),
                                "Crear tweet con mensaje mayor a 280 caracteres debe lanzar excepción");
                assertEquals(Tweet.ERROR_MENSAJE_LONGITUD, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Retweet válido se instancia correctamente y conoce su tweet de origen")
        void retweet_valido_instanciaCorrecta() {
                // Setup: Preparar el escenario
                Usuario autorOriginal = new Usuario("Pedro");
                Tweet tweetOriginal = new Tweet("Tweet original", autorOriginal);
                Usuario autorRetweet = new Usuario("Maria");
                // Ejercitación: Ejecutar la acción a probar
                Tweet retweet = new Tweet(autorRetweet, tweetOriginal);
                // Verificación: Verificar el resultado esperado
                assertNotNull(retweet, "El retweet debería haberse creado correctamente");
                assertTrue(retweet.esRetweet(), "El tweet debe ser identificado como retweet");
                assertEquals(tweetOriginal, retweet.tweetOrigen(), "El retweet debe conocer su tweet de origen");
                assertEquals(tweetOriginal.mensaje(), retweet.mensaje(),
                                "El mensaje del retweet debe coincidir con el original");
        }

        @Test
        @DisplayName("Retweet con tweet de origen nulo lanza excepción")
        void retweet_tweetOrigenNulo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario autorRetweet = new Usuario("Maria");
                Tweet tweetOriginal = null;
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Tweet(autorRetweet, tweetOriginal),
                                "Crear retweet con tweet de origen nulo debe lanzar excepción");
                assertEquals(Tweet.ERROR_TWEET_ORIGEN_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("No se puede crear un re-tweet de un tweet propio")
        void retweet_mismoAutor_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario usuario = new Usuario("Retweeter");
                Tweet tweetOriginal = new Tweet("Texto original", usuario);

                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Tweet(usuario, tweetOriginal),
                                "No se debe permitir crear un re-tweet de un tweet propio");
                assertEquals(Tweet.ERROR_RETWEET_MISMO_AUTOR, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }
}