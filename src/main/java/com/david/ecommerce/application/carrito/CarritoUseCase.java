package com.david.ecommerce.application.carrito;

import com.david.ecommerce.application.carrito.dto.CarritoResponseDTO;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.domain.carrito.CarritoRepository;
import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.domain.producto.ProductoRepository;
import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.domain.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CarritoUseCase {

    private static final Logger log = LoggerFactory.getLogger(CarritoUseCase.class);

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public CarritoUseCase(CarritoRepository carritoRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    private Carrito obtenerOCrearCarrito(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        return carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuarioId(usuario.getId());
                    Carrito guardado = carritoRepository.guardar(nuevo);
                    log.debug("Nuevo carrito creado para usuario ID: {}", usuarioId);
                    return guardado;
                });
    }

    public CarritoResponseDTO obtenerCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));
        log.debug("Consultando carrito - Usuario ID: {}, Items: {}", usuarioId, carrito.getItems().size());
        return CarritoResponseDTO.fromDomain(carrito);
    }

    public CarritoResponseDTO agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionNegocioException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), cantidad);
        }

        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            log.info("Cantidad actualizada en carrito - Usuario: {}, Producto: {}, Nueva cantidad: {}",
                    usuarioId, producto.getNombre(), item.getCantidad());
        } else {
            ItemCarrito nuevoItem = new ItemCarrito(
                    producto.getId(), producto.getNombre(), cantidad, producto.getPrecio()
            );
            carrito.agregarItem(nuevoItem);
            log.info("Producto agregado al carrito - Usuario: {}, Producto: {}, Cantidad: {}",
                    usuarioId, producto.getNombre(), cantidad);
        }

        Carrito guardado = carritoRepository.guardar(carrito);
        return CarritoResponseDTO.fromDomain(guardado);
    }

    public CarritoResponseDTO actualizarCantidad(Long usuarioId, Long itemId, Integer nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new ValidacionNegocioException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        ItemCarrito item = carritoRepository.buscarItemPorId(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de carrito", itemId));

        boolean pertenece = carrito.getItems().stream()
                .anyMatch(i -> i.getId().equals(itemId));
        if (!pertenece) {
            throw new ValidacionNegocioException("El item no pertenece al carrito del usuario");
        }

        Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));
        if (producto.getStock() < nuevaCantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), nuevaCantidad);
        }

        item.setCantidad(nuevaCantidad);
        carritoRepository.guardar(carrito);

        log.info("Cantidad actualizada en carrito - Item ID: {}, Nueva cantidad: {}", itemId, nuevaCantidad);

        return CarritoResponseDTO.fromDomain(carrito);
    }

    public CarritoResponseDTO eliminarItem(Long usuarioId, Long itemId) {
        Carrito carrito = carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        ItemCarrito item = carritoRepository.buscarItemPorId(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de carrito", itemId));

        boolean pertenece = carrito.getItems().stream()
                .anyMatch(i -> i.getId().equals(itemId));
        if (!pertenece) {
            throw new ValidacionNegocioException("El item no pertenece al carrito del usuario");
        }

        String nombreProducto = item.getProductoNombre();
        carrito.removerItem(item);
        carritoRepository.eliminarItem(item);

        log.info("Item eliminado del carrito - Usuario: {}, Producto: {}", usuarioId, nombreProducto);

        Carrito guardado = carritoRepository.guardar(carrito);
        return CarritoResponseDTO.fromDomain(guardado);
    }

    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        int cantidadItems = carrito.getItems().size();
        carrito.getItems().clear();
        carritoRepository.guardar(carrito);

        log.info("Carrito vaciado - Usuario: {}, Items eliminados: {}", usuarioId, cantidadItems);
    }
}
