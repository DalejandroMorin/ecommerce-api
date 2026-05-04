package com.david.ecommerce.producto.controller;

import com.david.ecommerce.producto.dto.ProductoRequestDTO;
import com.david.ecommerce.producto.dto.ProductoResponseDTO;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        ProductoResponseDTO nuevo = productoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ENDPOINT DE BÚSQUEDA CON FILTROS
    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos con filtros")
    public ResponseEntity<List<ProductoResponseDTO>> buscarProductos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Producto.Categoria categoria,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Integer stockMin) {

        List<ProductoResponseDTO> resultados = productoService.buscarConFiltros(
                nombre, categoria, precioMin, precioMax, stockMin);

        return ResponseEntity.ok(resultados);
    }
}