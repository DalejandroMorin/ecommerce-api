# 🛒 E-Commerce API

![Java](https://img.shields.io/badge/Java-25-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

API REST modular para sistema de e-commerce desarrollada con **Spring Boot**, **PostgreSQL** y autenticación **JWT**.


---

## ✨ Características

* 🔐 Autenticación JWT con roles (ADMIN / CLIENTE)
* 📦 CRUD completo de productos, usuarios, pedidos y carrito
* 🛒 Carrito con validación de stock
* 💳 Simulación de pagos
* 📜 Historial de pedidos con filtros
* 🔍 Búsqueda avanzada de productos
* 📝 Logging con SLF4J
* 🧪 Tests unitarios (JUnit + Mockito)
* 📚 Documentación con Swagger

---

## 🛠️ Tecnologías

| Tecnología  | Versión |
| ----------- | ------- |
| Java        | 25      |
| Spring Boot | 4.1     |
| PostgreSQL  | 18      |
| JWT         | 0.12.6  |
| Lombok      | 1.18    |
| JUnit       | 5       |
| Mockito     | 5       |
| Swagger     | 2.8     |

---

## 🧱 Arquitectura

Arquitectura en capas:

**Controller → Service → Repository → Model/DTO**

```text
com.david.ecommerce
├── auth/
├── usuario/
├── producto/
├── pedido/
├── carrito/
├── common/
└── config/
```

---

## 🚀 Instalación

### 1. Clonar repositorio

```bash
git clone https://github.com/tu-usuario/ecommerce-api.git
cd ecommerce-api
```

---

### 2. Crear base de datos

```sql
CREATE DATABASE ecommerce;
```

---

### 3. Variables de entorno

#### Windows

```cmd
set DB_URL=jdbc:postgresql://localhost:5432/ecommerce
set DB_USERNAME=postgres
set DB_PASSWORD=tu_password
set JWT_SECRET=tu_clave_secreta
```

#### Linux / Mac

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ecommerce
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password
export JWT_SECRET=tu_clave_secreta
```

---

### 4. Ejecutar

```bash
mvn clean install
mvn spring-boot:run
```

📍 URL:

```
http://localhost:8080
```

📚 Swagger:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Seguridad

* Autenticación basada en JWT
* Control de acceso por roles
* Endpoints protegidos según permisos

---

## 🧪 Tests

```bash
mvn test
```

✔️ Tests unitarios implementados con Mockito
✔️ Cobertura de lógica de negocio

---

## 📡 Endpoints principales

### 🔐 Auth

* POST `/api/auth/register`
* POST `/api/auth/login`

### 📦 Productos

* GET `/api/productos`
* GET `/api/productos/{id}`
* GET `/api/productos/buscar`
* POST `/api/productos` (ADMIN)

### 🛒 Carrito

* GET `/api/carrito`
* POST `/api/carrito/agregar`
* PUT `/api/carrito/items/{id}`
* DELETE `/api/carrito/items/{id}`

### 🧾 Pedidos

* POST `/api/pedidos/desde-carrito`
* POST `/api/pedidos/{id}/pagar`
* GET `/api/pedidos/historial/{id}`

---

## 👤 Crear ADMIN

```sql
UPDATE usuarios 
SET rol = 'ADMIN' 
WHERE email = 'admin@email.com';
```

---

## 📈 Mejoras futuras

* Dockerización 🐳
* Deploy en la nube ☁️
* CI/CD con GitHub Actions 🔄
* Pagos reales con Stripe 💳

---

## 📝 Licencia

MIT

---

## 👨‍💻 Autor

Desarrollado por **David Alejandro Reyes Morín**

> Proyecto enfocado en backend profesional con buenas prácticas
