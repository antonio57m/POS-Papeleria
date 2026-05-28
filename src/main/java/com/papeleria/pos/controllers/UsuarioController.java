package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.services.UsuarioService;
// --- NUEVOS IMPORTS DE SEGURIDAD ---
import com.papeleria.pos.security.CustomUserDetailsService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
// -----------------------------------
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    // INYECTAMOS EL SERVICIO DE DETALLES DE USUARIO
    private final CustomUserDetailsService customUserDetailsService;

    // --- RUTAS DE LECTURA (GET) ---

    // Dashboard del Admin: Ver a todos los empleados (activos e inactivos)
    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    // Filtro para selectores: Traer solo a los que pueden iniciar sesión hoy
    @GetMapping("/activos")
    public ResponseEntity<List<Usuario>> obtenerActivos() {
        return ResponseEntity.ok(usuarioService.obtenerUsuariosActivos());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<Usuario> obtenerPorUsername(@PathVariable String username) {
        return usuarioService.buscarPorUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- RUTAS DE ESCRITURA (POST, PUT, DELETE) ---

    // Registrar un nuevo empleado
    @PostMapping
    public ResponseEntity<?> crearUsuario(@Valid @RequestBody Usuario usuario) {
        try {
            usuario.setId(null); // Forzamos la creación
            Usuario nuevoUsuario = usuarioService.guardarUsuario(usuario);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Atrapa el error si el username ya está en uso
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Editar un empleado
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @Valid @RequestBody Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        Optional<Usuario> usuarioAntiguoOpt = usuarioService.buscarPorId(id);

        if (usuarioAntiguoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Usuario usuarioAntiguo = usuarioAntiguoOpt.get();
            usuario.setId(id);

            // 1. Detectamos si hubo un cambio crítico ANTES de guardar
            boolean cambioCritico = !usuarioAntiguo.getUsername().equals(usuario.getUsername()) ||
                    !usuarioAntiguo.getRol().equals(usuario.getRol()) ||
                    !usuarioAntiguo.getPasswordHash().equals(usuario.getPasswordHash()) ||
                    !usuarioAntiguo.getActivo().equals(usuario.getActivo());

            // 2. Guardamos el usuario en base de datos
            Usuario usuarioActualizado = usuarioService.guardarUsuario(usuario);

            // 3. MANEJO DE SESIÓN SEGURO
            Authentication authActual = SecurityContextHolder.getContext().getAuthentication();

            if (authActual != null && authActual.isAuthenticated()) {
                String usernameLogueado = authActual.getName();

                // Si el usuario editado es el mismo que tiene la sesión activa
                if (usernameLogueado.equals(usuarioAntiguo.getUsername())) {

                    if (cambioCritico) {
                        // LA SOLUCIÓN DEFINITIVA: Cierre de sesión total (Cookies + Contexto + Sesión HTTP)
                        new SecurityContextLogoutHandler().logout(request, response, authActual);

                        // Devolvemos 205 para avisarle al Frontend
                        return ResponseEntity.status(HttpStatus.RESET_CONTENT).body("SESION_CERRADA");
                    }
                }
            }

            return ResponseEntity.ok(usuarioActualizado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Dar de baja a un empleado (Borrado Lógico)
    // Aunque el verbo es DELETE para respetar REST, por debajo solo apaga la bandera "activo"
    @DeleteMapping("/{id}")
    public ResponseEntity<?> darDeBajaUsuario(@PathVariable Integer id) {
        try {
            usuarioService.darDeBajaUsuario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}