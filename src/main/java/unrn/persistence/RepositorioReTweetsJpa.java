package unrn.persistence;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaReTweetsSpringData;

import java.util.Collections;
import java.util.List;

@Repository
public class RepositorioReTweetsJpa implements RepositorioRetweets {

    private final JpaReTweetsSpringData jpa;

    public RepositorioReTweetsJpa(JpaReTweetsSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public ReTweet guardar(ReTweet retweet) {
        return jpa.save(retweet);
    }

    @Override
    public List<ReTweet> retweetsDeUsuario(Usuario autor) {
        return jpa.findByAutorOrderByFechaCreacionDesc(autor);
    }

    @Override
    public List<ReTweet> retweetsDeTweet(Tweet original) {
        return jpa.findByOriginal(original);
    }

    @Override
    public ReTweet buscarPorId(Long idRetweet) {
        return jpa.findById(idRetweet)
                .orElseThrow(() -> new RuntimeException("Retweet no encontrado: " + idRetweet));
    }

    @Override
    public boolean existeRetweetDeUsuarioSobreTweet(Usuario autor, Tweet original) {
        return jpa.existsByAutorAndOriginal(autor, original);
    }

    @Override
    public List<ReTweet> buscarRetweetsDeAutores(List<Usuario> autores, int limite) {
        if (autores == null || autores.isEmpty()) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, limite);
        return jpa.findByAutorInOrderByFechaCreacionDesc(autores, pageable);
    }
}
