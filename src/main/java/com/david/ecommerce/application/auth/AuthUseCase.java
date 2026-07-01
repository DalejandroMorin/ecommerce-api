package com.david.ecommerce.application.auth;

import com.david.ecommerce.application.auth.dto.AuthResponseDTO;
import com.david.ecommerce.application.auth.dto.LoginRequestDTO;
import com.david.ecommerce.application.auth.dto.RegisterRequestDTO;
import com.david.ecommerce.domain.common.Rol;
import com.david.ecommerce.infrastructure.security.UserDetailsImpl;
import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.domain.auth.TokenService;
import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.domain.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthUseCase.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthUseCase(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       AuthenticationManager authenticationManager) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setDireccion(dto.getDireccion());
        usuario.setRol(Rol.CLIENTE);
        usuario.validar();

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);

        String token = tokenService.generateToken(usuario.getEmail(), usuario.getRol().name());

        log.info("Usuario registrado - Email: {}", usuario.getEmail());

        return new AuthResponseDTO(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = tokenService.generateToken(userDetails.getUsername(),
                userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));

        log.info("Usuario autenticado - Email: {}", userDetails.getUsername());

        return new AuthResponseDTO(token, userDetails.getUsername(), userDetails.getNombre(),
                userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
    }
}
