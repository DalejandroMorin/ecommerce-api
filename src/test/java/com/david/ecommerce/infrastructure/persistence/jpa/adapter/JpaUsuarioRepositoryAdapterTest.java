package com.david.ecommerce.infrastructure.persistence.jpa.adapter;

import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaUsuarioRepositoryAdapter.class)
class JpaUsuarioRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private JpaUsuarioRepositoryAdapter adapter;

    @Autowired
    private TestEntityManager em;

    private UsuarioEntity usuarioEntity;

    @BeforeEach
    void setUp() {
        usuarioEntity = new UsuarioEntity();
        usuarioEntity.setNombre("Juan Pérez");
        usuarioEntity.setEmail("juan@ejemplo.com");
        usuarioEntity.setPassword("password123");
        usuarioEntity.setDireccion("Calle 123");
        usuarioEntity.setRol(UsuarioEntity.Rol.CLIENTE);
        em.persistAndFlush(usuarioEntity);

        var adminEntity = new UsuarioEntity();
        adminEntity.setNombre("Admin");
        adminEntity.setEmail("admin@ejemplo.com");
        adminEntity.setPassword("admin123");
        adminEntity.setRol(UsuarioEntity.Rol.ADMIN);
        em.persistAndFlush(adminEntity);

        em.clear();
    }

    @Test
    @DisplayName("findAll retorna todos los usuarios")
    void findAll_ConDatos_RetornaTodos() {
        var resultados = adapter.findAll();

        assertThat(resultados).hasSize(2);
    }

    @Test
    @DisplayName("findById con ID existente retorna Optional con usuario")
    void findById_Existente_RetornaUsuario() {
        var resultado = adapter.findById(usuarioEntity.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Juan Pérez");
        assertThat(resultado.get().getEmail()).isEqualTo("juan@ejemplo.com");
    }

    @Test
    @DisplayName("findById con ID inexistente retorna Optional vacío")
    void findById_NoExistente_RetornaVacio() {
        var resultado = adapter.findById(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("save crea un nuevo usuario")
    void save_Crear_RetornaUsuarioCreado() {
        var nuevo = new Usuario();
        nuevo.setNombre("María García");
        nuevo.setEmail("maria@ejemplo.com");
        nuevo.setPassword("pass456");
        nuevo.setDireccion("Av. Siempreviva 742");
        nuevo.setRol(Usuario.Rol.ADMIN);

        var resultado = adapter.save(nuevo);

        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("maria@ejemplo.com");
    }

    @Test
    @DisplayName("save actualiza un usuario existente")
    void save_Actualizar_RetornaUsuarioActualizado() {
        var usuarioDomain = adapter.findById(usuarioEntity.getId()).orElseThrow();
        usuarioDomain.setDireccion("Nueva Dirección 456");

        var resultado = adapter.save(usuarioDomain);

        assertThat(resultado.getDireccion()).isEqualTo("Nueva Dirección 456");
    }

    @Test
    @DisplayName("deleteById elimina un usuario")
    void deleteById_Existente_UsuarioEliminado() {
        adapter.deleteById(usuarioEntity.getId());

        var resultado = adapter.findById(usuarioEntity.getId());
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findByEmail con email existente retorna Optional con usuario")
    void findByEmail_Existente_RetornaUsuario() {
        var resultado = adapter.findByEmail("juan@ejemplo.com");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("findByEmail con email inexistente retorna Optional vacío")
    void findByEmail_NoExistente_RetornaVacio() {
        var resultado = adapter.findByEmail("noexiste@ejemplo.com");

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail retorna true para email existente")
    void existsByEmail_Existente_RetornaTrue() {
        assertThat(adapter.existsByEmail("juan@ejemplo.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail retorna false para email inexistente")
    void existsByEmail_NoExistente_RetornaFalse() {
        assertThat(adapter.existsByEmail("noexiste@ejemplo.com")).isFalse();
    }
}
