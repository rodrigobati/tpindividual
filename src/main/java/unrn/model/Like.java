package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
public class Like {

    static final String ERROR_AUTOR_LIKE_OBLIGATORIO = "El autor del like no puede ser nulo";
    static final String ERROR_TWEET_LIKE_OBLIGATORIO = "El tweet del like no puede ser nulo";
    static final String ERROR_FECHA_LIKE_OBLIGATORIA = "La fecha de creación del like no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tweet_id")
    private Tweet tweet;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected Like() {
        // JPA
    }

    public Like(Usuario autor,
            Tweet tweet,
            LocalDateTime fechaCreacion) {

        assertAutorValido(autor);
        assertTweetValido(tweet);
        assertFechaCreacionValida(fechaCreacion);

        this.autor = autor;
        this.tweet = tweet;
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esDe(Usuario posibleAutor) {
        return this.autor == posibleAutor;
    }

    public boolean esSobre(Tweet posibleTweet) {
        return this.tweet == posibleTweet;
    }

    public boolean fueCreadoAntesDe(LocalDateTime fecha) {
        return fechaCreacion.isBefore(fecha);
    }

    public Long id() {
        return this.id;
    }

    public Usuario autor() {
        return this.autor;
    }

    public LocalDateTime fechaCreacion() {
        return this.fechaCreacion;
    }

    private void assertAutorValido(Usuario autor) {
        if (autor == null) {
            throw new RuntimeException(ERROR_AUTOR_LIKE_OBLIGATORIO);
        }
    }

    private void assertTweetValido(Tweet tweet) {
        if (tweet == null) {
            throw new RuntimeException(ERROR_TWEET_LIKE_OBLIGATORIO);
        }
    }

    private void assertFechaCreacionValida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new RuntimeException(ERROR_FECHA_LIKE_OBLIGATORIA);
        }
    }
}
