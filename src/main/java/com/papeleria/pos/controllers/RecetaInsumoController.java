package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Insumo;
import com.papeleria.pos.models.RecetaInsumo;
import com.papeleria.pos.models.Servicio;
import com.papeleria.pos.services.InsumoService;
import com.papeleria.pos.services.RecetaInsumoService;
import com.papeleria.pos.services.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class RecetaInsumoController {

    private final RecetaInsumoService recetaInsumoService;
    private final ServicioService servicioService;
    private final InsumoService insumoService;

    // DTO (Data Transfer Object) interno usando Records (Java 16+).
    // Ideal para mapear el JSON que enviará el frontend directamente a estas 3 variables.
    public record RecetaRequest(Integer idServicio, Integer idInsumo, BigDecimal cantidadADescontar) {}

    // --- RUTAS DE LECTURA (GET) ---

    // Obtener la "receta" completa de un servicio específico
    // URL: http://localhost:8080/api/recetas/servicio/1
    @GetMapping("/servicio/{idServicio}")
    public ResponseEntity<List<RecetaInsumo>> obtenerRecetaDeServicio(@PathVariable Integer idServicio) {
        return ResponseEntity.ok(recetaInsumoService.obtenerInsumosDeServicio(idServicio));
    }

    // --- RUTAS DE ESCRITURA (POST, DELETE) ---

    // Vincular un insumo a un servicio
    @PostMapping
    public ResponseEntity<?> vincularInsumo(@RequestBody RecetaRequest request) {
        // 1. Validamos que el servicio realmente exista en la base de datos
        Optional<Servicio> servicioOpt = servicioService.buscarPorId(request.idServicio());
        if (servicioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El servicio con ID " + request.idServicio() + " no existe.");
        }

        // 2. Validamos que el insumo exista
        Optional<Insumo> insumoOpt = insumoService.buscarPorId(request.idInsumo());
        if (insumoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El insumo con ID " + request.idInsumo() + " no existe.");
        }

        // 3. Ejecutamos la vinculación
        try {
            RecetaInsumo nuevaReceta = recetaInsumoService.vincularInsumoAServicio(
                    servicioOpt.get(),
                    insumoOpt.get(),
                    request.cantidadADescontar()
            );
            return new ResponseEntity<>(nuevaReceta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Atrapa el error si la cantidad a descontar es negativa o cero
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Desvincular un insumo de un servicio (Eliminar parte de la receta)
    // URL: http://localhost:8080/api/recetas/servicio/1/insumo/2
    @DeleteMapping("/servicio/{idServicio}/insumo/{idInsumo}")
    public ResponseEntity<Void> desvincularInsumo(@PathVariable Integer idServicio, @PathVariable Integer idInsumo) {
        recetaInsumoService.desvincularInsumoDeServicio(idServicio, idInsumo);
        return ResponseEntity.noContent().build();
    }
}