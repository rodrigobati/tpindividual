package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import unrn.api.dto.UsuarioResponse;
import unrn.model.Usuario;
import unrn.persistence.RepositorioUsuarios;
import unrn.service.ServicioSocial;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final ServicioSocial servicioSocial;
    private final RepositorioUsuarios repositorioUsuarios;

    public SocialController(ServicioSocial servicioSocial, RepositorioUsuarios repositorioUsuarios) {
        this.servicioSocial = servicioSocial;
        this.repositorioUsuarios = repositorioUsuarios;
    }

    @PostMapping("/usuarios/{idSeguido}/seguir")
    public void seguir(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idSeguido) {

        asegurarUsuarioExiste(jwt);
        String keycloakIdSeguidor = jwt.getSubject();
        servicioSocial.seguir(keycloakIdSeguidor, idSeguido);
    }

    @DeleteMapping("/usuarios/{idSeguido}/seguir")
    public void dejarDeSeguir(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idSeguido) {

        asegurarUsuarioExiste(jwt);
        String keycloakIdSeguidor = jwt.getSubject();
        servicioSocial.dejarDeSeguir(keycloakIdSeguidor, idSeguido);
    }

    @GetMapping("/seguidos")
    public List<UsuarioResponse> seguidosDelActual(@AuthenticationPrincipal Jwt jwt) {
        asegurarUsuarioExiste(jwt);
        String keycloakIdSeguidor = jwt.getSubject();
        List<Usuario> seguidos = servicioSocial.seguidosDelActual(keycloakIdSeguidor);
        return seguidos.stream()
                .map(this::toUsuarioResponse)
                .toList();
    }

    @GetMapping("/usuarios/{idUsuario}/seguidores")
    public List<UsuarioResponse> seguidores(@PathVariable Long idUsuario) {
        List<Usuario> seguidores = servicioSocial.seguidoresDe(idUsuario);
        return seguidores.stream()
                .map(this::toUsuarioResponse)
                .toList();
    }

    private UsuarioResponse toUsuarioResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.id(),
                usuario.nombreUsuario(),
                usuario.avatarUrl());
    }

    // ----------- Gestión automática de usuarios -----------

    /**
     * Asegura que el usuario autenticado existe en la base de datos.
     * Si no existe, lo crea automáticamente con datos del JWT.
     */
    private void asegurarUsuarioExiste(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        // Si ya existe, no hacer nada
        if (repositorioUsuarios.existePorKeycloakId(keycloakId)) {
            return;
        }

        // Extraer información del JWT
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");

        // Si no hay email en el token, usar username@minitwitter.local
        if (email == null || email.isBlank()) {
            email = username + "@minitwitter.local";
        }

        // Crear el usuario
        Usuario nuevoUsuario = new Usuario(
                keycloakId,
                username,
                email,
                LocalDateTime.now(),
                null, // biografía vacía
                null // avatar vacío
        );

        repositorioUsuarios.guardar(nuevoUsuario);
    }
}
