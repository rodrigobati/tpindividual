package unrn.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import unrn.api.dto.UsuarioResponse;
import unrn.model.Usuario;
import unrn.service.ServicioSocial;
import unrn.service.ServicioUsuarios;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final ServicioSocial servicioSocial;
    private final ServicioUsuarios servicioUsuarios;

    public SocialController(ServicioSocial servicioSocial, ServicioUsuarios servicioUsuarios) {
        this.servicioSocial = servicioSocial;
        this.servicioUsuarios = servicioUsuarios;
    }

    @PostMapping("/usuarios/{idSeguido}/seguir")
    public void seguir(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idSeguido) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakIdSeguidor = jwt.getSubject();
        servicioSocial.seguir(keycloakIdSeguidor, idSeguido);
    }

    @DeleteMapping("/usuarios/{idSeguido}/seguir")
    public void dejarDeSeguir(@AuthenticationPrincipal Jwt jwt,
            @PathVariable Long idSeguido) {

        servicioUsuarios.asegurarUsuarioExiste(jwt);
        String keycloakIdSeguidor = jwt.getSubject();
        servicioSocial.dejarDeSeguir(keycloakIdSeguidor, idSeguido);
    }

    @GetMapping("/seguidos")
    public List<UsuarioResponse> seguidosDelActual(@AuthenticationPrincipal Jwt jwt) {
        servicioUsuarios.asegurarUsuarioExiste(jwt);
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
}
