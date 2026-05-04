package com.david.ecommerce.pedido.service;

import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.pedido.model.Pedido;
import com.david.ecommerce.pedido.repository.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PagoSimuladoService {

    private static final Logger log = LoggerFactory.getLogger(PagoSimuladoService.class);

    private final PedidoRepository pedidoRepository;

    @Autowired
    public PagoSimuladoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Simula un pago para un pedido.
     * Reglas de simulación:
     * - Monto total > $50,000 -> 20% de probabilidad de fallo (fondos insuficientes)
     * - Monto total <= $50,000 -> 95% de éxito
     * - Siempre se genera un número de transacción único
     */
    public ResultadoPago procesarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ValidacionNegocioException("Pedido no encontrado"));

        // Validar estado del pedido
        if (pedido.getEstado() != Pedido.EstadoPedido.PENDIENTE) {
            throw new ValidacionNegocioException(
                    "Solo se pueden pagar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
        }

        String numeroTransaccion = UUID.randomUUID().toString();
        boolean exitoso = simularResultado(pedido.getTotal());

        if (exitoso) {
            pedido.setEstado(Pedido.EstadoPedido.PAGADO);
            pedidoRepository.save(pedido);

            log.info(" Pago exitoso - Pedido: {}, Transacción: {}, Monto: ${}",
                    pedidoId, numeroTransaccion, pedido.getTotal());

            return new ResultadoPago(true, numeroTransaccion,
                    "Pago procesado exitosamente por $" + pedido.getTotal(),
                    LocalDateTime.now());
        } else {
            log.warn(" Pago rechazado - Pedido: {}, Transacción: {}, Monto: ${}, Motivo: Fondos insuficientes",
                    pedidoId, numeroTransaccion, pedido.getTotal());

            return new ResultadoPago(false, numeroTransaccion,
                    "Pago rechazado: fondos insuficientes. Intente con un monto menor.",
                    LocalDateTime.now());
        }
    }

    private boolean simularResultado(BigDecimal monto) {
        double probabilidadExito;
        if (monto.compareTo(new BigDecimal("50000")) > 0) {
            probabilidadExito = 0.80; // 80% éxito para montos altos
        } else if (monto.compareTo(new BigDecimal("10000")) > 0) {
            probabilidadExito = 0.90; // 90% éxito para montos medios
        } else {
            probabilidadExito = 0.95; // 95% éxito para montos bajos
        }
        return Math.random() < probabilidadExito;
    }

    /**
     * DTO interno para el resultado del pago
     */
    public record ResultadoPago(
            boolean exitoso,
            String numeroTransaccion,
            String mensaje,
            LocalDateTime fecha
    ) {}
}