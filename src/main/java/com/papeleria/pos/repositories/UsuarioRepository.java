package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // 1. El pilar de la seguridad: Autenticación.
    // Spring Security necesita encontrar al usuario por su login.
    // Usamos Optional para manejar de forma elegante si el usuario no existe.
    Optional<Usuario> findByUsername(String username);

    // 2. Gestión de Personal (Admin Dashboard).
    // Permite al dueño ver solo a los empleados activos o inactivos.
    List<Usuario> findByActivo(Boolean activo);

    // 3. Validación de Disponibilidad.
    // Antes de registrar un nuevo empleado, verificamos si el nombre de usuario ya está tomado.
    boolean existsByUsername(String username);

    // 4. Reporte de Productividad por Rol.
    // Útil para filtrar usuarios por jerarquía (ADMIN o CAJERO).
    List<Usuario> findByRol(Usuario.Rol rol);
}