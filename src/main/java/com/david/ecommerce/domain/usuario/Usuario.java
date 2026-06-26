package com.david.ecommerce.domain.usuario;

import java.time.LocalDateTime;

public class Usuario {

    public enum Rol {
        CLIENTE,
        ADMIN
    }

    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String direccion;
    private Rol rol;
    private LocalDateTime fechaRegistro;

    public Usuario() {}

    public Usuario(String nombre, String email, String password, String direccion, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.direccion = direccion;
        this.rol = rol;
    }

    public void validar() {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
