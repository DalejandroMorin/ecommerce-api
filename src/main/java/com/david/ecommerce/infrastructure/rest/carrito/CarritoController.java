package com.david.ecommerce.infrastructure.rest.carrito;

import com.david.ecommerce.application.carrito.CarritoUseCase;
import com.david.ecommerce.application.carrito.dto.CarritoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoUseCase carritoUseCase;

    public CarritoController(CarritoUseCase carritoUseCase) {
        this.carritoUseCase = carritoUseCase;
    }

    @GetMapping
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@RequestParam Long usuarioId) {
        return ResponseEntity.ok(carritoUseCase.obtenerCarrito(usuarioId));
    }

    @PostMapping("/agregar")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(@RequestParam Long usuarioId,
                                                               @RequestParam Long productoId,
                                                               @RequestParam Integer cantidad) {
        return ResponseEntity.ok(carritoUseCase.agregarProducto(usuarioId, productoId, cantidad));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> actualizarCantidad(@PathVariable Long itemId,
                                                                  @RequestParam Long usuarioId,
                                                                  @RequestParam Integer nuevaCantidad) {
        return ResponseEntity.ok(carritoUseCase.actualizarCantidad(usuarioId, itemId, nuevaCantidad));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoResponseDTO> eliminarItem(@PathVariable Long itemId,
                                                           @RequestParam Long usuarioId) {
        return ResponseEntity.ok(carritoUseCase.eliminarItem(usuarioId, itemId));
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<Void> vaciarCarrito(@RequestParam Long usuarioId) {
        carritoUseCase.vaciarCarrito(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
