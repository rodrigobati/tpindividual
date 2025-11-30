package unrn.api.exception;

/**
 * Excepción de dominio que indica que un usuario no fue encontrado.
 * 
 * Se usa para distinguir errores de negocio (usuario inexistente)
 * de errores técnicos internos del sistema.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
