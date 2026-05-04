package com.david.ecommerce.carrito.controller;

import com.david.ecommerce.carrito.dto.CarritoResponseDTO;
import com.david.ecommerce.carrito.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(usuarioId));
    }

    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(@RequestParam Long usuarioId,
                                                              @RequestParam Long productoId,
                                                              @RequestParam Integer cantidad) {
        return ResponseEntity.ok(carritoService.agregarProducto(usuarioId, productoId, cantidad));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> actualizarCantidad(@PathVariable Long itemId,
                                                                 @RequestParam Long usuarioId,
                                                                 @RequestParam Integer nuevaCantidad) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(usuarioId, itemId, nuevaCantidad));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarItem(@PathVariable Long itemId,
                                                           @RequestParam Long usuarioId) {
        return ResponseEntity.ok(carritoService.eliminarItem(usuarioId, itemId));
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@RequestParam Long usuarioId) {
        carritoService.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }
}