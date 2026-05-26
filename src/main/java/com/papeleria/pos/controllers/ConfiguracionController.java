package com.papeleria.pos.controllers;

import com.papeleria.pos.services.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    // Actualizamos el endpoint para devolver ambos datos
    @GetMapping("/reportes")
    public ResponseEntity<Map<String, String>> obtenerConfiguracion() {
        return ResponseEntity.ok(Map.of(
                "correos", configuracionService.obtenerCorreosReporte(),
                "dia", configuracionService.obtenerDiaReporteSemanal()
        ));
    }

    // Actualizamos para recibir ambos datos
    @PutMapping("/reportes")
    public ResponseEntity<?> actualizarConfiguracion(@RequestBody Map<String, String> request) {
        configuracionService.guardarConfiguracionReportes(
                request.get("correos"),
                request.get("dia")
        );
        return ResponseEntity.ok().build();
    }
}