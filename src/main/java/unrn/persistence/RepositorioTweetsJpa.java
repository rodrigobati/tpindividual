package unrn.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaTweetsSpringData;

import java.util.Collections;
import java.util.List;

@Repository
public class RepositorioTweetsJpa implements RepositorioTweets {

    private final JpaTweetsSpringData jpa;

    public RepositorioTweetsJpa(JpaTweetsSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public Tweet buscarPorId(Long idTweet) {
        return jpa.findById(idTweet)
                .orElseThrow(() -> new RuntimeException("Tweet no encontrado: " + idTweet));
    }

    @Override
    public Tweet guardar(Tweet tweet) {
        return jpa.save(tweet);
    }

    @Override
    public void eliminar(Long idTweet) {
        jpa.deleteById(idTweet);
    }

    @Override
    public List<Tweet> tweetsDeUsuario(Usuario autor) {
        return jpa.findByAutorOrderByFechaCreacionDesc(autor);
    }

    @Override
    public List<Tweet> timelineDeUsuario(Usuario usuario, int limite) {
        // Implementación simple: por ahora solo tweets del usuario.
        // Más adelante, podés armar un JPQL que incluya seguidos.
        var todos = jpa.findByAutorOrderByFechaCreacionDesc(usuario);
        return todos.size() > limite ? todos.subList(0, limite) : todos;
    }

    @Override
    public List<Tweet> buscarTweetsDeAutores(List<Usuario> autores, int limite) {
        if (autores == null || autores.isEmpty()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, limite);
        return jpa.findByAutorInOrderByFechaCreacionDesc(autores, pageable);
    }

    @Override
    public List<Tweet> buscarTodosTweets(int limite) {
        // Buscar TODOS los tweets del sistema sin filtrar por autor
        // Ordenados por fecha descendente, limitados
        Pageable pageable = PageRequest.of(0, limite);
        return jpa.findAllByOrderByFechaCreacionDesc(pageable);
    }

    @Override
    public void marcarTweetsComoEliminadosDe(Long idUsuario) {
        jpa.marcarTweetsComoEliminadosDe(idUsuario);
    }
}
