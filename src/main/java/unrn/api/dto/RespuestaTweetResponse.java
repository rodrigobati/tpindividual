package unrn.api.dto;

public record RespuestaTweetResponse(
        Long id,
        String autor,
        String contenido,
        String fechaCreacion,
        boolean eliminado) {
}
