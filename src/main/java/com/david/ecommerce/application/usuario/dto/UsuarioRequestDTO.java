package com.david.ecommerce.application.usuario.dto;

import com.david.ecommerce.domain.common.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {
    private String nombre;
    private String email;
    private String password;
    private String direccion;
    private Rol rol;
}
