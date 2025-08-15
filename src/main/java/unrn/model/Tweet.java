package unrn.model;

public class Tweet {
    static final String ERROR_MENSAJE_INVALIDO = "El mensaje del tweet no puede ser vacío";
    static final String ERROR_MENSAJE_LONGITUD = "El mensaje del tweet debe tener entre 1 y 280 caracteres";
    static final String ERROR_RETWEET_MISMO_AUTOR = "No se puede hacer re-tweet de un tweet propio";

    private final String mensaje;
    private final Usuario autor;

    // Constructor para tweet normal
    public Tweet(String mensaje, Usuario autor) {
        assertMensajeValido(mensaje);
        assertMensajeLongitud(mensaje);
        this.mensaje = mensaje;
        this.autor = autor;
    }

    // Constructor para retweet
    public Tweet(Usuario autor, Tweet tweetOrigen) {
        //assertTweetOrigenValido(tweetOrigen);
        assertRetweetNoPropio(autor, tweetOrigen);
        this.mensaje = tweetOrigen.mensaje();
        this.autor = autor;
    }

    public String mensaje() {
        return mensaje;
    }


    private void assertMensajeValido(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new RuntimeException(ERROR_MENSAJE_INVALIDO);
        }
    }

    private void assertMensajeLongitud(String mensaje) {
        int length = mensaje.length();
        if (length < 1 || length > 280) {
            throw new RuntimeException(ERROR_MENSAJE_LONGITUD);
        }
    }


    private void assertRetweetNoPropio(Usuario autor, Tweet tweetOrigen) {
        if (tweetOrigen.autor.equals(autor) ) {
            throw new RuntimeException(ERROR_RETWEET_MISMO_AUTOR);
        }
    }

    public boolean esAutor(Usuario usuario) {
       return this.autor.equals(usuario);
    }
}