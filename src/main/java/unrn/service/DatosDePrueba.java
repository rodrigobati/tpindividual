package unrn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.ReTweet;
import unrn.model.Tweet;
import unrn.model.Usuario;
import unrn.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generador de datos de prueba para entorno de desarrollo.
 * 
 * Estrategia:
 * - Se ejecuta solo si app.load-test-data=true en application.properties
 * - Es idempotente: verifica si los datos ya existen antes de crearlos
 * - Crea usuarios en el backend que corresponden a los usuarios de Keycloak
 * - Genera tweets, retweets y relaciones de seguimiento de ejemplo
 * - Usa los servicios de aplicación existentes para respetar reglas de dominio
 * 
 * IMPORTANTE: Esta clase NO depende de @Profile("dev") para permitir
 * cargar datos de prueba sin desactivar OAuth2
 */
@Component
@ConditionalOnProperty(name = "app.load-test-data", havingValue = "true")
public class DatosDePrueba implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDePrueba.class);

    private final RepositorioUsuarios repositorioUsuarios;
    private final RepositorioTweets repositorioTweets;
    private final RepositorioRetweets repositorioRetweets;
    private final RepositorioFollows repositorioFollows;
    private final ServicioTweets servicioTweets;
    private final ServicioSocial servicioSocial;

    public DatosDePrueba(RepositorioUsuarios repositorioUsuarios,
            RepositorioTweets repositorioTweets,
            RepositorioRetweets repositorioRetweets,
            RepositorioFollows repositorioFollows,
            ServicioTweets servicioTweets,
            ServicioSocial servicioSocial) {

        this.repositorioUsuarios = repositorioUsuarios;
        this.repositorioTweets = repositorioTweets;
        this.repositorioRetweets = repositorioRetweets;
        this.repositorioFollows = repositorioFollows;
        this.servicioTweets = servicioTweets;
        this.servicioSocial = servicioSocial;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Iniciando carga de datos de prueba ===");

        // 1. Crear/verificar usuarios
        Map<String, Usuario> usuarios = crearUsuariosDePrueba();

        // 2. Crear tweets de ejemplo
        Map<String, List<Tweet>> tweetsPorUsuario = crearTweetsDePrueba(usuarios);

        // 3. Crear retweets cruzados
        crearRetweetsDePrueba(usuarios, tweetsPorUsuario);

        // 4. Crear relaciones de seguimiento
        crearRelacionesDeSeguimiento(usuarios);

        log.info("=== Carga de datos de prueba completada ===");
    }

    /**
     * Crea o busca usuarios correspondientes a los usuarios de Keycloak.
     * Los keycloakId deben coincidir con los IDs definidos en realm-export.json
     */
    private Map<String, Usuario> crearUsuariosDePrueba() {
        log.info(">> Creando/verificando usuarios de prueba...");

        Map<String, Usuario> usuarios = new HashMap<>();

        // Definir usuarios con sus keycloakIds del realm-export.json
        String[][] datosUsuarios = {
                // { keycloakId, nombreUsuario, email, firstName, lastName }
                { "1a51c71a-319e-483c-aa64-aaf11899efb1", "carlos.gomez", "carlos.gomez@minitwitter.com", "Carlos",
                        "Gomez" },
                { "2b51c71a-319e-483c-bb64-bbf11899efb1", "maria.fernandez", "maria.fernandez@minitwitter.com",
                        "Maria", "Fernandez" },
                { "3c51c71a-319e-483c-cc64-ccf11899efb1", "juan.perez", "juan.perez@minitwitter.com", "Juan",
                        "Perez" },
                { "4d51c71a-319e-483c-dd64-ddf11899efb1", "ana.rodriguez", "ana.rodriguez@minitwitter.com", "Ana",
                        "Rodriguez" },
                { "5e51c71a-319e-483c-ee64-eef11899efb1", "luis.sanchez", "luis.sanchez@minitwitter.com", "Luis",
                        "Sanchez" },
                { "de51c71a-319e-483c-dd64-dbf11899efb1", "usuariocliente", "usuariocliente@gmail.com", "Jose",
                        "Lopez" }
        };

        for (String[] datos : datosUsuarios) {
            String keycloakId = datos[0];
            String nombreUsuario = datos[1];
            String email = datos[2];
            String firstName = datos[3];
            String lastName = datos[4];

            Usuario usuario;

            // Verificar si el usuario ya existe (idempotencia)
            if (repositorioUsuarios.existePorKeycloakId(keycloakId)) {
                usuario = repositorioUsuarios.buscarPorKeycloakId(keycloakId);
                log.info("   Usuario '{}' ya existe (ID: {})", nombreUsuario, usuario.id());
            } else {
                // Crear nuevo usuario
                String biografia = "Hola, soy " + firstName + " y estoy probando MiniTwitter";
                usuario = new Usuario(
                        keycloakId,
                        nombreUsuario,
                        email,
                        LocalDateTime.now(),
                        biografia,
                        null // avatarUrl
                );
                usuario = repositorioUsuarios.guardar(usuario);
                log.info("   Usuario '{}' creado (ID: {})", nombreUsuario, usuario.id());
            }

            usuarios.put(nombreUsuario, usuario);
        }

        return usuarios;
    }

    /**
     * Crea tweets de ejemplo para cada usuario.
     * Verifica si ya tienen tweets para evitar duplicados.
     */
    private Map<String, List<Tweet>> crearTweetsDePrueba(Map<String, Usuario> usuarios) {
        log.info(">> Creando tweets de prueba...");

        Map<String, List<Tweet>> tweetsPorUsuario = new HashMap<>();

        // Contenidos de ejemplo por usuario
        Map<String, String[]> contenidosPorUsuario = new HashMap<>();
        contenidosPorUsuario.put("carlos.gomez", new String[] {
                "¡Hola MiniTwitter! Primer tweet desde mi cuenta 🚀",
                "Aprendiendo sobre arquitectura de software. ¡Es fascinante!",
                "El café de la mañana es esencial para programar bien ☕"
        });

        contenidosPorUsuario.put("maria.fernandez", new String[] {
                "Nueva en MiniTwitter. ¡Qué emoción estar aquí! 🎉",
                "Trabajando en un proyecto de Spring Boot. ¡Me encanta!",
                "¿Alguien más usa Keycloak para autenticación? Es genial"
        });

        contenidosPorUsuario.put("juan.perez", new String[] {
                "Mi primer tweet en esta plataforma. ¡Saludos a todos!",
                "Reflexionando sobre patrones de diseño y DDD",
                "Los tests unitarios son tus mejores amigos 🧪"
        });

        contenidosPorUsuario.put("ana.rodriguez", new String[] {
                "¡Hola comunidad! Lista para compartir ideas",
                "Clean Architecture hace la diferencia en proyectos grandes",
                "Documentar el código es un acto de amor hacia tu yo futuro 📝"
        });

        contenidosPorUsuario.put("luis.sanchez", new String[] {
                "Empezando mi aventura en MiniTwitter",
                "Docker Compose simplifica tanto el desarrollo local 🐳",
                "La separación de responsabilidades es clave en Spring"
        });

        contenidosPorUsuario.put("usuariocliente", new String[] {
                "Tweet de prueba del usuario cliente",
                "Probando funcionalidades de MiniTwitter"
        });

        // Crear tweets para cada usuario
        for (Map.Entry<String, String[]> entry : contenidosPorUsuario.entrySet()) {
            String nombreUsuario = entry.getKey();
            String[] contenidos = entry.getValue();
            Usuario usuario = usuarios.get(nombreUsuario);

            if (usuario == null) {
                continue;
            }

            List<Tweet> tweetsDelUsuario = new ArrayList<>();

            // Verificar si el usuario ya tiene tweets (idempotencia)
            List<Tweet> tweetsExistentes = repositorioTweets.buscarTweetsDeAutores(List.of(usuario), 100);
            if (!tweetsExistentes.isEmpty()) {
                log.info("   Usuario '{}' ya tiene {} tweets", nombreUsuario, tweetsExistentes.size());
                tweetsDelUsuario.addAll(tweetsExistentes);
            } else {
                // Crear tweets nuevos
                for (String contenido : contenidos) {
                    Tweet tweet = servicioTweets.publicarTweet(usuario.keycloakId(), contenido);
                    tweetsDelUsuario.add(tweet);
                    log.info("   Tweet creado para '{}': '{}'", nombreUsuario,
                            contenido.substring(0, Math.min(50, contenido.length())) + "...");
                }
            }

            tweetsPorUsuario.put(nombreUsuario, tweetsDelUsuario);
        }

        return tweetsPorUsuario;
    }

    /**
     * Crea retweets cruzados entre usuarios.
     * Respeta la regla de negocio: no se puede retweetear tweets propios.
     */
    private void crearRetweetsDePrueba(Map<String, Usuario> usuarios, Map<String, List<Tweet>> tweetsPorUsuario) {
        log.info(">> Creando retweets de prueba...");

        // Definir pares de retweets: [usuario que retweetea, usuario cuyo tweet se
        // retweetea, índice del tweet]
        Object[][] retweets = {
                { "maria.fernandez", "carlos.gomez", 0 }, // Maria retweetea primer tweet de Carlos
                { "carlos.gomez", "maria.fernandez", 1 }, // Carlos retweetea segundo tweet de Maria
                { "juan.perez", "ana.rodriguez", 0 }, // Juan retweetea primer tweet de Ana
                { "ana.rodriguez", "luis.sanchez", 1 }, // Ana retweetea segundo tweet de Luis
                { "luis.sanchez", "juan.perez", 0 }, // Luis retweetea primer tweet de Juan
                { "usuariocliente", "carlos.gomez", 1 } // Cliente retweetea segundo tweet de Carlos
        };

        for (Object[] retweet : retweets) {
            String nombreUsuarioQueRetweetea = (String) retweet[0];
            String nombreUsuarioDeTweetOriginal = (String) retweet[1];
            int indiceTweet = (int) retweet[2];

            Usuario usuarioQueRetweetea = usuarios.get(nombreUsuarioQueRetweetea);
            List<Tweet> tweetsOriginales = tweetsPorUsuario.get(nombreUsuarioDeTweetOriginal);

            if (usuarioQueRetweetea == null || tweetsOriginales == null
                    || tweetsOriginales.size() <= indiceTweet) {
                continue;
            }

            Tweet tweetOriginal = tweetsOriginales.get(indiceTweet);

            // Verificar si ya existe el retweet (idempotencia)
            if (repositorioRetweets.existeRetweetDeUsuarioSobreTweet(usuarioQueRetweetea, tweetOriginal)) {
                log.info("   Retweet de '{}' sobre tweet de '{}' ya existe",
                        nombreUsuarioQueRetweetea, nombreUsuarioDeTweetOriginal);
                continue;
            }

            try {
                // Crear retweet usando el servicio (respeta reglas de negocio)
                ReTweet nuevoRetweet = servicioTweets.retweetear(
                        usuarioQueRetweetea.keycloakId(),
                        tweetOriginal.id());

                log.info("   Retweet creado: '{}' retwetteó tweet de '{}'",
                        nombreUsuarioQueRetweetea, nombreUsuarioDeTweetOriginal);
            } catch (Exception e) {
                log.warn("   No se pudo crear retweet: {}", e.getMessage());
            }
        }
    }

    /**
     * Crea relaciones de seguimiento entre usuarios.
     * Forma un grafo social de ejemplo.
     */
    private void crearRelacionesDeSeguimiento(Map<String, Usuario> usuarios) {
        log.info(">> Creando relaciones de seguimiento...");

        // Definir relaciones: [seguidor, seguido]
        String[][] relaciones = {
                { "carlos.gomez", "maria.fernandez" }, // Carlos sigue a Maria
                { "carlos.gomez", "juan.perez" }, // Carlos sigue a Juan
                { "maria.fernandez", "carlos.gomez" }, // Maria sigue a Carlos
                { "maria.fernandez", "ana.rodriguez" }, // Maria sigue a Ana
                { "juan.perez", "maria.fernandez" }, // Juan sigue a Maria
                { "juan.perez", "luis.sanchez" }, // Juan sigue a Luis
                { "ana.rodriguez", "carlos.gomez" }, // Ana sigue a Carlos
                { "ana.rodriguez", "maria.fernandez" }, // Ana sigue a Maria
                { "luis.sanchez", "juan.perez" }, // Luis sigue a Juan
                { "luis.sanchez", "ana.rodriguez" }, // Luis sigue a Ana
                { "usuariocliente", "carlos.gomez" }, // Cliente sigue a Carlos
                { "usuariocliente", "maria.fernandez" } // Cliente sigue a Maria
        };

        for (String[] relacion : relaciones) {
            String nombreSeguidor = relacion[0];
            String nombreSeguido = relacion[1];

            Usuario seguidor = usuarios.get(nombreSeguidor);
            Usuario seguido = usuarios.get(nombreSeguido);

            if (seguidor == null || seguido == null) {
                continue;
            }

            // Verificar si ya existe la relación (idempotencia)
            if (repositorioFollows.existeFollowEntre(seguidor, seguido)) {
                log.info("   Relación '{}' -> '{}' ya existe", nombreSeguidor, nombreSeguido);
                continue;
            }

            try {
                // Crear relación usando el servicio (respeta reglas de negocio)
                servicioSocial.seguir(seguidor.keycloakId(), seguido.id());
                log.info("   Relación creada: '{}' ahora sigue a '{}'", nombreSeguidor, nombreSeguido);
            } catch (Exception e) {
                log.warn("   No se pudo crear relación: {}", e.getMessage());
            }
        }
    }
}
