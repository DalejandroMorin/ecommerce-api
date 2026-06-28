package com.david.ecommerce.application.carrito.dto;

import com.david.ecommerce.domain.carrito.ItemCarrito;
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
