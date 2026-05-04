package com.david.ecommerce.carrito.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 👈 evita UnnecessaryStubbingException
class CarritoServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Edgar");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Tablet");
        producto.setPrecio(new BigDecimal("300.00"));
        producto.setStock(5);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setItems(new ArrayList<>());

        // Stubs lenient para no causar error si algún test no los usa
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        lenient().when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
    }

    @Test
    @DisplayName("Agregar producto al carrito por primera vez")
    void agregarProducto_PrimeraVez() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = carritoService.agregarProducto(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCantidad()).isEqualTo(3);
        assertThat(result.getItems().get(0).getProductoNombre()).isEqualTo("Tablet");
    }

    @Test
    @DisplayName("Agregar producto existente incrementa cantidad")
    void agregarProducto_Existente_IncrementaCantidad() {
        ItemCarrito itemExistente = new ItemCarrito();
        itemExistente.setId(1L);
        itemExistente.setProducto(producto);
        itemExistente.setCantidad(2);
        itemExistente.setPrecioUnitario(producto.getPrecio());
        itemExistente.setCarrito(carrito);
        carrito.agregarItem(itemExistente);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = carritoService.agregarProducto(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCantidad()).isEqualTo(5);
    }

    @Test
    @DisplayName("Lanzar excepción si stock insuficiente al agregar")
    void agregarProducto_StockInsuficiente_LanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> carritoService.agregarProducto(1L, 1L, 10))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    @Test
    @DisplayName("Actualizar cantidad de item")
    void actualizarCantidad() {
        ItemCarrito item = new ItemCarrito();
        item.setId(10L);
        item.setProducto(producto);
        item.setCantidad(2);
        item.setPrecioUnitario(producto.getPrecio());
        item.setCarrito(carrito);
        carrito.getItems().add(item);

        when(itemCarritoRepository.findById(10L)).thenReturn(Optional.of(item));
        // aquí ya usa el stub lenient de findByUsuarioId
        when(carritoRepository.save(any(Carrito.class))).thenReturn(carrito);

        var result = carritoService.actualizarCantidad(1L, 10L, 4);

        assertThat(result.getItems().get(0).getCantidad()).isEqualTo(4);
    }
}