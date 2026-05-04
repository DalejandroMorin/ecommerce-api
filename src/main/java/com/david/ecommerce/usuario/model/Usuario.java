package com.david.ecommerce.usuario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data                   // Lombok: genera getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Constructor vacío (obligatorio para JPA)
@AllArgsConstructor     // Constructor con todos los campos (opcional pero útil)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // En fase posterior se encriptará con BCrypt

    @Column(length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    // Relación con Pedido (1 usuario -> muchos pedidos)
    // Se usará en Fase 3, por ahora en comentario o lazy
    // @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // private List<Pedido> pedidos = new ArrayList<>();

    // Relación con Carrito (1 usuario -> 1 carrito activo)
    // Se puede implementar también en Fase 3
    // @OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // private Carrito carrito;

    // Inicialización automática de la fecha de registro
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }

    // Enum para el rol del usuario
    public enum Rol {
        CLIENTE,
        ADMIN
    }
}