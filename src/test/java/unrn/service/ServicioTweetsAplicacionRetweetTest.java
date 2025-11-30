package unrn.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.RepositorioRetweets;
import unrn.persistence.RepositorioTweets;
import unrn.persistence.RepositorioUsuarios;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests para la funcionalidad de retweet en ServicioTweetsAplicacion.
 * Verifica: creación exitosa, idempotencia, y manejo de casos edge.
 */
@ExtendWith(MockitoExtension.class)
class ServicioTweetsAplicacionRetweetTest {

    @Mock
    private RepositorioTweets repositorioTweets;

    @Mock
    private RepositorioRetweets repositorioRetweets;

    @Mock
    private RepositorioUsuarios repositorioUsuarios;

    @Mock
    private ServicioSocial servicioSocial;

    @InjectMocks
    private ServicioTweetsAplicacion servicio;

    private Usuario usuarioAutor;
    private Usuario usuarioRetweeter;
    private Tweet tweetOriginal;

    @BeforeEach
    void setUp() {
        usuarioAutor = new Usuario(
                "keycloak-autor",
                "autor",
                "autor@example.com",
                LocalDateTime.now(),
                null,
                null);

        usuarioRetweeter = new Usuario(
                "keycloak-retweeter",
                "retweeter",
                "retweeter@example.com",
                LocalDateTime.now(),
                null,
                null);

        tweetOriginal = new Tweet(
                usuarioAutor,
                "Tweet original",
                LocalDateTime.now(),
                false);
    }

