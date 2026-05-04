package com.david.ecommerce.carrito.service;

import com.david.ecommerce.carrito.dto.CarritoResponseDTO;
import com.david.ecommerce.carrito.model.Carrito;
import com.david.ecommerce.carrito.model.ItemCarrito;
import com.david.ecommerce.carrito.repository.CarritoRepository;
import com.david.ecommerce.carrito.repository.ItemCarritoRepository;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.repository.ProductoRepository;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CarritoService {

    private static final Logger log = LoggerFactory.getLogger(CarritoService.class);

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    @Autowired
    public CarritoService(CarritoRepository carritoRepository,
                          ItemCarritoRepository itemCarritoRepository,
                          UsuarioRepository usuarioRepository,
                          ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    private Carrito obtenerOCrearCarritoEntidad(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuario(usuario);
                    Carrito guardado = carritoRepository.save(nuevoCarrito);
                    log.debug("🆕 Carrito creado para usuario ID: {}", usuarioId);
                    return guardado;
                });
    }

    public CarritoResponseDTO obtenerCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));
        log.debug("🛒 Consultando carrito - Usuario ID: {}, Items: {}", usuarioId, carrito.getItems().size());
        return new CarritoResponseDTO(carrito);
    }

    public CarritoResponseDTO agregarProducto(Long usuarioId, Long productoId, Integer cantidad) {
        if (cantidad <= 0) {
            throw new ValidacionNegocioException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = obtenerOCrearCarritoEntidad(usuarioId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto", productoId));

        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), cantidad);
        }

        Optional<ItemCarrito> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            log.info("🛒 Cantidad actualizada en carrito - Usuario: {}, Producto: {}, Nueva cantidad: {}",
                    usuarioId, producto.getNombre(), item.getCantidad());
        } else {
            ItemCarrito nuevoItem = new ItemCarrito();
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(producto.getPrecio());
            carrito.agregarItem(nuevoItem);
            log.info("🛒 Producto agregado al carrito - Usuario: {}, Producto: {}, Cantidad: {}",
                    usuarioId, producto.getNombre(), cantidad);
        }

        Carrito guardado = carritoRepository.save(carrito);
        return new CarritoResponseDTO(guardado);
    }

    public CarritoResponseDTO actualizarCantidad(Long usuarioId, Long itemId, Integer nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            throw new ValidacionNegocioException("La cantidad debe ser mayor a cero");
        }

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de carrito", itemId));

        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new ValidacionNegocioException("El item no pertenece al carrito del usuario");
        }

        Producto producto = item.getProducto();
        if (producto.getStock() < nuevaCantidad) {
            throw new StockInsuficienteException(producto.getNombre(), producto.getStock(), nuevaCantidad);
        }

        item.setCantidad(nuevaCantidad);
        itemCarritoRepository.save(item);

        log.info("✏️ Cantidad actualizada en carrito - Item ID: {}, Nueva cantidad: {}", itemId, nuevaCantidad);

        return new CarritoResponseDTO(carrito);
    }

    public CarritoResponseDTO eliminarItem(Long usuarioId, Long itemId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        ItemCarrito item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Item de carrito", itemId));

        if (!item.getCarrito().getId().equals(carrito.getId())) {
            throw new ValidacionNegocioException("El item no pertenece al carrito del usuario");
        }

        String nombreProducto = item.getProducto().getNombre();
        carrito.removerItem(item);
        itemCarritoRepository.delete(item);

        log.info("🗑️ Item eliminado del carrito - Usuario: {}, Producto: {}", usuarioId, nombreProducto);

        Carrito guardado = carritoRepository.save(carrito);
        return new CarritoResponseDTO(guardado);
    }

    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado para el usuario " + usuarioId));

        int cantidadItems = carrito.getItems().size();
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        log.info("🗑️ Carrito vaciado - Usuario: {}, Items eliminados: {}", usuarioId, cantidadItems);
    }
}