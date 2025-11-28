package unrn.service;

import unrn.model.Tweet;

import java.util.List;

/**
 * Consultas de lectura (timeline, tweets de usuario, etc.).
 * No modifica el estado, solo lee.
 */
public interface ServicioTimeline {

    /**
     * Timeline principal del usuario: tweets propios + de seguidos,
     * ordenados del más nuevo al más viejo.
     */
    List<Tweet> timelineDeUsuario(Long idUsuario);

    /**
     * Tweets publicados por un usuario específico.
     */
    List<Tweet> tweetsDeUsuario(Long idUsuario);
}
