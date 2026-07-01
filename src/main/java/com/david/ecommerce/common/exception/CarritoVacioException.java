package com.david.ecommerce.common.exception;

public class CarritoVacioException extends BusinessException {
    public CarritoVacioException(Long usuarioId) {
        super("El carrito del usuario " + usuarioId + " está vacío");
    }
}