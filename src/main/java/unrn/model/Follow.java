package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(name = "uk_seguidor_seguido", columnNames = {
        "seguidor_id", "seguido_id" }))
public class Follow {

    static final String ERROR_SEGUIDOR_OBLIGATORIO = "El seguidor no puede ser nulo";
    static final String ERROR_SEGUIDO_OBLIGATORIO = "El seguido no puede ser nulo";
    static final String ERROR_SEGUIR_A_SI_MISMO = "Un usuario no puede seguirse a sí mismo";
    static final String ERROR_FECHA_FOLLOW_OBLIGATORIA = "La fecha de creación del follow no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seguidor_id")
    private Usuario seguidor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id")
    private Usuario seguido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    protected Follow() {
        // JPA
    }

    public Follow(Usuario seguidor,
            Usuario seguido,
            LocalDateTime fechaCreacion) {

        assertUsuariosValidos(seguidor, seguido);
        assertFechaCreacionValida(fechaCreacion);

        this.seguidor = seguidor;
        this.seguido = seguido;
        this.fechaCreacion = fechaCreacion;
    }

    public boolean esEntre(Usuario posibleSeguidor, Usuario posibleSeguido) {
        return seguidor == posibleSeguidor && seguido == posibleSeguido;
    }

    public boolean esSeguidor(Usuario usuario) {
        return seguidor == usuario;
    }

    public boolean esSeguido(Usuario usuario) {
        return seguido == usuario;
    }

    // Accesores de dominio (nombre corto, no getX) para uso en repositorios y tests
    public Usuario seguidor() {
        return this.seguidor;
    }

    public Usuario seguido() {
        return this.seguido;
    }

    private void assertUsuariosValidos(Usuario seguidor, Usuario seguido) {
        if (seguidor == null) {
            throw new RuntimeException(ERROR_SEGUIDOR_OBLIGATORIO);
        }
        if (seguido == null) {
            throw new RuntimeException(ERROR_SEGUIDO_OBLIGATORIO);
        }
        if (seguidor == seguido) {
            throw new RuntimeException(ERROR_SEGUIR_A_SI_MISMO);
        }
    }

    private void assertFechaCreacionValida(LocalDateTime fechaCreacion) {
        if (fechaCreacion == null) {
            throw new RuntimeException(ERROR_FECHA_FOLLOW_OBLIGATORIA);
        }
    }
}
