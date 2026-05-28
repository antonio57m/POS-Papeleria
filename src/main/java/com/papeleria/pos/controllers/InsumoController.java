package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Insumo;
import com.papeleria.pos.services.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    // --- RUTAS DE LECTURA (GET) ---

    @GetMapping
    public ResponseEntity<List<Insumo>> obtenerTodos() {
        return ResponseEntity.ok(insumoService.obtenerTodosLosInsumos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insumo> obtenerPorId(@PathVariable Integer id) {
        return insumoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscador manual de materiales
    // URL: http://localhost:8080/api/insumos/buscar?nombre=tinta
    @GetMapping("/buscar")
    public ResponseEntity<List<Insumo>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(insumoService.buscarPorNombre(nombre));
    }

    // Dashboard: Alertas de insumos bajos
    // URL: http://localhost:8080/api/insumos/alertas?umbral=10.5
    @GetMapping("/alertas")
    public ResponseEntity<List<Insumo>> obtenerAlertas(@RequestParam BigDecimal umbral) {
        return ResponseEntity.ok(insumoService.obtenerAlertas(umbral));
    }

    // --- RUTAS DE ESCRITURA (POST, PUT, PATCH) ---

    @PostMapping
    public ResponseEntity<Insumo> crearInsumo(@Valid @RequestBody Insumo insumo) {
        insumo.setId(null);
        Insumo nuevoInsumo = insumoService.guardarInsumo(insumo);
        return new ResponseEntity<>(nuevoInsumo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insumo> actualizarInsumo(@PathVariable Integer id, @Valid @RequestBody Insumo insumo) {
        if (insumoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        insumo.setId(id);
        Insumo insumoActualizado = insumoService.guardarInsumo(insumo);
        return ResponseEntity.ok(insumoActualizado);
    }

    // Abastecer el almacén cuando se compra material
    // URL: http://localhost:8080/api/insumos/1/abastecer?cantidad=1000
    @PatchMapping("/{id}/abastecer")
    public ResponseEntity<?> abastecerInsumo(@PathVariable Integer id, @RequestParam BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("La cantidad a abastecer debe ser mayor a 0");
        }
        try {
            insumoService.abastecerInsumo(id, cantidad);
            return ResponseEntity.ok().body("Stock de insumo abastecido correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Descontar material (por merma o ajuste manual)
    // URL: http://localhost:8080/api/insumos/1/descontar?cantidad=5.5
    @PatchMapping("/{id}/descontar")
    public ResponseEntity<?> descontarStock(@PathVariable Integer id, @RequestParam BigDecimal cantidad) {
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("La cantidad a descontar debe ser mayor a 0");
        }
        try {
            insumoService.descontarStock(id, cantidad);
            return ResponseEntity.ok().body("Stock de insumo descontado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> alternarEstado(@PathVariable Integer id) {
        try {
            insumoService.alternarEstado(id);
            return ResponseEntity.ok().body("Estado actualizado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PatchMapping("/{id}/merma")
    public ResponseEntity<?> registrarMerma(@PathVariable Integer id, @RequestParam BigDecimal cantidad, @RequestParam String motivo) {
        try {
            insumoService.registrarMerma(id, cantidad, motivo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}