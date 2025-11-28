package unrn.persistence;

import org.springframework.stereotype.Repository;
import unrn.model.RespuestaTweet;
import unrn.model.Tweet;
import unrn.persistence.jpa.JpaRespuestasSpringData;

import java.util.List;

@Repository
public class RepositorioRespuestasJpa implements RepositorioRespuestas {

    private final JpaRespuestasSpringData jpa;

    public RepositorioRespuestasJpa(JpaRespuestasSpringData jpa) {
        this.jpa = jpa;
    }

    @Override
    public RespuestaTweet guardar(RespuestaTweet respuesta) {
        return jpa.save(respuesta);
    }

    @Override
    public RespuestaTweet buscarPorId(Long idRespuesta) {
        return jpa.findById(idRespuesta)
                .orElseThrow(() -> new RuntimeException("Respuesta no encontrada: " + idRespuesta));
    }

    @Override
    public void eliminar(Long idRespuesta) {
        jpa.deleteById(idRespuesta);
    }

    @Override
    public List<RespuestaTweet> respuestasDeTweet(Tweet tweet) {
        return jpa.findByTweetRespondidoOrderByFechaCreacionAsc(tweet);
    }
}
