package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

        @Test
        @DisplayName("Crear usuario con nombre válido instancia correctamente")
        void crearUsuario_nombreValido_instanciaCorrecta() {
                // Setup: Preparar el escenario
                String nombre = "Juan";
                // Ejercitación: Ejecutar la acción a probar
                Usuario usuario = new Usuario(nombre);
                // Verificación: Verificar el resultado esperado
                assertNotNull(usuario, "El usuario debería haberse creado correctamente");
                assertEquals(0, usuario.tweetsRealizados().size(), "El usuario recién creado no debe tener tweets");
        }

        @Test
        @DisplayName("Crear usuario con nombre nulo lanza excepción")
        void crearUsuario_nombreNulo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                String nombre = null;
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Usuario(nombre),
                                "Crear usuario con nombre nulo debe lanzar excepción");
                assertEquals(Usuario.ERROR_NOMBRE_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Crear usuario con nombre vacío lanza excepción")
        void crearUsuario_nombreVacio_lanzaExcepcion() {
                // Setup: Preparar el escenario
                String nombre = "   ";
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Usuario(nombre),
                                "Crear usuario con nombre vacío debe lanzar excepción");
                assertEquals(Usuario.ERROR_NOMBRE_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Publicar tweet agrega el tweet a la lista del usuario")
        void publicarTweet_mensajeValido_agregaTweet() {
                // Setup: Preparar el escenario
                Usuario usuario = new Usuario("Ana");
                String mensaje = "Hola mundo!";
                // Ejercitación: Ejecutar la acción a probar
                usuario.publicarTweet(mensaje);
                // Verificación: Verificar el resultado esperado
                List<Tweet> tweets = usuario.tweetsRealizados();
                assertEquals(1, tweets.size(), "El usuario debe tener un tweet publicado");
                assertEquals(mensaje, tweets.get(0).mensaje(), "El mensaje del tweet debe coincidir");
        }

        @Test
        @DisplayName("Publicar tweet con mensaje nulo lanza excepción")
        void publicarTweet_mensajeNulo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                Usuario usuario = new Usuario("Pedro");
                String mensaje = null;
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> usuario.publicarTweet(mensaje),
                                "Publicar tweet con mensaje nulo debe lanzar excepción");
                assertEquals(Tweet.ERROR_MENSAJE_INVALIDO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida en Tweet");
        }

        @Test
        @DisplayName("La lista de tweets realizados es solo lectura")
        void tweetsRealizados_listaSoloLectura() {
                // Setup: Preparar el escenario
                Usuario usuario = new Usuario("Lucas");
                usuario.publicarTweet("Primer tweet");
                List<Tweet> tweets = usuario.tweetsRealizados();
                // Ejercitación y Verificación: Ejecutar y verificar excepción al modificar la
                // lista
                assertThrows(UnsupportedOperationException.class, () -> tweets.add(new Tweet("tro", usuario)),
                                "La lista de tweets debe ser solo lectura");
        }

        @Test
        @DisplayName("eliminar sobre usuario sin tweets no genera error")
        void eliminar_usuarioSinTweets_noGeneraError() {
                // Setup: Preparar el escenario
                Usuario usuario = new Usuario("Sofia");
                assertEquals(0, usuario.tweetsRealizados().size(), "El usuario recién creado no debe tener tweets");

                // Ejercitación: Ejecutar la acción a probar
                usuario.eliminar();

                // Verificación: Verificar el resultado esperado
                assertEquals(0, usuario.tweetsRealizados().size(),
                                "El usuario debe seguir sin tweets después de eliminar");
        }

        @Test
        @DisplayName("No se pueden agregar dos usuarios con el mismo userName")
        void crearUsuario_nombreDuplicado_lanzaExcepcion() {
                // Setup: Preparar el escenario
                String nombre = "duplicado";
                new Usuario(nombre);

                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Usuario(nombre),
                                "No se puede crear dos usuarios con el mismo nombre");
                assertEquals(Usuario.ERROR_NOMBRE_DUPLICADO, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Crear usuario con nombre menor a 5 caracteres lanza excepción")
        void crearUsuario_nombreMuyCorto_lanzaExcepcion() {
                // Setup: Preparar el escenario
                String nombre = "abcd";
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Usuario(nombre),
                                "Crear usuario con nombre menor a 5 caracteres debe lanzar excepción");
                assertEquals(Usuario.ERROR_NOMBRE_LONGITUD, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }

        @Test
        @DisplayName("Crear usuario con nombre mayor a 25 caracteres lanza excepción")
        void crearUsuario_nombreMuyLargo_lanzaExcepcion() {
                // Setup: Preparar el escenario
                String nombre = "abcdefghijklmnopqrstuvwxyz";
                // Ejercitación y Verificación: Ejecutar y verificar excepción
                var ex = assertThrows(RuntimeException.class, () -> new Usuario(nombre),
                                "Crear usuario con nombre mayor a 25 caracteres debe lanzar excepción");
                assertEquals(Usuario.ERROR_NOMBRE_LONGITUD, ex.getMessage(),
                                "El mensaje de error debe coincidir con la constante definida");
        }
}