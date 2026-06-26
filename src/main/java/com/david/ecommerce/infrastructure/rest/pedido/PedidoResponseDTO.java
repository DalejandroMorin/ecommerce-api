package com.david.ecommerce.infrastructure.rest.pedido;

import com.david.ecommerce.domain.pedido.Pedido;
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

    public static PedidoResponseDTO fromDomain(Pedido pedido) {
        return fromDomain(pedido, null);
    }

    public static PedidoResponseDTO fromDomain(Pedido pedido, String usuarioNombre) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setUsuarioId(pedido.getUsuarioId());
        dto.setUsuarioNombre(usuarioNombre);
        dto.setFechaPedido(pedido.getFechaPedido());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado());
        dto.setDetalles(pedido.getDetalles().stream()
                .map(DetallePedidoDTO::fromDomain)
                .collect(Collectors.toList()));
        return dto;
    }
}
