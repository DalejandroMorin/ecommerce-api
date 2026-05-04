package com.david.ecommerce.carrito.dto;

import com.david.ecommerce.carrito.model.ItemCarrito;
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

    public ItemCarritoDTO(ItemCarrito item) {
        this.id = item.getId();
        this.productoId = item.getProducto().getId();
        this.productoNombre = item.getProducto().getNombre();
        this.cantidad = item.getCantidad();
        this.precioUnitario = item.getPrecioUnitario();
        this.subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
    }
}