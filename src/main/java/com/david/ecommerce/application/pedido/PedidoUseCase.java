package com.david.ecommerce.application.pedido;

import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.domain.carrito.Carrito;
import com.david.ecommerce.domain.carrito.CarritoRepository;
import com.david.ecommerce.domain.carrito.ItemCarrito;
import com.david.ecommerce.domain.pedido.DetallePedido;
import com.david.ecommerce.domain.pedido.Pedido;
import com.david.ecommerce.domain.pedido.PedidoRepository;
import com.david.ecommerce.domain.producto.Producto;
import com.david.ecommerce.domain.producto.ProductoRepository;
import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.domain.usuario.UsuarioRepository;
import com.david.ecommerce.application.pedido.dto.PedidoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoUseCase {

    private static final Logger log = LoggerFactory.getLogger(PedidoUseCase.class);

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public PedidoUseCase(PedidoRepository pedidoRepository,
                         UsuarioRepository usuarioRepository,
                         CarritoRepository carritoRepository,
                         ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    public PedidoResponseDTO crearPedidoDesdeCarrito(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", usuarioId));

        Carrito carrito = carritoRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new ValidacionNegocioException("El usuario no tiene un carrito activo"));

        carrito.validar();

        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));
            if (producto.getStock() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        producto.getNombre(), producto.getStock(), item.getCantidad());
            }
        }

        Pedido pedido = construirPedido(usuario, carrito);
        pedido.validar();

        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        carrito.getItems().clear();
        carritoRepository.guardar(carrito);

        Pedido guardado = pedidoRepository.save(pedido);

        log.info("Pedido creado - ID: {}, Usuario: {}, Total: ${}, Productos: {}",
                guardado.getId(), usuario.getNombre(), guardado.getTotal(), guardado.getDetalles().size());

        return PedidoResponseDTO.fromDomain(guardado, usuario.getNombre());
    }

    private Pedido construirPedido(Usuario usuario, Carrito carrito) {
        Pedido pedido = new Pedido(usuario.getId(), Pedido.EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));

            DetallePedido detalle = new DetallePedido(
                    producto.getId(), producto.getNombre(),
                    item.getCantidad(), item.getPrecioUnitario(),
                    item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()))
            );

            pedido.agregarDetalle(detalle);
            total = total.add(detalle.getSubtotal());
        }
        pedido.setTotal(total);
        return pedido;
    }

    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoRepository.findAll().stream()
                .map(PedidoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        return PedidoResponseDTO.fromDomain(pedido);
    }

    public List<PedidoResponseDTO> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
                .map(PedidoResponseDTO::fromDomain)
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
                .map(PedidoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    public PedidoResponseDTO actualizarEstado(Long id, Pedido.EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido", id));
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);

        log.info("Estado de pedido actualizado - ID: {}, Nuevo estado: {}", id, nuevoEstado);

        return PedidoResponseDTO.fromDomain(actualizado);
    }

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

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", detalle.getProductoId()));
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        log.warn("Pedido cancelado - ID: {}, Estado anterior: {}", id, estadoAnterior);
    }
}
