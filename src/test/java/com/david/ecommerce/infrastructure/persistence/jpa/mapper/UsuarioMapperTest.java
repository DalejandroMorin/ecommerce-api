package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioMapperTest {

    @Test
    @DisplayName("toEntity luego toDomain preserva todos los campos (roundtrip)")
    void roundtrip_DominioAEntidadADominio_PreservaCampos() {
        var domain = new Usuario();
        domain.setId(1L);
        domain.setNombre("Juan Pérez");
        domain.setEmail("juan@ejemplo.com");
        domain.setPassword("password123");
        domain.setDireccion("Calle 123, Ciudad");
        domain.setRol(Usuario.Rol.CLIENTE);
        domain.setFechaRegistro(LocalDateTime.of(2025, 3, 10, 8, 30));

        var entity = UsuarioMapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNombre()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan@ejemplo.com");
        assertThat(entity.getPassword()).isEqualTo("password123");
        assertThat(entity.getDireccion()).isEqualTo("Calle 123, Ciudad");
        assertThat(entity.getRol()).isEqualTo(UsuarioEntity.Rol.CLIENTE);

        var result = UsuarioMapper.toDomain(entity);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan Pérez");
        assertThat(result.getEmail()).isEqualTo("juan@ejemplo.com");
        assertThat(result.getPassword()).isEqualTo("password123");
        assertThat(result.getDireccion()).isEqualTo("Calle 123, Ciudad");
        assertThat(result.getRol()).isEqualTo(Usuario.Rol.CLIENTE);
        assertThat(result.getFechaRegistro()).isEqualTo(domain.getFechaRegistro());
    }

    @Test
    @DisplayName("toEntity con null retorna null")
    void toEntity_Null_RetornaNull() {
        assertThat(UsuarioMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("toDomain con null retorna null")
    void toDomain_Null_RetornaNull() {
        assertThat(UsuarioMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Todos los roles del dominio mapean correctamente al entity enum mediante valueOf")
    void todosLosRoles_MapeanCorrectamente() {
        for (Usuario.Rol domainRol : Usuario.Rol.values()) {
            var domain = new Usuario();
            domain.setNombre("Test");
            domain.setEmail("test@ejemplo.com");
            domain.setPassword("password123");
            domain.setRol(domainRol);

            var entity = UsuarioMapper.toEntity(domain);
            var result = UsuarioMapper.toDomain(entity);

            assertThat(result.getRol()).isEqualTo(domainRol);
        }
    }
}
