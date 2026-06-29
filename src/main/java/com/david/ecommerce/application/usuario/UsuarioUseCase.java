package com.david.ecommerce.application.usuario;

import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.domain.usuario.Usuario;
import com.david.ecommerce.domain.usuario.UsuarioRepository;
import com.david.ecommerce.application.usuario.dto.UsuarioRequestDTO;
import com.david.ecommerce.application.usuario.dto.UsuarioResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(UsuarioUseCase.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        return UsuarioResponseDTO.fromDomain(usuario);
    }

    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }

        Usuario usuario = new Usuario(
                dto.getNombre(), dto.getEmail(), dto.getPassword(),
                dto.getDireccion(), dto.getRol() != null ? dto.getRol() : Usuario.Rol.CLIENTE
        );
        usuario.validar();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado - ID: {}, Email: {}", guardado.getId(), guardado.getEmail());
        return UsuarioResponseDTO.fromDomain(guardado);
    }

    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        if (!existente.getEmail().equals(dto.getEmail()) &&
                usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }

        existente.setNombre(dto.getNombre());
        existente.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            existente.setPassword(dto.getPassword());
        }
        existente.setDireccion(dto.getDireccion());
        if (dto.getRol() != null) {
            existente.setRol(dto.getRol());
        }

        Usuario actualizado = usuarioRepository.save(existente);
        log.info("Usuario actualizado - ID: {}", actualizado.getId());
        return UsuarioResponseDTO.fromDomain(actualizado);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario", id);
        }
        usuarioRepository.deleteById(id);
        log.warn("Usuario eliminado - ID: {}", id);
    }
}
