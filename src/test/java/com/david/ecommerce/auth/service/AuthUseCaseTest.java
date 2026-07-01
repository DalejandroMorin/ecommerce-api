package com.david.ecommerce.auth.service;

import com.david.ecommerce.application.auth.AuthUseCase;
import com.david.ecommerce.application.auth.dto.AuthResponseDTO;
import com.david.ecommerce.application.auth.dto.LoginRequestDTO;
import com.david.ecommerce.application.auth.dto.RegisterRequestDTO;
import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.domain.auth.TokenService;
import com.david.ecommerce.domain.common.Rol;
import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.domain.usuario.UsuarioRepository;
import com.david.ecommerce.infrastructure.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthUseCase authUseCase;

    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterRequestDTO("Edgar", "edgar@email.com", "password123", "Calle 123");
        loginDTO = new LoginRequestDTO("edgar@email.com", "password123");
    }

    @Test
    @DisplayName("Registrar usuario con éxito")
    void register_Exitoso() {
        when(usuarioRepository.existsByEmail("edgar@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenService.generateToken("edgar@email.com", "CLIENTE")).thenReturn("jwt-token");

        AuthResponseDTO result = authUseCase.register(registerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("edgar@email.com");
        assertThat(result.getNombre()).isEqualTo("Edgar");
        assertThat(result.getRol()).isEqualTo("CLIENTE");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Lanzar excepción si la contraseña es corta — validar antes de codificar")
    void register_PasswordCorta_LanzaExcepcionAntesDeCodificar() {
        RegisterRequestDTO dto = new RegisterRequestDTO("Edgar", "edgar@email.com", "123", "Calle 123");

        assertThatThrownBy(() -> authUseCase.register(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contraseña");

        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanzar excepción si el email ya está registrado")
    void register_EmailDuplicado_LanzaExcepcion() {
        when(usuarioRepository.existsByEmail("edgar@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authUseCase.register(registerDTO))
                .isInstanceOf(EmailDuplicadoException.class)
                .hasMessageContaining("edgar@email.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login exitoso")
    void login_Exitoso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Edgar");
        usuario.setEmail("edgar@email.com");
        usuario.setPassword("encodedPass");
        usuario.setRol(Rol.CLIENTE);

        UserDetailsImpl userDetails = new UserDetailsImpl(usuario);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenService.generateToken("edgar@email.com", "CLIENTE")).thenReturn("jwt-token");

        AuthResponseDTO result = authUseCase.login(loginDTO);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("edgar@email.com");
        assertThat(result.getNombre()).isEqualTo("Edgar");
        assertThat(result.getRol()).isEqualTo("CLIENTE");
    }

    @Test
    @DisplayName("Login con credenciales inválidas lanza excepción")
    void login_CredencialesInvalidas_LanzaExcepcion() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authUseCase.login(loginDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bad credentials");
    }
}
