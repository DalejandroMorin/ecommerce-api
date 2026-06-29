package com.david.ecommerce.domain.carrito;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarritoTest {

    @Test
    @DisplayName("validar() con items nulos lanza IllegalArgumentException")
    void validar_ItemsNulos_LanzaExcepcion() {
        Carrito carrito = new Carrito();
        carrito.setItems(null);

        assertThatThrownBy(carrito::validar)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("validar() con items vacíos lanza IllegalArgumentException")
    void validar_ItemsVacios_LanzaExcepcion() {
        Carrito carrito = new Carrito();
        carrito.setItems(new ArrayList<>());

        assertThatThrownBy(carrito::validar)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("validar() con items válidos no lanza excepción")
    void validar_ConItems_NoLanzaExcepcion() {
        Carrito carrito = new Carrito();
        carrito.setItems(new ArrayList<>());
        carrito.agregarItem(new ItemCarrito(1L, "Producto", 2, new BigDecimal("10.00")));

        assertThatCode(carrito::validar).doesNotThrowAnyException();
    }
}
