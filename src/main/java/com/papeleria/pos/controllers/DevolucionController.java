package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Devolucion;
import com.papeleria.pos.services.DevolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService devolucionService;

    // DTO para recibir los datos de manera limpia
    public record DevolucionRequest(
            Integer idDetalle,
            Integer idCajero,
            BigDecimal cantidadADevolver,
            String motivo,
            Boolean esMerma) {}

    @PostMapping
    public ResponseEntity<?> procesarDevolucion(@RequestBody DevolucionRequest request) {
        try {
            Devolucion dev = devolucionService.procesarDevolucion(
                    request.idDetalle(),
                    request.idCajero(),
                    request.cantidadADevolver(),
                    request.motivo(),
                    request.esMerma()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Devolución procesada con éxito",
                    "idDevolucion", dev.getId(),
                    "montoReintegrado", dev.getMontoReintegrado(),
                    "alerta", "Recuerda colocar el ticket y el producto en la Bandeja Roja para auditoría."
            ));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Atrapamos las reglas de negocio (ej. "No puedes devolver más de lo comprado")
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Error general del servidor
            return ResponseEntity.internalServerError().body(Map.of("error", "Error procesando la devolución."));
        }
    }

    // (Opcional) Dejo este endpoint preparado para cuando hagamos el Dashboard del Admin
    @GetMapping("/pendientes")
    public ResponseEntity<?> obtenerPendientesAuditoria() {
        // Retornaría devolucionRepository.findByAuditadaFalse();
        return ResponseEntity.ok().build();
    }
}