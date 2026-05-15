package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

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

    // Editar un empleado (ej. cambiarle el rol de CAJERO a ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @Valid @RequestBody Usuario usuario) {
        if (usuarioService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            usuario.setId(id);
            Usuario usuarioActualizado = usuarioService.guardarUsuario(usuario);
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