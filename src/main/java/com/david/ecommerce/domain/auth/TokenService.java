package com.david.ecommerce.domain.auth;

public interface TokenService {

    String generateToken(String email, String rol);

    String extractEmail(String token);

    String extractRol(String token);

    boolean isTokenValid(String token, String email);
}
