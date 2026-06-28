package com.david.ecommerce.application.carrito.dto;

import com.david.ecommerce.domain.carrito.Carrito;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponseDTO {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private List<ItemCarritoDTO> items;
    private BigDecimal total;
    private Integer cantidadTotalItems;

    public static CarritoResponseDTO fromDomain(Carrito carrito) {
        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setUsuarioId(carrito.getUsuarioId());
        dto.setItems(carrito.getItems().stream()
                .map(ItemCarritoDTO::fromDomain)
                .collect(Collectors.toList()));
        dto.setTotal(dto.getItems().stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setCantidadTotalItems(dto.getItems().stream()
                .mapToInt(ItemCarritoDTO::getCantidad)
                .sum());
        return dto;
    }
}
