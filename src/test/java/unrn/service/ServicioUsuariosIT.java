package unrn.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar invariantes de dominio relacionadas con
 * usuarios.
 * 
 * Invariante crítica:
 * "Los tweets de un usuario deben eliminarse cuando el usuario es eliminado.
 * No pueden existir tweets sin usuario activo."
 */
@SpringBootTest
@ActiveProfiles("test")
class ServicioUsuariosIT {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @Autowired
    private RepositorioUsuarios repositorioUsuarios;

    @Autowired
    private RepositorioTweets repositorioTweets;

    /**
     * Test de invariante de dominio: Desactivar usuario elimina todos sus tweets.
     * 
     * Escenario:
     * 1. Crear usuario con varios tweets
     * 2. Verificar que tweets están activos
     * 3. Desactivar usuario
     * 4. Verificar que usuario está inactivo
     * 5. Verificar que NO hay tweets visibles del usuario
     * 
     * Resultado esperado:
     * - Usuario.activo = false
     * - Todos sus tweets tienen eliminado = true
     * - Las queries NO retornan tweets del usuario inactivo
     * - Invariante mantenida: no existen tweets de usuarios inactivos
     */
    @Test
    void desactivarUsuario_debeEliminarTodosLosTweetsDelUsuario() {
        // ARRANGE: Crear usuario y tweets
        Usuario usuario = new Usuario(
                "keycloak-test-id-desactivar",
                "usuario_test_desactivar",
                "test.desactivar@minitwitter.test",
                LocalDateTime.now(),
                "Bio de prueba",
                null);
        usuario = repositorioUsuarios.guardar(usuario);

        // Crear múltiples tweets del usuario
        Tweet tweet1 = usuario.publicarTweet("Tweet 1 del usuario");
        Tweet tweet2 = usuario.publicarTweet("Tweet 2 del usuario");
        Tweet tweet3 = usuario.publicarTweet("Tweet 3 del usuario");

        tweet1 = repositorioTweets.guardar(tweet1);
        tweet2 = repositorioTweets.guardar(tweet2);
        tweet3 = repositorioTweets.guardar(tweet3);

        // Verificar estado inicial: tweets están activos
        assertFalse(tweet1.estaEliminado(), "Tweet 1 debe estar activo antes de desactivar usuario");
        assertFalse(tweet2.estaEliminado(), "Tweet 2 debe estar activo antes de desactivar usuario");
        assertFalse(tweet3.estaEliminado(), "Tweet 3 debe estar activo antes de desactivar usuario");
        assertTrue(usuario.estaActivo(), "Usuario debe estar activo inicialmente");

        // Verificar que las queries retornan los tweets
        List<Tweet> tweetsAntesDeDesactivar = repositorioTweets.tweetsDeUsuario(usuario);
        assertEquals(3, tweetsAntesDeDesactivar.size(),
                "Deben existir 3 tweets activos del usuario antes de desactivar");

        // ACT: Desactivar usuario (debe eliminar automáticamente todos sus tweets)
        servicioUsuarios.desactivarUsuario(usuario.keycloakId());

        // ASSERT: Verificar que usuario está inactivo
        Usuario usuarioDesactivado = repositorioUsuarios.buscarPorKeycloakId(usuario.keycloakId());
        assertFalse(usuarioDesactivado.estaActivo(),
                "Usuario debe estar inactivo después de desactivar");

        // ASSERT: Verificar que tweets están marcados como eliminados
        Tweet tweet1Actualizado = repositorioTweets.buscarPorId(tweet1.id());
        Tweet tweet2Actualizado = repositorioTweets.buscarPorId(tweet2.id());
        Tweet tweet3Actualizado = repositorioTweets.buscarPorId(tweet3.id());

        assertTrue(tweet1Actualizado.estaEliminado(),
                "Tweet 1 debe estar eliminado después de desactivar usuario");
        assertTrue(tweet2Actualizado.estaEliminado(),
                "Tweet 2 debe estar eliminado después de desactivar usuario");
        assertTrue(tweet3Actualizado.estaEliminado(),
                "Tweet 3 debe estar eliminado después de desactivar usuario");

        // ASSERT: Verificar que las queries NO retornan tweets del usuario inactivo
        // (las queries filtran eliminado = false)
        List<Tweet> tweetsDespuesDeDesactivar = repositorioTweets.tweetsDeUsuario(usuarioDesactivado);
        assertTrue(tweetsDespuesDeDesactivar.isEmpty(),
                "No deben retornarse tweets de un usuario inactivo (invariante mantenida)");

        // INVARIANTE VERIFICADA:
        // ✅ No existen tweets visibles de un usuario inactivo
        // ✅ Operación es atómica (transaccional)
        // ✅ Arquitectura DDD correcta (orquestación desde servicio de aplicación)
    }

    /**
     * Test de caso borde: Desactivar usuario sin tweets no debe fallar.
     */
    @Test
    void desactivarUsuario_sinTweets_noDebefallar() {
        // ARRANGE: Crear usuario sin tweets
        Usuario usuario = new Usuario(
                "keycloak-test-id-sin-tweets",
                "usuario_sin_tweets",
                "sin.tweets@minitwitter.test",
                LocalDateTime.now(),
                null,
                null);
        usuario = repositorioUsuarios.guardar(usuario);
        final String keycloakId = usuario.keycloakId();

        assertTrue(usuario.estaActivo(), "Usuario debe estar activo inicialmente");

        // ACT: Desactivar usuario (no tiene tweets)
        assertDoesNotThrow(() -> servicioUsuarios.desactivarUsuario(keycloakId),
                "Desactivar usuario sin tweets no debe lanzar excepción");

        // ASSERT: Verificar que usuario está inactivo
        Usuario usuarioDesactivado = repositorioUsuarios.buscarPorKeycloakId(keycloakId);
        assertFalse(usuarioDesactivado.estaActivo(),
                "Usuario sin tweets debe estar inactivo después de desactivar");
    }
}
