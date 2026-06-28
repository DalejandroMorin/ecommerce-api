package com.david.ecommerce.infrastructure.rest.producto;

import com.david.ecommerce.application.producto.ProductoUseCase;
import com.david.ecommerce.application.producto.dto.ProductoRequestDTO;
import com.david.ecommerce.application.producto.dto.ProductoResponseDTO;
import com.david.ecommerce.common.exception.GlobalExceptionHandler;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.domain.producto.Producto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductoControllerTest {

    private MockMvc mockMvc;
    private ProductoUseCase productoUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        productoUseCase = mock(ProductoUseCase.class);
        var controller = new ProductoController(productoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(new LocalValidatorFactoryBean())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("listarTodos retorna 200 con lista de productos")
    void listarTodos_Retorna200() throws Exception {
        var producto = new ProductoResponseDTO(1L, "Laptop", "Laptop gamer", new BigDecimal("1500"),
                10, null, Producto.Categoria.ELECTRONICA, LocalDateTime.now(), LocalDateTime.now());

        when(productoUseCase.obtenerTodos()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[0].precio").value(1500));
    }

    @Test
    @DisplayName("obtenerPorId con ID existente retorna 200")
    void obtenerPorId_Existente_Retorna200() throws Exception {
        var producto = new ProductoResponseDTO(1L, "Laptop", null, new BigDecimal("1500"),
                10, null, Producto.Categoria.ELECTRONICA, null, null);

        when(productoUseCase.obtenerPorId(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    @DisplayName("obtenerPorId con ID inexistente retorna 404")
    void obtenerPorId_NoExistente_Retorna404() throws Exception {
        when(productoUseCase.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Producto", 99L));

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Producto no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("crear con datos válidos retorna 201")
    void crear_Exitoso_Retorna201() throws Exception {
        var request = new ProductoRequestDTO("Laptop", "Laptop gamer", new BigDecimal("1500"), 10, null, Producto.Categoria.ELECTRONICA);
        var response = new ProductoResponseDTO(1L, "Laptop", "Laptop gamer", new BigDecimal("1500"),
                10, null, Producto.Categoria.ELECTRONICA, LocalDateTime.now(), LocalDateTime.now());

        when(productoUseCase.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    @DisplayName("crear con nombre vacío retorna 400")
    void crear_ConNombreVacio_Retorna400() throws Exception {
        var request = new ProductoRequestDTO("", "Desc", new BigDecimal("100"), 5, null, Producto.Categoria.OTROS);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("crear con precio nulo retorna 400")
    void crear_ConPrecioNulo_Retorna400() throws Exception {
        var request = new ProductoRequestDTO("Laptop", "Desc", null, 5, null, Producto.Categoria.OTROS);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("crear con stock negativo retorna 400")
    void crear_ConStockNegativo_Retorna400() throws Exception {
        var request = new ProductoRequestDTO("Laptop", "Desc", new BigDecimal("100"), -1, null, Producto.Categoria.OTROS);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("actualizar con datos válidos retorna 200")
    void actualizar_Exitoso_Retorna200() throws Exception {
        var request = new ProductoRequestDTO("Laptop Pro", "Actualizada", new BigDecimal("2000"), 5, null, Producto.Categoria.ELECTRONICA);
        var response = new ProductoResponseDTO(1L, "Laptop Pro", "Actualizada", new BigDecimal("2000"),
                5, null, Producto.Categoria.ELECTRONICA, LocalDateTime.now(), LocalDateTime.now());

        when(productoUseCase.actualizar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop Pro"));
    }

    @Test
    @DisplayName("eliminar retorna 204")
    void eliminar_Exitoso_Retorna204() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("buscarPorNombre retorna 200 con resultados")
    void buscarPorNombre_Retorna200() throws Exception {
        var producto = new ProductoResponseDTO(1L, "Laptop", null, new BigDecimal("1500"),
                10, null, Producto.Categoria.ELECTRONICA, null, null);

        when(productoUseCase.buscarConFiltros(eq("Laptop"), any(), any(), any(), any()))
                .thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos/buscar").param("nombre", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"));
    }
}
