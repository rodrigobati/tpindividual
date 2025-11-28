package unrn.persistence;

import org.springframework.stereotype.Repository;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.jpa.JpaReTweetsSpringData;

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
}
