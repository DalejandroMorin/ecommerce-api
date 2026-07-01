package com.david.ecommerce.domain.pedido;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

    @Test
    @DisplayName("validar() con detalles nulos lanza IllegalArgumentException")
    void validar_DetallesNulos_LanzaExcepcion() {
        Pedido pedido = new Pedido();
        pedido.setDetalles(null);

        assertThatThrownBy(pedido::validar)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos");
    }

    @Test
    @DisplayName("validar() con detalles vacíos lanza IllegalArgumentException")
    void validar_DetallesVacios_LanzaExcepcion() {
        Pedido pedido = new Pedido();
        pedido.setDetalles(new ArrayList<>());

        assertThatThrownBy(pedido::validar)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos");
    }

    @Test
    @DisplayName("validar() con cantidad inválida lanza IllegalArgumentException")
    void validar_CantidadInvalida_LanzaExcepcion() {
        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido(1L, "Producto", -1,
                new BigDecimal("10.00"), new BigDecimal("-10.00"));
        pedido.agregarDetalle(detalle);

        assertThatThrownBy(pedido::validar)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cantidad");
    }

    @Test
    @DisplayName("validar() con detalles válidos no lanza excepción")
    void validar_Valido_NoLanzaExcepcion() {
        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido(1L, "Producto", 2,
                new BigDecimal("10.00"), new BigDecimal("20.00"));
        pedido.agregarDetalle(detalle);

        assertThatCode(pedido::validar).doesNotThrowAnyException();
    }
}
