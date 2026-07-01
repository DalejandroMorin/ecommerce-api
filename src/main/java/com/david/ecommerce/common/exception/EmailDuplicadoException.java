package com.david.ecommerce.common.exception;

public class EmailDuplicadoException extends BusinessException {
    public EmailDuplicadoException(String email) {
        super("El email '" + email + "' ya está registrado");
    }
}