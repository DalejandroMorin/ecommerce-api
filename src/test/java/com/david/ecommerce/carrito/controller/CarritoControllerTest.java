package com.david.ecommerce.carrito.controller;

import com.david.ecommerce.application.carrito.CarritoUseCase;
import com.david.ecommerce.carrito.dto.CarritoResponseDTO;
import com.david.ecommerce.carrito.dto.ItemCarritoDTO;
import com.david.ecommerce.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CarritoControllerTest {

    private MockMvc mockMvc;
    private CarritoUseCase carritoUseCase;

    @BeforeEach
    void setUp() {
        carritoUseCase = mock(CarritoUseCase.class);
        var controller = new CarritoController(carritoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerCarrito_Retorna200() throws Exception {
        var item = new ItemCarritoDTO(1L, 1L, "Laptop", 2, new BigDecimal("1000"), new BigDecimal("2000"));
        var carrito = new CarritoResponseDTO(1L, 1L, "Juan", List.of(item), new BigDecimal("2000"), 2);

        when(carritoUseCase.obtenerCarrito(1L)).thenReturn(carrito);

        mockMvc.perform(get("/api/carrito").param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].productoNombre").value("Laptop"))
                .andExpect(jsonPath("$.total").value(2000));
    }

    @Test
    void agregarProducto_Exitoso_Retorna200() throws Exception {
        var item = new ItemCarritoDTO(1L, 1L, "Laptop", 2, new BigDecimal("1000"), new BigDecimal("2000"));
        var carrito = new CarritoResponseDTO(1L, 1L, "Juan", List.of(item), new BigDecimal("2000"), 2);

        when(carritoUseCase.agregarProducto(1L, 1L, 2)).thenReturn(carrito);

        mockMvc.perform(post("/api/carrito/agregar")
                        .param("usuarioId", "1")
                        .param("productoId", "1")
                        .param("cantidad", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void actualizarCantidad_Exitoso_Retorna200() throws Exception {
        var item = new ItemCarritoDTO(1L, 1L, "Laptop", 5, new BigDecimal("1000"), new BigDecimal("5000"));
        var carrito = new CarritoResponseDTO(1L, 1L, "Juan", List.of(item), new BigDecimal("5000"), 5);

        when(carritoUseCase.actualizarCantidad(1L, 1L, 5)).thenReturn(carrito);

        mockMvc.perform(put("/api/carrito/items/1")
                        .param("usuarioId", "1")
                        .param("nuevaCantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(5));
    }

    @Test
    void eliminarItem_Exitoso_Retorna200() throws Exception {
        var carrito = new CarritoResponseDTO(1L, 1L, "Juan", List.of(), BigDecimal.ZERO, 0);

        when(carritoUseCase.eliminarItem(1L, 1L)).thenReturn(carrito);

        mockMvc.perform(delete("/api/carrito/items/1")
                        .param("usuarioId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void vaciarCarrito_Exitoso_Retorna204() throws Exception {
        mockMvc.perform(delete("/api/carrito/vaciar")
                        .param("usuarioId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void agregarProducto_CantidadInvalida_Retorna400() throws Exception {
        when(carritoUseCase.agregarProducto(1L, 1L, -1))
                .thenThrow(new IllegalArgumentException("La cantidad debe ser positiva"));

        mockMvc.perform(post("/api/carrito/agregar")
                        .param("usuarioId", "1")
                        .param("productoId", "1")
                        .param("cantidad", "-1"))
                .andExpect(status().isBadRequest());
    }
}
