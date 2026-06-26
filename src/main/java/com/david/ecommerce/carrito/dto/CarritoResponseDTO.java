package com.david.ecommerce.carrito.dto;

import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.CarritoEntity;
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

    public CarritoResponseDTO(CarritoEntity carrito) {
        this.id = carrito.getId();
        this.usuarioId = carrito.getUsuario().getId();
        this.usuarioNombre = carrito.getUsuario().getNombre();
        this.items = carrito.getItems().stream()
                .map(ItemCarritoDTO::new)
                .collect(Collectors.toList());
        this.total = items.stream()
                .map(ItemCarritoDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.cantidadTotalItems = items.stream()
                .mapToInt(ItemCarritoDTO::getCantidad)
                .sum();
    }

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
