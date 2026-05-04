package com.david.ecommerce.pedido.service;

import com.david.ecommerce.carrito.model.Carrito;
import com.david.ecommerce.carrito.model.ItemCarrito;
import com.david.ecommerce.carrito.repository.CarritoRepository;
import com.david.ecommerce.common.exception.CarritoVacioException;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
import com.david.ecommerce.pedido.model.DetallePedido;
import com.david.ecommerce.pedido.model.Pedido;
import com.david.ecommerce.pedido.repository.PedidoRepository;
import com.david.ecommerce.producto.model.Producto;
import com.david.ecommerce.producto.repository.ProductoRepository;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CarritoRepository carritoRepository;
    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Edgar");
        usuario.setEmail("edgar@email.com");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setPrecio(new BigDecimal("1000.00"));
        producto.setStock(10);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("Crear pedido desde carrito con éxito")
    void crearPedidoDesdeCarrito_Exitoso() {
        // Arrange
        ItemCarrito item = new ItemCarrito();
        item.setProducto(producto);
        item.setCantidad(2);
        item.setPrecioUnitario(producto.getPrecio());
        carrito.agregarItem(item);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        //  stub para que el producto se "guarde" y el test pueda verificar el stock luego
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = pedidoService.crearPedidoDesdeCarrito(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getEstado()).isEqualTo(Pedido.EstadoPedido.PENDIENTE);
        assertThat(result.getDetalles()).hasSize(1);

        // Verificar descuento de stock (ahora producto es el mismo objeto)
        assertThat(producto.getStock()).isEqualTo(8);
        verify(productoRepository).save(producto);
        verify(carritoRepository).save(carrito);
    }

    @Test
    @DisplayName("Lanzar excepción si el carrito está vacío")
    void crearPedidoDesdeCarrito_CarritoVacio_LanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> pedidoService.crearPedidoDesdeCarrito(1L))
                .isInstanceOf(CarritoVacioException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("Lanzar excepción si stock insuficiente")
    void crearPedidoDesdeCarrito_StockInsuficiente_LanzaExcepcion() {
        ItemCarrito item = new ItemCarrito();
        item.setProducto(producto);
        item.setCantidad(15); // Solo hay 10
        carrito.agregarItem(item);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> pedidoService.crearPedidoDesdeCarrito(1L))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining("Laptop");
    }

    @Test
    @DisplayName("Cancelar pedido devuelve stock al producto")
    void cancelarPedido_DevuelveStock() {
        Producto productoConStockBajo = new Producto();
        productoConStockBajo.setId(2L);
        productoConStockBajo.setNombre("Mouse");
        productoConStockBajo.setPrecio(new BigDecimal("50.00"));
        productoConStockBajo.setStock(3);

        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuario(usuario);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);
        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(productoConStockBajo);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(new BigDecimal("50.00"));
        // 👇 Cálculo manual del subtotal
        detalle.setSubtotal(new BigDecimal("100.00"));
        pedido.agregarDetalle(detalle);
        pedido.setTotal(new BigDecimal("100.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // Act
        pedidoService.cancelarPedido(1L);

        // Assert
        assertThat(pedido.getEstado()).isEqualTo(Pedido.EstadoPedido.CANCELADO);
        assertThat(productoConStockBajo.getStock()).isEqualTo(5); // 3 + 2
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("No se puede cancelar un pedido ya cancelado")
    void cancelarPedido_YaCancelado_LanzaExcepcion() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya está cancelado");
    }

    @Test
    @DisplayName("Obtener pedido por ID lanza excepción si no existe")
    void obtenerPorId_NoEncontrado_LanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Pedido no encontrado");
    }
}