package com.david.ecommerce.usuario.service;

import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.usuario.dto.UsuarioRequestDTO;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void crearUsuario_EmailDuplicado_LanzaExcepcion() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("test@email.com");
        dto.setPassword("123456");

        when(usuarioRepository.existsByEmail("test@email.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(dto))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessageContaining("ya está registrado");
    }

    @Test
    void crearUsuario_ContraseniaCorta_LanzaExcepcion() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("nuevo@email.com");
        dto.setPassword("123");

        assertThatThrownBy(() -> usuarioService.crear(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessageContaining("contraseña");
    }
}