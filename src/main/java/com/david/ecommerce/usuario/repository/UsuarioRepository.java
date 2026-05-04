package com.david.ecommerce.usuario.repository;

import com.david.ecommerce.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por email (para autenticación)
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un email (para validar duplicados)
    boolean existsByEmail(String email);
}