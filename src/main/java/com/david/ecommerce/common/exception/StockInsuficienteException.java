package com.david.ecommerce.common.exception;

public class StockInsuficienteException extends BusinessException {
    public StockInsuficienteException(String producto, int disponible, int solicitado) {
        super(String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
                producto, disponible, solicitado));
    }
}