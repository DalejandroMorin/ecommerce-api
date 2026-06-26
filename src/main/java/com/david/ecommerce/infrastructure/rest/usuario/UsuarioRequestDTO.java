package com.david.ecommerce.infrastructure.rest.usuario;

import com.david.ecommerce.domain.usuario.Usuario;
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
    private Usuario.Rol rol;
}
