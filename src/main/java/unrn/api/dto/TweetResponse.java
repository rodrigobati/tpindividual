package unrn.api.dto;

public record TweetResponse(
                Long id,
                String autor,
                String contenido,
                String fechaCreacion,
                boolean eliminado,
                boolean esRetweet,
                String retweeteadoPor) {
}
