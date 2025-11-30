package unrn.persistence.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unrn.model.Tweet;
import unrn.model.Usuario;

import java.util.List;

public interface JpaTweetsSpringData extends JpaRepository<Tweet, Long> {

    /**
     * Busca tweets de un usuario que NO estén eliminados, ordenados por fecha
     * descendente.
     * Mantiene invariante: solo mostrar tweets activos.
     */
    @Query("SELECT t FROM Tweet t WHERE t.autor = :autor AND t.eliminado = false ORDER BY t.fechaCreacion DESC")
    List<Tweet> findByAutorOrderByFechaCreacionDesc(@Param("autor") Usuario autor);

    /**
     * Busca tweets de una lista de autores que NO estén eliminados, ordenados por
     * fecha descendente.
     * Usa Pageable para limitar resultados.
     * Mantiene invariante: solo mostrar tweets activos.
     */
    @Query("SELECT t FROM Tweet t WHERE t.autor IN :autores AND t.eliminado = false ORDER BY t.fechaCreacion DESC")
    List<Tweet> findByAutorInOrderByFechaCreacionDesc(@Param("autores") List<Usuario> autores, Pageable pageable);

    /**
     * Busca TODOS los tweets del sistema que NO estén eliminados, ordenados por
     * fecha descendente.
     * Usa Pageable para limitar resultados.
     * Usado para la vista "Ver todos" en Home Page.
     */
    @Query("SELECT t FROM Tweet t WHERE t.eliminado = false ORDER BY t.fechaCreacion DESC")
    List<Tweet> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    /**
     * Marca como eliminados todos los tweets de un usuario.
     * Operación transaccional que mantiene la invariante de dominio:
     * "Los tweets de un usuario deben eliminarse cuando el usuario es eliminado."
     * 
     * @param idUsuario ID del usuario cuyos tweets deben marcarse como eliminados
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Tweet t SET t.eliminado = true WHERE t.autor.id = :idUsuario")
    void marcarTweetsComoEliminadosDe(@Param("idUsuario") Long idUsuario);
}
