package com.david.ecommerce.common.exception;

public class RecursoNoEncontradoException extends BusinessException {
    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " no encontrado con ID: " + id);
    }

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}