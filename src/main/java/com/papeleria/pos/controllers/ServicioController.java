package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Servicio;
import com.papeleria.pos.services.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor // Inyección por constructor limpia y segura
public class ServicioController {

    // Al ser 'final', Spring garantiza su inyección en el momento exacto en que arranca el Controller
    private final ServicioService servicioService;

    // --- RUTAS DE LECTURA (GET) ---

    // Catálogo completo de servicios
    @GetMapping
    public ResponseEntity<List<Servicio>> obtenerTodos() {
        return ResponseEntity.ok(servicioService.obtenerTodosLosServicios());
    }

    // Búsqueda específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerPorId(@PathVariable Integer id) {
        return servicioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscador ágil para el cajero (ej. http://localhost:8080/api/servicios/buscar?nombre=engargolado)
    @GetMapping("/buscar")
    public ResponseEntity<List<Servicio>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(servicioService.buscarPorNombre(nombre));
    }

    // Endpoint Estratégico: Devuelve servicios con precio 0.00 (ej. recargas, recibos)
    @GetMapping("/precio-variable")
    public ResponseEntity<List<Servicio>> obtenerServiciosPrecioVariable() {
        return ResponseEntity.ok(servicioService.obtenerServiciosPrecioVariable());
    }

    // --- RUTAS DE ESCRITURA (POST, PUT, DELETE) ---

    // Crear un nuevo servicio
    @PostMapping
    public ResponseEntity<?> crearServicio(@Valid @RequestBody Servicio servicio) {
        try {
            servicio.setId(null); // Forzamos a que la BD asigne el nuevo ID
            Servicio nuevoServicio = servicioService.guardarServicio(servicio);
            return new ResponseEntity<>(nuevoServicio, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Captura validaciones del Service (ej. duplicados o precios negativos)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Actualizar un servicio existente
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarServicio(@PathVariable Integer id, @Valid @RequestBody Servicio servicio) {
        // Uso de isEmpty() para evitar la negación visual (!isPresent())
        if (servicioService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            servicio.setId(id);
            Servicio servicioActualizado = servicioService.guardarServicio(servicio);
            return ResponseEntity.ok(servicioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Eliminar un servicio
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarServicio(@PathVariable Integer id) {
        try {
            servicioService.eliminarServicio(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            // Si el servicio ya fue vendido antes, el escudo del Service lanza el error
            // y respondemos con un 409 Conflict para que el Frontend avise al administrador.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> alternarEstado(@PathVariable Integer id) {
        try {
            servicioService.alternarEstadoServicio(id);
            return ResponseEntity.ok().body("Estado del servicio actualizado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}