    @Test
    @DisplayName("retweetear() crea un nuevo retweet cuando no existe uno previo")
    void retweetear_noExistePrevio_creaRetweetExitosamente() {
        // Arrange
        String keycloakIdRetweeter = "keycloak-retweeter";
        Long idTweetOriginal = 1L;

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakIdRetweeter))
                .thenReturn(usuarioRetweeter);
        when(repositorioTweets.buscarPorId(idTweetOriginal))
                .thenReturn(tweetOriginal);
        when(repositorioRetweets.existeRetweetDeUsuarioSobreTweet(usuarioRetweeter, tweetOriginal))
                .thenReturn(false);

        ReTweet retweetEsperado = new ReTweet(usuarioRetweeter, tweetOriginal, LocalDateTime.now());
        when(repositorioRetweets.guardar(any(ReTweet.class)))
                .thenReturn(retweetEsperado);

        // Act
        ReTweet resultado = servicio.retweetear(keycloakIdRetweeter, idTweetOriginal);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.esDe(usuarioRetweeter));
        assertTrue(resultado.esSobre(tweetOriginal));
        verify(repositorioRetweets).existeRetweetDeUsuarioSobreTweet(usuarioRetweeter, tweetOriginal);
        verify(repositorioRetweets).guardar(any(ReTweet.class));
    }

    @Test
    @DisplayName("retweetear() retorna retweet existente cuando ya existe (idempotencia)")
    void retweetear_yaExiste_retornaExistenteIdempotente() {
        // Arrange
        String keycloakIdRetweeter = "keycloak-retweeter";
        Long idTweetOriginal = 1L;

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakIdRetweeter))
                .thenReturn(usuarioRetweeter);
        when(repositorioTweets.buscarPorId(idTweetOriginal))
                .thenReturn(tweetOriginal);
        when(repositorioRetweets.existeRetweetDeUsuarioSobreTweet(usuarioRetweeter, tweetOriginal))
                .thenReturn(true); // YA EXISTE

        ReTweet retweetExistente = new ReTweet(usuarioRetweeter, tweetOriginal, LocalDateTime.now().minusHours(1));
        when(repositorioRetweets.retweetsDeUsuario(usuarioRetweeter))
                .thenReturn(Arrays.asList(retweetExistente));

        // Act
        ReTweet resultado = servicio.retweetear(keycloakIdRetweeter, idTweetOriginal);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.esDe(usuarioRetweeter));
        assertTrue(resultado.esSobre(tweetOriginal));
        assertEquals(retweetExistente, resultado);
        verify(repositorioRetweets).existeRetweetDeUsuarioSobreTweet(usuarioRetweeter, tweetOriginal);
        verify(repositorioRetweets, never()).guardar(any()); // NO debe guardar uno nuevo
    }

    @Test
    @DisplayName("retweetear() lanza OperacionNoPermitidaException cuando usuario intenta retweet de tweet propio")
    void retweetear_tweetPropio_lanzaExcepcion() {
        // Arrange
        String keycloakId = "keycloak-autor";
        Long idTweet = 1L;

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakId))
                .thenReturn(usuarioAutor);
        when(repositorioTweets.buscarPorId(idTweet))
                .thenReturn(tweetOriginal);

        // Act & Assert
        var excepcion = assertThrows(unrn.api.exception.OperacionNoPermitidaException.class,
                () -> servicio.retweetear(keycloakId, idTweet));

        assertEquals("No se puede hacer retweet de un tweet propio", excepcion.getMessage());
        verify(repositorioRetweets, never()).guardar(any()); // NO debe intentar guardar
    }

    @Test
    @DisplayName("retweetear() lanza excepción cuando el usuario no existe")
    void retweetear_usuarioNoExiste_lanzaExcepcion() {
        // Arrange
        String keycloakIdInexistente = "keycloak-inexistente";
        Long idTweet = 1L;

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakIdInexistente))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> servicio.retweetear(keycloakIdInexistente, idTweet));
        verify(repositorioRetweets, never()).guardar(any());
    }

    @Test
    @DisplayName("retweetear() lanza excepción cuando el tweet no existe")
    void retweetear_tweetNoExiste_lanzaExcepcion() {
        // Arrange
        String keycloakId = "keycloak-retweeter";
        Long idTweetInexistente = 999L;

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakId))
                .thenReturn(usuarioRetweeter);
        when(repositorioTweets.buscarPorId(idTweetInexistente))
                .thenThrow(new RuntimeException("Tweet no encontrado"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> servicio.retweetear(keycloakId, idTweetInexistente));
        verify(repositorioRetweets, never()).guardar(any());
    }

    @Test
    @DisplayName("retweetear() idempotencia con múltiples retweets existentes")
    void retweetear_multiplesRetweetsExistentes_encuentraElCorrecto() {
        // Arrange
        String keycloakIdRetweeter = "keycloak-retweeter";
        Long idTweetOriginal = 1L;

        Tweet otroTweet = new Tweet(usuarioAutor, "Otro tweet", LocalDateTime.now(), false);
        ReTweet retweetOtro = new ReTweet(usuarioRetweeter, otroTweet, LocalDateTime.now().minusDays(1));
        ReTweet retweetBuscado = new ReTweet(usuarioRetweeter, tweetOriginal, LocalDateTime.now().minusHours(2));

        when(repositorioUsuarios.buscarPorKeycloakId(keycloakIdRetweeter))
                .thenReturn(usuarioRetweeter);
        when(repositorioTweets.buscarPorId(idTweetOriginal))
                .thenReturn(tweetOriginal);
        when(repositorioRetweets.existeRetweetDeUsuarioSobreTweet(usuarioRetweeter, tweetOriginal))
                .thenReturn(true);
        when(repositorioRetweets.retweetsDeUsuario(usuarioRetweeter))
                .thenReturn(Arrays.asList(retweetOtro, retweetBuscado));

        // Act
        ReTweet resultado = servicio.retweetear(keycloakIdRetweeter, idTweetOriginal);

        // Assert
        assertNotNull(resultado);
        assertEquals(retweetBuscado, resultado);
        assertTrue(resultado.esSobre(tweetOriginal));
        verify(repositorioRetweets, never()).guardar(any());
    }
}
