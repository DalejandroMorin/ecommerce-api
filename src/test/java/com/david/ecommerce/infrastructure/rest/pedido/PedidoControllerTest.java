package com.david.ecommerce.infrastructure.rest.pedido;

import com.david.ecommerce.application.pedido.PagoSimuladoUseCase;
import com.david.ecommerce.application.pedido.PedidoUseCase;
import com.david.ecommerce.application.pedido.dto.DetallePedidoDTO;
import com.david.ecommerce.application.pedido.dto.PedidoResponseDTO;
import com.david.ecommerce.common.exception.GlobalExceptionHandler;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.domain.pedido.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PedidoControllerTest {

    private MockMvc mockMvc;
    private PedidoUseCase pedidoUseCase;
    private PagoSimuladoUseCase pagoSimuladoUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        pedidoUseCase = mock(PedidoUseCase.class);
        pagoSimuladoUseCase = mock(PagoSimuladoUseCase.class);
        var controller = new PedidoController(pedidoUseCase, pagoSimuladoUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearDesdeCarrito_Exitoso_Retorna201() throws Exception {
        var detalle = new DetallePedidoDTO(1L, 1L, "Laptop", 2, new BigDecimal("1000"), new BigDecimal("2000"));
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PENDIENTE, List.of(detalle));

        when(pedidoUseCase.crearPedidoDesdeCarrito(1L)).thenReturn(pedido);

        mockMvc.perform(post("/api/pedidos/desde-carrito").param("usuarioId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(2000))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void crearDesdeCarrito_StockInsuficiente_Retorna400() throws Exception {
        when(pedidoUseCase.crearPedidoDesdeCarrito(1L))
                .thenThrow(new StockInsuficienteException("Laptop", 5, 10));

        mockMvc.perform(post("/api/pedidos/desde-carrito").param("usuarioId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
    }

    @Test
    void listarTodos_Retorna200() throws Exception {
        var detalle = new DetallePedidoDTO(1L, 1L, "Laptop", 2, new BigDecimal("1000"), new BigDecimal("2000"));
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PENDIENTE, List.of(detalle));

        when(pedidoUseCase.obtenerTodos()).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(2000));
    }

    @Test
    void obtenerPorId_Existente_Retorna200() throws Exception {
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PENDIENTE, List.of());

        when(pedidoUseCase.obtenerPorId(1L)).thenReturn(pedido);

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_NoExistente_Retorna404() throws Exception {
        when(pedidoUseCase.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Pedido", 99L));

        mockMvc.perform(get("/api/pedidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorUsuario_Retorna200() throws Exception {
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PENDIENTE, List.of());

        when(pedidoUseCase.obtenerPorUsuario(1L)).thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/pedidos/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    void historial_SinFiltros_Retorna200() throws Exception {
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PENDIENTE, List.of());

        when(pedidoUseCase.obtenerHistorial(eq(1L), isNull(), isNull(), isNull()))
                .thenReturn(List.of(pedido));

        mockMvc.perform(get("/api/pedidos/historial/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(1));
    }

    @Test
    void actualizarEstado_Exitoso_Retorna200() throws Exception {
        var pedido = new PedidoResponseDTO(1L, 1L, null, LocalDateTime.now(), new BigDecimal("2000"),
                Pedido.EstadoPedido.PAGADO, List.of());

        when(pedidoUseCase.actualizarEstado(1L, Pedido.EstadoPedido.PAGADO)).thenReturn(pedido);

        mockMvc.perform(patch("/api/pedidos/1/estado")
                        .param("nuevoEstado", "PAGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));
    }

    @Test
    void pagarPedido_Exitoso_Retorna200() throws Exception {
        var resultado = new PagoSimuladoUseCase.ResultadoPago(true, "TXN-123", "Pago exitoso", LocalDateTime.now());

        when(pagoSimuladoUseCase.procesarPago(1L)).thenReturn(resultado);

        mockMvc.perform(post("/api/pedidos/1/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.numeroTransaccion").value("TXN-123"));
    }

    @Test
    void pagarPedido_Rechazado_Retorna402() throws Exception {
        var resultado = new PagoSimuladoUseCase.ResultadoPago(false, "TXN-456", "Fondos insuficientes", LocalDateTime.now());

        when(pagoSimuladoUseCase.procesarPago(1L)).thenReturn(resultado);

        mockMvc.perform(post("/api/pedidos/1/pagar"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.exitoso").value(false));
    }

    @Test
    void cancelarPedido_Exitoso_Retorna200() throws Exception {
        mockMvc.perform(post("/api/pedidos/1/cancelar"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelarPedido_YaCancelado_Retorna400() throws Exception {
        doThrow(new ValidacionNegocioException("El pedido ya está cancelado"))
                .when(pedidoUseCase).cancelarPedido(1L);

        mockMvc.perform(post("/api/pedidos/1/cancelar"))
                .andExpect(status().isBadRequest());
    }
}
