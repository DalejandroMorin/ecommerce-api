package com.david.ecommerce.carrito.service;

import com.david.ecommerce.application.carrito.CarritoUseCase;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class CarritoServiceTest {

    @Mock private CarritoRepository carritoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoUseCase carritoUseCase;

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
        carrito.setUsuarioId(1L);
        carrito.setItems(new ArrayList<>());

        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        lenient().when(carritoRepository.buscarPorUsuarioId(1L)).thenReturn(Optional.of(carrito));
    }

    @Test
    @DisplayName("Agregar producto al carrito por primera vez")
    void agregarProducto_PrimeraVez() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.guardar(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = carritoUseCase.agregarProducto(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCantidad()).isEqualTo(3);
        assertThat(result.getItems().get(0).getProductoNombre()).isEqualTo("Tablet");
    }

    @Test
    @DisplayName("Agregar producto existente incrementa cantidad")
    void agregarProducto_Existente_IncrementaCantidad() {
        ItemCarrito itemExistente = new ItemCarrito(1L, "Tablet", 2, new BigDecimal("300.00"));
        itemExistente.setId(1L);
        carrito.agregarItem(itemExistente);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.guardar(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = carritoUseCase.agregarProducto(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getCantidad()).isEqualTo(5);
    }

    @Test
    @DisplayName("Lanzar excepción si stock insuficiente al agregar")
    void agregarProducto_StockInsuficiente_LanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> carritoUseCase.agregarProducto(1L, 1L, 10))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}
