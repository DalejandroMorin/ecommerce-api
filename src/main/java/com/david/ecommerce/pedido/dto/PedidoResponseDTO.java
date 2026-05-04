package com.david.ecommerce.pedido.dto;

import com.david.ecommerce.pedido.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private Pedido.EstadoPedido estado;
    private List<DetallePedidoDTO> detalles;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.usuarioId = pedido.getUsuario().getId();
        this.usuarioNombre = pedido.getUsuario().getNombre();
        this.fechaPedido = pedido.getFechaPedido();
        this.total = pedido.getTotal();
        this.estado = pedido.getEstado();
        this.detalles = pedido.getDetalles().stream()
                .map(DetallePedidoDTO::new)
                .collect(Collectors.toList());
    }
}