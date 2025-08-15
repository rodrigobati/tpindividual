package unrn.model;

public class ReTweet {

    static final String ERROR_TWEET_ORIGEN_INVALIDO = "El tweet de origen no puede ser vacío";
    static final String ERROR_TWEET_ORIGEN_DISTINTO = "Un usuario no puede hacer re-tweet de su propio tweet";
    private Usuario usuario;
    private Tweet tweetOrigen;
    
    public ReTweet(Usuario usuario, Tweet tweetOrigen) {
        assertTweetOrigenValido(tweetOrigen);
        assertTweetOrigenDistintoTweet(tweetOrigen);
        this.usuario = usuario;
        this.tweetOrigen = tweetOrigen;

    }

    
    public Tweet tweetOrigen() {
        return tweetOrigen;
    }
    
    
    private void assertTweetOrigenValido(Tweet tweetOrigen) {
        if (tweetOrigen == null) {
            throw new RuntimeException(ERROR_TWEET_ORIGEN_INVALIDO);
        }
    }

    private void assertTweetOrigenDistintoTweet(Tweet reTweet) {
        if (mismoAutor(reTweet)) {
            throw new RuntimeException(ERROR_TWEET_ORIGEN_DISTINTO);
        }
    }

    private boolean mismoAutor(Tweet tweet) {
        return tweet.esAutor(this.usuario);
    }
}
