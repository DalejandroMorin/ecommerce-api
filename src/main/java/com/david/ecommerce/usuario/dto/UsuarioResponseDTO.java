package com.david.ecommerce.usuario.dto;

import com.david.ecommerce.usuario.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String direccion;
    private Usuario.Rol rol;
    private LocalDateTime fechaRegistro;

    // Constructor para mapear desde la entidad
    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.direccion = usuario.getDireccion();
        this.rol = usuario.getRol();
        this.fechaRegistro = usuario.getFechaRegistro();
    }
}