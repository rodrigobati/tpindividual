package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "retweets")
public class ReTweet {

    static final String ERROR_AUTOR_RETWEET_OBLIGATORIO = "El autor del retweet no puede ser nulo";
    static final String ERROR_TWEET_ORIGINAL_OBLIGATORIO = "El tweet original no puede ser nulo";
    static final String ERROR_FECHA_RETWEET_OBLIGATORIA = "La fecha de creación del retweet no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tweet_original_id")
    private Tweet original;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected ReTweet() {
        // JPA
    }

    public ReTweet(Usuario autor,
            Tweet original,
            LocalDateTime fechaCreacion) {

        assertAutorValido(autor);
        assertOriginalValido(original);
        assertFechaCreacionValida(fechaCreacion);

        this.autor = autor;
        this.original = original;
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esDe(Usuario posibleAutor) {
        return this.autor == posibleAutor;
    }

    public boolean esSobre(Tweet posibleOriginal) {
        return this.original == posibleOriginal;
    }

    // Accesores para exponer información al exterior (DTOs, servicios)
    public Long id() {
        return this.id;
    }

    public Usuario autor() {
        return this.autor;
    }

    public Tweet original() {
        return this.original;
    }

    public java.time.LocalDateTime fechaCreacion() {
        return this.fechaCreacion;
    }

    private void assertAutorValido(Usuario autor) {
        if (autor == null) {
            throw new RuntimeException(ERROR_AUTOR_RETWEET_OBLIGATORIO);
        }
    }

    private void assertOriginalValido(Tweet original) {
        if (original == null) {
            throw new RuntimeException(ERROR_TWEET_ORIGINAL_OBLIGATORIO);
        }
    }

    private void assertFechaCreacionValida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new RuntimeException(ERROR_FECHA_RETWEET_OBLIGATORIA);
        }
    }
}
