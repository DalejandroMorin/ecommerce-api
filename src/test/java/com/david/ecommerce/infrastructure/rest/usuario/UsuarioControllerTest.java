package com.david.ecommerce.infrastructure.rest.usuario;

import com.david.ecommerce.application.usuario.UsuarioUseCase;
import com.david.ecommerce.application.usuario.dto.UsuarioRequestDTO;
import com.david.ecommerce.application.usuario.dto.UsuarioResponseDTO;
import com.david.ecommerce.common.exception.GlobalExceptionHandler;
import com.david.ecommerce.common.exception.RecursoNoEncontradoException;
import com.david.ecommerce.domain.common.Rol;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest {

    private MockMvc mockMvc;
    private UsuarioUseCase usuarioUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        usuarioUseCase = mock(UsuarioUseCase.class);
        var controller = new UsuarioController(usuarioUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listarTodos_Retorna200() throws Exception {
        var usuario = new UsuarioResponseDTO(1L, "Juan", "juan@email.com", "Calle 123",
                Rol.CLIENTE, LocalDateTime.now());

        when(usuarioUseCase.obtenerTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].email").value("juan@email.com"));
    }

    @Test
    void obtenerPorId_Existente_Retorna200() throws Exception {
        var usuario = new UsuarioResponseDTO(1L, "Juan", "juan@email.com", "Calle 123",
                Rol.CLIENTE, LocalDateTime.now());

        when(usuarioUseCase.obtenerPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void obtenerPorId_NoExistente_Retorna404() throws Exception {
        when(usuarioUseCase.obtenerPorId(99L)).thenThrow(new RecursoNoEncontradoException("Usuario", 99L));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_Exitoso_Retorna201() throws Exception {
        var request = new UsuarioRequestDTO("Juan", "juan@email.com", "pass123", "Calle 123", Rol.CLIENTE);
        var response = new UsuarioResponseDTO(1L, "Juan", "juan@email.com", "Calle 123",
                Rol.CLIENTE, LocalDateTime.now());

        when(usuarioUseCase.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void actualizar_Exitoso_Retorna200() throws Exception {
        var request = new UsuarioRequestDTO("Juan Actualizado", "juan@email.com", null, "Calle Nueva", Rol.CLIENTE);
        var response = new UsuarioResponseDTO(1L, "Juan Actualizado", "juan@email.com", "Calle Nueva",
                Rol.CLIENTE, LocalDateTime.now());

        when(usuarioUseCase.actualizar(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Actualizado"));
    }

    @Test
    void eliminar_Exitoso_Retorna204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }
}
