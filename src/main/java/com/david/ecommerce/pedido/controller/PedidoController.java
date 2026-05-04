package com.david.ecommerce.pedido.controller;

import com.david.ecommerce.pedido.dto.PedidoResponseDTO;
import com.david.ecommerce.pedido.model.Pedido;
import com.david.ecommerce.pedido.service.PagoSimuladoService;
import com.david.ecommerce.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestión de pedidos y pagos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PagoSimuladoService pagoSimuladoService;

    @Autowired
    public PedidoController(PedidoService pedidoService,
                            PagoSimuladoService pagoSimuladoService) {
        this.pedidoService = pedidoService;
        this.pagoSimuladoService = pagoSimuladoService;
    }

    @PostMapping("/desde-carrito")
    @Operation(summary = "Crear un pedido desde el carrito del usuario")
    public ResponseEntity<PedidoResponseDTO> crearDesdeCarrito(@RequestParam Long usuarioId) {
        PedidoResponseDTO pedido = pedidoService.crearPedidoDesdeCarrito(usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por ID")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar pedidos de un usuario")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/historial/{usuarioId}")
    @Operation(summary = "Consultar historial de pedidos de un usuario")
    public ResponseEntity<List<PedidoResponseDTO>> historial(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Pedido.EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        List<PedidoResponseDTO> historial = pedidoService.obtenerHistorial(usuarioId, estado, desde, hasta);
        return ResponseEntity.ok(historial);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un pedido")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(@PathVariable Long id,
                                                              @RequestParam Pedido.EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, nuevoEstado));
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Procesar pago simulado de un pedido")
    public ResponseEntity<PagoSimuladoService.ResultadoPago> pagarPedido(@PathVariable Long id) {
        PagoSimuladoService.ResultadoPago resultado = pagoSimuladoService.procesarPago(id);

        if (resultado.exitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(resultado);
        }
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar un pedido y devolver stock")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.ok().build();
    }
}