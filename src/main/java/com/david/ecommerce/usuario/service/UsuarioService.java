package com.david.ecommerce.usuario.service;

import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.common.exception.ValidacionNegocioException;
import com.david.ecommerce.usuario.dto.UsuarioRequestDTO;
import com.david.ecommerce.usuario.dto.UsuarioResponseDTO;
import com.david.ecommerce.usuario.model.Usuario;
import com.david.ecommerce.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponseDTO> obtenerTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        return new UsuarioResponseDTO(usuario);
    }

    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        // Validar email único
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }

        // Validar formato de email básico
        if (!dto.getEmail().contains("@")) {
            throw new ValidacionNegocioException("Formato de email inválido");
        }

        // Validar contraseña
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new ValidacionNegocioException("La contraseña debe tener al menos 6 caracteres");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        usuario.setDireccion(dto.getDireccion());
        usuario.setRol(dto.getRol() != null ? dto.getRol() : Usuario.Rol.CLIENTE);

        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(guardado);
    }

    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));

        // Verificar si el nuevo email ya pertenece a otro usuario
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
        return new UsuarioResponseDTO(actualizado);
    }

    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario", id);
        }
        usuarioRepository.deleteById(id);
    }
}