package com.david.ecommerce.infrastructure.rest.auth;

import com.david.ecommerce.application.auth.AuthUseCase;
import com.david.ecommerce.common.exception.EmailDuplicadoException;
import com.david.ecommerce.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthUseCase authUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authUseCase = mock(AuthUseCase.class);
        var controller = new AuthController(authUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_Exitoso_Retorna201() throws Exception {
        var request = new RegisterRequestDTO("Juan", "juan@email.com", "pass123", "Calle 123");
        var response = new AuthResponseDTO("jwt-token", "juan@email.com", "Juan", "CLIENTE");

        when(authUseCase.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("juan@email.com"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void register_EmailDuplicado_Retorna400() throws Exception {
        var request = new RegisterRequestDTO("Juan", "juan@email.com", "pass123", "Calle 123");

        when(authUseCase.register(any())).thenThrow(new EmailDuplicadoException("juan@email.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("juan@email.com")));
    }

    @Test
    void login_Exitoso_Retorna200() throws Exception {
        var request = new LoginRequestDTO("juan@email.com", "pass123");
        var response = new AuthResponseDTO("jwt-token", "juan@email.com", "Juan", "CLIENTE");

        when(authUseCase.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("juan@email.com"));
    }

    @Test
    void login_CredencialesInvalidas_Retorna500() throws Exception {
        var request = new LoginRequestDTO("juan@email.com", "wrong");

        when(authUseCase.login(any())).thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
