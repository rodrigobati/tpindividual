package unrn.api.exception;

/**
 * Excepción de dominio que indica que una operación no está permitida
 * por las reglas de negocio.
 * 
 * Ejemplos de uso:
 * - Usuario intenta hacer retweet de su propio tweet
 * - Usuario intenta eliminar contenido que no le pertenece
 * - Operaciones que violan restricciones del dominio
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
