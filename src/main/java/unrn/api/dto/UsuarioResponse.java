package unrn.api.dto;

public record UsuarioResponse(
        Long id,
        String nombreUsuario,
        String avatarUrl) {
}
