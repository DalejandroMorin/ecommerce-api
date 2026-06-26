package com.david.ecommerce.application.pedido;

import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.domain.pedido.Pedido;
import com.david.ecommerce.domain.pedido.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PagoSimuladoUseCase {

    private static final Logger log = LoggerFactory.getLogger(PagoSimuladoUseCase.class);

    private final PedidoRepository pedidoRepository;

    public PagoSimuladoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public ResultadoPago procesarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ValidacionNegocioException("Pedido no encontrado"));

        if (pedido.getEstado() != Pedido.EstadoPedido.PENDIENTE) {
            throw new ValidacionNegocioException(
                    "Solo se pueden pagar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
        }

        String numeroTransaccion = UUID.randomUUID().toString();
        boolean exitoso = simularResultado(pedido.getTotal());

        if (exitoso) {
            pedido.setEstado(Pedido.EstadoPedido.PAGADO);
            pedidoRepository.save(pedido);

            log.info("Pago exitoso - Pedido: {}, Transacción: {}, Monto: ${}",
                    pedidoId, numeroTransaccion, pedido.getTotal());

            return new ResultadoPago(true, numeroTransaccion,
                    "Pago procesado exitosamente por $" + pedido.getTotal(),
                    LocalDateTime.now());
        } else {
            log.warn("Pago rechazado - Pedido: {}, Transacción: {}, Monto: ${}, Motivo: Fondos insuficientes",
                    pedidoId, numeroTransaccion, pedido.getTotal());

            return new ResultadoPago(false, numeroTransaccion,
                    "Pago rechazado: fondos insuficientes. Intente con un monto menor.",
                    LocalDateTime.now());
        }
    }

    private boolean simularResultado(BigDecimal monto) {
        double probabilidadExito;
        if (monto.compareTo(new BigDecimal("50000")) > 0) {
            probabilidadExito = 0.80;
        } else if (monto.compareTo(new BigDecimal("10000")) > 0) {
            probabilidadExito = 0.90;
        } else {
            probabilidadExito = 0.95;
        }
        return Math.random() < probabilidadExito;
    }

    public record ResultadoPago(
            boolean exitoso,
            String numeroTransaccion,
            String mensaje,
            LocalDateTime fecha
    ) {}
}
