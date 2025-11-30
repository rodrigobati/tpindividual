package unrn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    // Mensajes de error
    static final String ERROR_KEYCLOAK_ID_OBLIGATORIO = "El id de Keycloak no puede ser nulo ni vacío";
    static final String ERROR_NOMBRE_OBLIGATORIO = "El nombre de usuario no puede ser nulo ni vacío";
    static final String ERROR_NOMBRE_LONGITUD = "El nombre de usuario debe tener entre 5 y 25 caracteres";
    static final String ERROR_EMAIL_OBLIGATORIO = "El email no puede ser nulo ni vacío";
    static final String ERROR_FECHA_REGISTRO_OBLIGATORIA = "La fecha de registro no puede ser nula";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 64)
    private String keycloakId;

    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 40)
    private String nombreUsuario;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 280)
    private String biografia;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private boolean activo;

    // --- Constructor vacío para JPA (no usar desde el dominio) ---
    protected Usuario() {
        // solo para JPA
    }

    // --- Constructor “de verdad” (dominio) ---
    public Usuario(String keycloakId,
            String nombreUsuario,
            String email,
            LocalDateTime fechaRegistro,
            String biografia,
            String avatarUrl) {

        assertKeycloakIdValido(keycloakId);
        assertNombreValido(nombreUsuario);
        assertEmailValido(email);
        assertFechaRegistroValida(fechaRegistro);

        this.keycloakId = keycloakId;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.biografia = biografia;
        this.avatarUrl = avatarUrl;
        this.activo = true;
    }

    // --- Comportamiento de dominio (acorde al diagrama) ---

    public Tweet publicarTweet(String contenido) {
        return new Tweet(this, contenido, LocalDateTime.now(), false);
    }

    public ReTweet retweet(Tweet original) {
        if (original == null) {
            throw new RuntimeException("El tweet original no puede ser nulo");
        }
        return new ReTweet(this, original, LocalDateTime.now());
    }

    public RespuestaTweet responder(Tweet a, String contenido) {
        if (a == null) {
            throw new RuntimeException("El tweet a responder no puede ser nulo");
        }
        return new RespuestaTweet(this, a, contenido, LocalDateTime.now(), false);
    }

    public Follow seguir(Usuario a) {
        if (a == null) {
            throw new RuntimeException("El usuario a seguir no puede ser nulo");
        }
        if (a == this) {
            throw new RuntimeException("Un usuario no puede seguirse a sí mismo");
        }
        return new Follow(this, a, LocalDateTime.now());
    }

    public void dejarDeSeguir(Usuario a) {
        if (a == null || a == this) {
            return;
        }
        // La eliminación concreta del Follow la hará el servicio/repositorio.
    }

    public Like darLike(Tweet a) {
        if (a == null) {
            throw new RuntimeException("El tweet a likear no puede ser nulo");
        }
        return new Like(this, a, LocalDateTime.now());
    }

    public void quitarLike(Tweet a) {
        if (a == null) {
            return;
        }
        // Igual que dejarDeSeguir: lo concreto lo maneja el servicio/repositorio.
    }

    public boolean estaActivo() {
        return activo;
    }

    public void desactivar() {
        this.activo = false;
    }

    public String nombreUsuario() {
        return nombreUsuario;
    }

    public String keycloakId() {
        return keycloakId;
    }

    public Long id() {
        return this.id;
    }

    public String avatarUrl() {
        return this.avatarUrl;
    }

    // --- Validaciones privadas ---

    private void assertKeycloakIdValido(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) {
            throw new RuntimeException(ERROR_KEYCLOAK_ID_OBLIGATORIO);
        }
    }

    private void assertNombreValido(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new RuntimeException(ERROR_NOMBRE_OBLIGATORIO);
        }
        if (nombreUsuario.length() < 5 || nombreUsuario.length() > 25) {
            throw new RuntimeException(ERROR_NOMBRE_LONGITUD);
        }
    }

    private void assertEmailValido(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException(ERROR_EMAIL_OBLIGATORIO);
        }
    }

    private void assertFechaRegistroValida(LocalDateTime fechaRegistro) {
        if (fechaRegistro == null) {
            throw new RuntimeException(ERROR_FECHA_REGISTRO_OBLIGATORIA);
        }
    }
}
