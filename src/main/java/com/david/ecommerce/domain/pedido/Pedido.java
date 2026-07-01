package com.david.ecommerce.domain.pedido;

import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.domain.common.EstadoPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private Long id;
    private Long usuarioId;
    private List<DetallePedido> detalles = new ArrayList<>();
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private EstadoPedido estado;

    public Pedido() {}

    public Pedido(Long usuarioId, EstadoPedido estado) {
        this.usuarioId = usuarioId;
        this.estado = estado;
        this.fechaPedido = LocalDateTime.now();
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        calcularTotal();
    }

    public void validar() {
        if (detalles == null || detalles.isEmpty())
            throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
        for (DetallePedido d : detalles) {
            if (d.getCantidad() == null || d.getCantidad() <= 0)
                throw new IllegalArgumentException("La cantidad del detalle debe ser positiva");
        }
    }

    public void removerDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean puedeCancelar() {
        return estado != EstadoPedido.CANCELADO && estado != EstadoPedido.ENTREGADO;
    }

    public void cancelar() {
        if (!puedeCancelar()) {
            throw new ValidacionNegocioException(
                    "No se puede cancelar un pedido en estado: " + estado);
        }
        this.estado = EstadoPedido.CANCELADO;
    }

    public boolean puedePagar() {
        return estado == EstadoPedido.PENDIENTE;
    }

    public void pagar() {
        if (!puedePagar()) {
            throw new ValidacionNegocioException(
                    "Solo se pueden pagar pedidos en estado PENDIENTE. Estado actual: " + estado);
        }
        this.estado = EstadoPedido.PAGADO;
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }
    public BigDecimal getTotal() { return total; }
    private void setTotal(BigDecimal total) { this.total = total; }
    public EstadoPedido getEstado() { return estado; }
    private void setEstado(EstadoPedido estado) { this.estado = estado; }
}
