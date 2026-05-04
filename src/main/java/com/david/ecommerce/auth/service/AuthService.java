package com.david.ecommerce.auth.service;

import com.david.ecommerce.auth.dto.AuthResponseDTO;
import com.david.ecommerce.auth.dto.LoginRequestDTO;
import com.david.ecommerce.auth.dto.RegisterRequestDTO;
import com.david.ecommerce.auth.model.UserDetailsImpl;
import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setDireccion(dto.getDireccion());
        usuario.setRol(Usuario.Rol.CLIENTE); // Por defecto, todos son CLIENTE

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol().name());

        return new AuthResponseDTO(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername(), userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));

        return new AuthResponseDTO(token, userDetails.getUsername(), userDetails.getNombre(), userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
    }
}