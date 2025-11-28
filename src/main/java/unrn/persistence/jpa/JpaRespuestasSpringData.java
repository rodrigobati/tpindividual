package unrn.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import unrn.model.RespuestaTweet;
import unrn.model.Tweet;

import java.util.List;

public interface JpaRespuestasSpringData extends JpaRepository<RespuestaTweet, Long> {

    // Todas las respuestas a un tweet, más nuevas primero
    List<RespuestaTweet> findByTweetRespondidoOrderByFechaCreacionAsc(Tweet tweetRespondido);
}
