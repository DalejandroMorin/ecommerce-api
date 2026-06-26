package com.david.ecommerce.domain.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    public enum EstadoPedido {
        PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO
    }

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
    }

    public void removerDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
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
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
}
