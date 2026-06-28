package com.david.ecommerce.application.pedido.dto;

import com.david.ecommerce.domain.pedido.DetallePedido;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public static DetallePedidoDTO fromDomain(DetallePedido detalle) {
        DetallePedidoDTO dto = new DetallePedidoDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProductoId());
        dto.setProductoNombre(detalle.getProductoNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }

    public DetallePedidoDTO(DetallePedido detalle) {
        this.id = detalle.getId();
        this.productoId = detalle.getProductoId();
        this.productoNombre = detalle.getProductoNombre();
        this.cantidad = detalle.getCantidad();
        this.precioUnitario = detalle.getPrecioUnitario();
        this.subtotal = detalle.getSubtotal();
    }
}
