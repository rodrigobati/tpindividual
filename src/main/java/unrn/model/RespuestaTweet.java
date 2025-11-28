package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respuestas")
public class RespuestaTweet {

    static final String ERROR_AUTOR_RESPUESTA_OBLIGATORIO = "El autor de la respuesta no puede ser nulo";
    static final String ERROR_TWEET_RESPONDIDO_OBLIGATORIO = "El tweet respondido no puede ser nulo";
    static final String ERROR_CONTENIDO_RESPUESTA_OBLIGATORIO = "El contenido de la respuesta no puede ser nulo ni vacío";
    static final String ERROR_CONTENIDO_RESPUESTA_LARGO = "La respuesta no puede tener más de 280 caracteres";
    static final String ERROR_FECHA_RESPUESTA_OBLIGATORIA = "La fecha de creación de la respuesta no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tweet_respondido_id")
    private Tweet tweetRespondido;

    @Column(nullable = false, length = 280)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean eliminado;

    protected RespuestaTweet() {
        // JPA
    }

    public RespuestaTweet(Usuario autor,
            Tweet tweetRespondido,
            String contenido,
            LocalDateTime fechaCreacion,
            boolean eliminado) {

        assertAutorValido(autor);
        assertTweetRespondidoValido(tweetRespondido);
        assertContenidoValido(contenido);
        assertFechaCreacionValida(fechaCreacion);

        this.autor = autor;
        this.tweetRespondido = tweetRespondido;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
        this.eliminado = eliminado;
    }

    public boolean esDe(Usuario posibleAutor) {
        return this.autor == posibleAutor;
    }

    public boolean respondeA(Tweet posibleTweet) {
        return this.tweetRespondido == posibleTweet;
    }

    public void eliminar() {
        this.eliminado = true;
    }

    public boolean estaEliminado() {
        return eliminado;
    }

    public Long id() {
        return this.id;
    }

    public Usuario autor() {
        return this.autor;
    }

    public String contenido() {
        return this.contenido;
    }

    public LocalDateTime fechaCreacion() {
        return this.fechaCreacion;
    }

    private void assertAutorValido(Usuario autor) {
        if (autor == null) {
            throw new RuntimeException(ERROR_AUTOR_RESPUESTA_OBLIGATORIO);
        }
    }

    private void assertTweetRespondidoValido(Tweet tweetRespondido) {
        if (tweetRespondido == null) {
            throw new RuntimeException(ERROR_TWEET_RESPONDIDO_OBLIGATORIO);
        }
    }

    private void assertContenidoValido(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new RuntimeException(ERROR_CONTENIDO_RESPUESTA_OBLIGATORIO);
        }
        if (contenido.length() > 280) {
            throw new RuntimeException(ERROR_CONTENIDO_RESPUESTA_LARGO);
        }
    }

    private void assertFechaCreacionValida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new RuntimeException(ERROR_FECHA_RESPUESTA_OBLIGATORIA);
        }
    }
}
