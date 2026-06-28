package com.david.ecommerce.infrastructure.rest.pedido;

import com.david.ecommerce.application.pedido.PagoSimuladoUseCase;
import com.david.ecommerce.application.pedido.PedidoUseCase;
import com.david.ecommerce.application.pedido.dto.PedidoResponseDTO;
import com.david.ecommerce.domain.pedido.Pedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final PedidoUseCase pedidoUseCase;
    private final PagoSimuladoUseCase pagoSimuladoUseCase;

    public PedidoController(PedidoUseCase pedidoUseCase,
                            PagoSimuladoUseCase pagoSimuladoUseCase) {
        this.pedidoUseCase = pedidoUseCase;
        this.pagoSimuladoUseCase = pagoSimuladoUseCase;
    }

    @PostMapping("/desde-carrito")
    @Operation(summary = "Crear un pedido desde el carrito del usuario")
    public ResponseEntity<PedidoResponseDTO> crearDesdeCarrito(@RequestParam Long usuarioId) {
        PedidoResponseDTO pedido = pedidoUseCase.crearPedidoDesdeCarrito(usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoUseCase.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por ID")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoUseCase.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar pedidos de un usuario")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoUseCase.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/historial/{usuarioId}")
    @Operation(summary = "Consultar historial de pedidos de un usuario")
    public ResponseEntity<List<PedidoResponseDTO>> historial(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Pedido.EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        List<PedidoResponseDTO> historial = pedidoUseCase.obtenerHistorial(usuarioId, estado, desde, hasta);
        return ResponseEntity.ok(historial);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar el estado de un pedido")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(@PathVariable Long id,
                                                              @RequestParam Pedido.EstadoPedido nuevoEstado) {
        return ResponseEntity.ok(pedidoUseCase.actualizarEstado(id, nuevoEstado));
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Procesar pago simulado de un pedido")
    public ResponseEntity<PagoSimuladoUseCase.ResultadoPago> pagarPedido(@PathVariable Long id) {
        PagoSimuladoUseCase.ResultadoPago resultado = pagoSimuladoUseCase.procesarPago(id);

        if (resultado.exitoso()) {
            return ResponseEntity.ok(resultado);
        } else {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(resultado);
        }
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar un pedido y devolver stock")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoUseCase.cancelarPedido(id);
        return ResponseEntity.ok().build();
    }
}
