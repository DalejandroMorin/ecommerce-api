package com.david.ecommerce.pedido.service;

import com.david.ecommerce.application.pedido.PedidoUseCase;

import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.StockInsuficienteException;
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
    private PedidoUseCase pedidoUseCase;

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
        carrito.setUsuarioId(1L);
        carrito.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("Crear pedido desde carrito con éxito")
    void crearPedidoDesdeCarrito_Exitoso() {
        ItemCarrito item = new ItemCarrito(1L, "Laptop", 2, new BigDecimal("1000.00"));
        carrito.agregarItem(item);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.buscarPorUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(carritoRepository.guardar(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = pedidoUseCase.crearPedidoDesdeCarrito(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("2000.00"));
        assertThat(result.getEstado()).isEqualTo(Pedido.EstadoPedido.PENDIENTE);
        assertThat(result.getDetalles()).hasSize(1);

        assertThat(producto.getStock()).isEqualTo(8);
        verify(productoRepository).save(producto);
        verify(carritoRepository).guardar(carrito);
    }

    @Test
    @DisplayName("Lanzar excepción si el carrito está vacío")
    void crearPedidoDesdeCarrito_CarritoVacio_LanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.buscarPorUsuarioId(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> pedidoUseCase.crearPedidoDesdeCarrito(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("Lanzar excepción si stock insuficiente")
    void crearPedidoDesdeCarrito_StockInsuficiente_LanzaExcepcion() {
        ItemCarrito item = new ItemCarrito(1L, "Laptop", 15, new BigDecimal("1000.00"));
        carrito.agregarItem(item);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.buscarPorUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> pedidoUseCase.crearPedidoDesdeCarrito(1L))
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

        Pedido pedido = new Pedido(1L, Pedido.EstadoPedido.PENDIENTE);
        pedido.setId(1L);
        DetallePedido detalle = new DetallePedido(2L, "Mouse", 2, new BigDecimal("50.00"), new BigDecimal("100.00"));
        detalle.setId(1L);
        pedido.agregarDetalle(detalle);
        pedido.setTotal(new BigDecimal("100.00"));

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(productoConStockBajo));

        pedidoUseCase.cancelarPedido(1L);

        assertThat(pedido.getEstado()).isEqualTo(Pedido.EstadoPedido.CANCELADO);
        assertThat(productoConStockBajo.getStock()).isEqualTo(5);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    @DisplayName("No se puede cancelar un pedido ya cancelado")
    void cancelarPedido_YaCancelado_LanzaExcepcion() {
        Pedido pedido = new Pedido(1L, Pedido.EstadoPedido.CANCELADO);
        pedido.setId(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoUseCase.cancelarPedido(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya está cancelado");
    }

    @Test
    @DisplayName("Obtener pedido por ID lanza excepción si no existe")
    void obtenerPorId_NoEncontrado_LanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoUseCase.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no encontrado");
    }
}
