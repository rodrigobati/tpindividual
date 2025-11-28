package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tweets")
public class Tweet {

    static final String ERROR_AUTOR_OBLIGATORIO = "El autor del tweet no puede ser nulo";
    static final String ERROR_CONTENIDO_OBLIGATORIO = "El contenido del tweet no puede ser nulo ni vacío";
    static final String ERROR_CONTENIDO_LARGO = "El tweet no puede tener más de 280 caracteres";
    static final String ERROR_FECHA_CREACION_OBLIGATORIA = "La fecha de creación no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @Column(nullable = false, length = 280)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private boolean eliminado;

    protected Tweet() {
        // solo JPA
    }

    public Tweet(Usuario autor,
            String contenido,
            LocalDateTime fechaCreacion,
            boolean eliminado) {

        assertAutorValido(autor);
        assertContenidoValido(contenido);
        assertFechaCreacionValida(fechaCreacion);

        this.autor = autor;
        this.contenido = contenido;
        this.fechaCreacion = fechaCreacion;
        this.eliminado = eliminado;
    }

    public boolean esDe(Usuario posibleAutor) {
        return this.autor == posibleAutor;
    }

    public void eliminar() {
        this.eliminado = true;
    }

    public boolean estaEliminado() {
        return eliminado;
    }

    public boolean fueCreadoAntesDe(LocalDateTime fecha) {
        return fechaCreacion.isBefore(fecha);
    }

    public Usuario autor() {
        return autor;
    }

    public Long id() {
        return this.id;
    }

    public String contenido() {
        return this.contenido;
    }

    public LocalDateTime fechaCreacion() {
        return this.fechaCreacion;
    }

    private void assertAutorValido(Usuario autor) {
        if (autor == null) {
            throw new RuntimeException(ERROR_AUTOR_OBLIGATORIO);
        }
    }

    private void assertContenidoValido(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new RuntimeException(ERROR_CONTENIDO_OBLIGATORIO);
        }
        if (contenido.length() > 280) {
            throw new RuntimeException(ERROR_CONTENIDO_LARGO);
        }
    }

    private void assertFechaCreacionValida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new RuntimeException(ERROR_FECHA_CREACION_OBLIGATORIA);
        }
    }
}
