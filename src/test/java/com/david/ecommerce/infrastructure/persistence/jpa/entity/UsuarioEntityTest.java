package com.david.ecommerce.infrastructure.persistence.jpa.entity;

import com.david.ecommerce.infrastructure.persistence.jpa.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioEntityTest extends AbstractIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("persistir usuario con todos los campos y leerlo por ID")
    void persistir_UsuarioConTodosLosCampos_RetornaUsuarioCreado() {
        var entity = new UsuarioEntity();
        entity.setNombre("Juan Pérez");
        entity.setEmail("juan@ejemplo.com");
        entity.setPassword("password123");
        entity.setDireccion("Calle 123, Ciudad");
        entity.setRol(UsuarioEntity.Rol.CLIENTE);

        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(UsuarioEntity.class, saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getNombre()).isEqualTo("Juan Pérez");
        assertThat(found.getEmail()).isEqualTo("juan@ejemplo.com");
        assertThat(found.getPassword()).isEqualTo("password123");
        assertThat(found.getDireccion()).isEqualTo("Calle 123, Ciudad");
        assertThat(found.getRol()).isEqualTo(UsuarioEntity.Rol.CLIENTE);
    }

    @Test
    @DisplayName("@PrePersist establece fechaRegistro al insertar")
    void prePersist_EstableceFechaRegistro_Correctamente() {
        var entity = new UsuarioEntity();
        entity.setNombre("María García");
        entity.setEmail("maria@ejemplo.com");
        entity.setPassword("pass456");
        entity.setRol(UsuarioEntity.Rol.ADMIN);

        var now = LocalDateTime.now();
        var beforePersist = now.withNano((now.getNano() / 1000) * 1000);
        var saved = em.persistAndFlush(entity);
        em.clear();

        var found = em.find(UsuarioEntity.class, saved.getId());

        assertThat(found.getFechaRegistro()).isNotNull();
        assertThat(found.getFechaRegistro()).isAfterOrEqualTo(beforePersist);
        assertThat(found.getFechaRegistro()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("email único genera DataIntegrityViolationException al duplicar")
    void emailUnico_ConEmailDuplicado_LanzaExcepcion() {
        var usuario1 = new UsuarioEntity();
        usuario1.setNombre("Usuario Uno");
        usuario1.setEmail("duplicado@ejemplo.com");
        usuario1.setPassword("pass123");
        usuario1.setRol(UsuarioEntity.Rol.CLIENTE);

        em.persistAndFlush(usuario1);
        em.clear();

        var usuario2 = new UsuarioEntity();
        usuario2.setNombre("Usuario Dos");
        usuario2.setEmail("duplicado@ejemplo.com");
        usuario2.setPassword("pass456");
        usuario2.setRol(UsuarioEntity.Rol.CLIENTE);

        assertThatThrownBy(() -> em.persistAndFlush(usuario2))
                .isInstanceOfAny(DataIntegrityViolationException.class, ConstraintViolationException.class);
    }

    @Test
    @DisplayName("Rol enum se persiste como STRING para CLIENTE y ADMIN")
    void rolEnum_SePersisteComoString_TodosLosValores() {
        for (UsuarioEntity.Rol rol : UsuarioEntity.Rol.values()) {
            var entity = new UsuarioEntity();
            entity.setNombre("Usuario " + rol.name());
            entity.setEmail(rol.name().toLowerCase() + "@ejemplo.com");
            entity.setPassword("pass123");
            entity.setRol(rol);

            var saved = em.persistAndFlush(entity);
            em.clear();

            var found = em.find(UsuarioEntity.class, saved.getId());

            assertThat(found.getRol()).isEqualTo(rol);
        }
    }

    @Test
    @DisplayName("Verificar que todos los Rol del dominio existen en la entidad (EC-1)")
    void enumSync_DominioAEntidad_TodosLosRolesMapean() {
        for (com.david.ecommerce.domain.usuario.Usuario.Rol domainRol
                : com.david.ecommerce.domain.usuario.Usuario.Rol.values()) {
            var entityRol = UsuarioEntity.Rol.valueOf(domainRol.name());
            assertThat(entityRol.name()).isEqualTo(domainRol.name());
        }
    }
}
