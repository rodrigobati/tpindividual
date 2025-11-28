package unrn.persistence;

import unrn.model.RespuestaTweet;
import unrn.model.Tweet;

import java.util.List;

public interface RepositorioRespuestas {

    RespuestaTweet guardar(RespuestaTweet respuesta);

    RespuestaTweet buscarPorId(Long idRespuesta);

    void eliminar(Long idRespuesta);

    List<RespuestaTweet> respuestasDeTweet(Tweet tweet);
}
