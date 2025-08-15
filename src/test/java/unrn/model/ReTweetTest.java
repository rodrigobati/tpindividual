package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReTweetTest {

    @Test
    @DisplayName("Crear un re-tweet con un tweet de origen válido")
    public void testCrearReTweetConTweetOrigenValido() {
        Usuario usuario = new Usuario("usuario1");
        Tweet tweetOrigen = new Tweet("Mensaje original", usuario);
        ReTweet reTweet = new ReTweet(usuario, tweetOrigen);
        
        assertNotNull(reTweet);
        assertEquals(tweetOrigen, reTweet.tweetOrigen());
    }

    @Test
    @DisplayName("No se puede hacer re-tweet de un tweet propio")
    public void testNoHacerRetweetDePropioTweet() {
        Usuario usuario = new Usuario("usuario1");
        Tweet tweet = new Tweet("Mensaje original", usuario);
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            new ReTweet(usuario, tweet);
        });
        
        assertEquals(ReTweet.ERROR_TWEET_ORIGEN_DISTINTO, exception.getMessage());
    }

    @Test
    @DisplayName("No se puede crear un re-tweet con un tweet nulo")
    public void testNoCrearReTweetConTweetNulo() {
        Usuario usuario = new Usuario("usuario1");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            new ReTweet(usuario, null);
        });
        
        assertEquals(ReTweet.ERROR_TWEET_ORIGEN_INVALIDO, exception.getMessage());
    }

}
