package com.david.ecommerce.pedido.service;

import com.david.ecommerce.carrito.model.Carrito;
import com.david.ecommerce.carrito.model.ItemCarrito;
import com.david.ecommerce.carrito.repository.CarritoRepository;
import com.david.ecommerce.common.exception.*;
import com.david.ecommerce.pedido.dto.PedidoResponseDTO;
import com.david.ecommerce.pedido.model.DetallePedido;
import com.david.ecommerce.pedido.model.Pedido;
import com.david.ecommerce.pedido.repository.PedidoRepository;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.repository.ProductoRepository;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioRepository usuarioRepository,
                         CarritoRepository carritoRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    // ==================== CREAR PEDIDO ====================

    public PedidoResponseDTO crearPedidoDesdeCarrito(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        Carrito carrito = carritoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ValidacionNegocioException("El usuario no tiene un carrito activo"));

        if (carrito.getItems().isEmpty()) {
            throw new CarritoVacioException(usuarioId);
        }

        // Validar stock
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();
            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        producto.getNombre(),
                        producto.getStock(),
                        item.getCantidad()
                );
            }
        }

        // Construir pedido
        Pedido pedido = construirPedido(usuario, carrito);

        // Descontar stock
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        // Vaciar carrito
        carrito.getItems().clear();
        carritoRepository.save(carrito);

        Pedido guardado = pedidoRepository.save(pedido);

        log.info("📦 Pedido creado - ID: {}, Usuario: {}, Total: ${}, Productos: {}",
                guardado.getId(), usuario.getNombre(), guardado.getTotal(), guardado.getDetalles().size());

        return new PedidoResponseDTO(guardado);
    }

    private Pedido construirPedido(Usuario usuario, Carrito carrito) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(
                    item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
            );

            pedido.agregarDetalle(detalle);
            total = total.add(detalle.getSubtotal());
        }
        pedido.setTotal(total);
        return pedido;
    }

    // ==================== CONSULTAS ====================

    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoRepository.findAll()
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        return new PedidoResponseDTO(pedido);
    }

    public List<PedidoResponseDTO> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<PedidoResponseDTO> obtenerHistorial(Long usuarioId,
                                                    Pedido.EstadoPedido estado,
                                                    LocalDateTime desde,
                                                    LocalDateTime hasta) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuarioId);

        return pedidos.stream()
                .filter(p -> estado == null || p.getEstado() == estado)
                .filter(p -> desde == null || !p.getFechaPedido().isBefore(desde))
                .filter(p -> hasta == null || !p.getFechaPedido().isAfter(hasta))
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ==================== ACTUALIZAR ESTADO ====================

    public PedidoResponseDTO actualizarEstado(Long id, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);

        log.info("🔄 Estado de pedido actualizado - ID: {}, Nuevo estado: {}", id, nuevoEstado);

        return new PedidoResponseDTO(actualizado);
    }

    // ==================== CANCELAR PEDIDO ====================

    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));

        Pedido.EstadoPedido estadoAnterior = pedido.getEstado();

        if (pedido.getEstado() == Pedido.EstadoPedido.CANCELADO) {
            throw new ValidacionNegocioException("El pedido ya está cancelado");
        }
        if (pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {
            throw new ValidacionNegocioException("No se puede cancelar un pedido entregado");
        }

        // Devolver stock
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        log.warn("🗑️ Pedido cancelado - ID: {}, Estado anterior: {}", id, estadoAnterior);
    }
}