package unrn.persistence;

import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface RepositorioRetweets {

    ReTweet guardar(ReTweet retweet);

    List<ReTweet> retweetsDeUsuario(Usuario autor);

    List<ReTweet> retweetsDeTweet(Tweet original);

    ReTweet buscarPorId(Long idRetweet);
}
