package unrn.model;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Usuario {
    static final String ERROR_NOMBRE_INVALIDO = "El nombre de usuario no puede ser vacío";
    static final String ERROR_TWEET_NULO = "El tweet no puede ser vacío";
    static final String ERROR_NOMBRE_DUPLICADO = "Ya existe un usuario con ese nombre";
    static final String ERROR_NOMBRE_LONGITUD = "El nombre de usuario debe tener entre 5 y 25 caracteres";

    private static final Set<String> nombresRegistrados = new HashSet<>();

    private final String nombre;
    private final List<Tweet> tweets;

    public Usuario(String nombre) {
        assertNombreValido(nombre);
        assertNombreLongitud(nombre);
        assertNombreUnico(nombre);
        this.nombre = nombre;
        this.tweets = new ArrayList<>();
        nombresRegistrados.add(nombre);
    }

    public void publicarTweet(String mensaje) {
        Tweet tweet = new Tweet(mensaje, this);
        assertTweetNoNulo(tweet);
        tweets.add(tweet);
    }

    public List<Tweet> tweetsRealizados() {
        return Collections.unmodifiableList(tweets);
    }

    // Elimina el usuario y sus tweets
    public void eliminar() {
        tweets.clear();
        nombresRegistrados.remove(nombre);
    }

    private void assertNombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException(ERROR_NOMBRE_INVALIDO);
        }
    }

    private void assertNombreLongitud(String nombre) {
        int length = nombre.trim().length();
        if (length < 5 || length > 25) {
            throw new RuntimeException(ERROR_NOMBRE_LONGITUD);
        }
    }

    private void assertNombreUnico(String nombre) {
        if (nombresRegistrados.contains(nombre)) {
            throw new RuntimeException(ERROR_NOMBRE_DUPLICADO);
        }
    }

    private void assertTweetNoNulo(Tweet tweet) {
        if (tweet == null) {
            throw new RuntimeException(ERROR_TWEET_NULO);
        }
    }

    public boolean equals(Usuario usuario) {
        return nombre.equals(usuario.nombre);
    }
}