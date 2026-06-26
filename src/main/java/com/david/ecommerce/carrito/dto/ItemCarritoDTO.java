package com.david.ecommerce.carrito.dto;

import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.ItemCarritoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public ItemCarritoDTO(ItemCarritoEntity item) {
        this.id = item.getId();
        this.productoId = item.getProducto().getId();
        this.productoNombre = item.getProducto().getNombre();
        this.cantidad = item.getCantidad();
        this.precioUnitario = item.getPrecioUnitario();
        this.subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
    }

    public static ItemCarritoDTO fromDomain(ItemCarrito item) {
        ItemCarritoDTO dto = new ItemCarritoDTO();
        dto.setId(item.getId());
        dto.setProductoId(item.getProductoId());
        dto.setProductoNombre(item.getProductoNombre());
        dto.setCantidad(item.getCantidad());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
