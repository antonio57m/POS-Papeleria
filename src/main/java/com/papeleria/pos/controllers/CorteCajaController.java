package com.papeleria.pos.controllers;

import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.services.CorteCajaService;
import com.papeleria.pos.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cortes-caja")
@RequiredArgsConstructor
public class CorteCajaController {

    private final CorteCajaService corteCajaService;
    private final UsuarioService usuarioService;

    // DTOs limpios usando Records para la apertura y cierre de caja
    public record AbrirCajaRequest(Integer idUsuario) {}
    public record CerrarCajaRequest(BigDecimal montoDeclarado, BigDecimal montoEsperado) {}

    // --- RUTAS DE ESCRITURA (POST, PUT) ---

    // 1. Abrir el turno al inicio del día
    // URL: http://localhost:8080/api/cortes-caja/abrir
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaja(@RequestBody AbrirCajaRequest request) {
        return usuarioService.buscarPorId(request.idUsuario())
                // Eliminamos el tipo explícito, Java lo infiere automáticamente
                .map(cajero -> {
                    try {
                        CorteCaja nuevoCorte = corteCajaService.abrirCaja(cajero);
                        return new ResponseEntity<>(nuevoCorte, HttpStatus.CREATED);
                    } catch (IllegalStateException e) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("Usuario no encontrado."));
    }

    // 2. Cerrar el turno (Corte a Ciegas)
    // URL: http://localhost:8080/api/cortes-caja/1/cerrar
    @PutMapping("/{id}/cerrar")
    public ResponseEntity<?> cerrarCaja(@PathVariable Integer id, @RequestBody CerrarCajaRequest request) {
        try {
            CorteCaja corteCerrado = corteCajaService.cerrarCaja(id, request.montoDeclarado(), request.montoEsperado());
            return ResponseEntity.ok(corteCerrado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- RUTAS DE LECTURA (GET) ---

    // Saber si un cajero tiene un turno activo actualmente (útil para que el Frontend sepa si mostrar la pantalla de ventas)
    @GetMapping("/activa/usuario/{idUsuario}")
    public ResponseEntity<?> obtenerCajaActiva(@PathVariable Integer idUsuario) {
        return usuarioService.buscarPorId(idUsuario)
                .<ResponseEntity<?>>map(cajero -> corteCajaService.obtenerCajaActiva(cajero)
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build()))
                .orElseGet(() -> ResponseEntity.badRequest().body("Usuario no encontrado."));
    }

    // Dashboard del Admin: Ver cortes donde hubo faltantes o sobrantes
    @GetMapping("/discrepancias")
    public ResponseEntity<List<CorteCaja>> obtenerDiscrepancias() {
        return ResponseEntity.ok(corteCajaService.obtenerCortesConDiscrepancia());
    }

    // Dashboard del Admin: Ver turnos que los cajeros olvidaron cerrar en días anteriores
    // URL: http://localhost:8080/api/cortes-caja/zombies?fechaLimite=2026-04-30T00:00:00
    @GetMapping("/zombies")
    public ResponseEntity<List<CorteCaja>> obtenerZombies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaLimite) {
        return ResponseEntity.ok(corteCajaService.buscarTurnosZombies(fechaLimite));
    }
}