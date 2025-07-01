package com.partymap.backend.Exceptions;

/**
 * Excepción personalizada para manejar errores de acceso denegado.
 * Se lanza cuando un usuario intenta acceder a un recurso para el cual no tiene permisos.
 */
public class AccessDeniedException extends RuntimeException {
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
} 