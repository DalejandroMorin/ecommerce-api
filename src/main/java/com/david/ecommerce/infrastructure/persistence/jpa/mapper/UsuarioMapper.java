package com.david.ecommerce.infrastructure.persistence.jpa.mapper;

import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;

public class UsuarioMapper {

    private UsuarioMapper() {}

    public static UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setDireccion(domain.getDireccion());
        if (domain.getRol() != null) {
            entity.setRol(UsuarioEntity.Rol.valueOf(domain.getRol().name()));
        }
        entity.setFechaRegistro(domain.getFechaRegistro());
        return entity;
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;
        Usuario domain = new Usuario();
        domain.setId(entity.getId());
        domain.setNombre(entity.getNombre());
        domain.setEmail(entity.getEmail());
        domain.setPassword(entity.getPassword());
        domain.setDireccion(entity.getDireccion());
        if (entity.getRol() != null) {
            domain.setRol(Usuario.Rol.valueOf(entity.getRol().name()));
        }
        domain.setFechaRegistro(entity.getFechaRegistro());
        return domain;
    }
}
