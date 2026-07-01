package com.david.ecommerce.domain.carrito;

import java.util.ArrayList;
import java.util.List;

public class Carrito {

    private Long id;
    private Long usuarioId;
    private List<ItemCarrito> items = new ArrayList<>();

    public Carrito() {}

    public void agregarItem(ItemCarrito item) {
        items.add(item);
    }

    public void validar() {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("El carrito no puede estar vacío");
    }

    public void removerItem(ItemCarrito item) {
        items.remove(item);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public List<ItemCarrito> getItems() { return items; }
    public void setItems(List<ItemCarrito> items) { this.items = items; }
}
