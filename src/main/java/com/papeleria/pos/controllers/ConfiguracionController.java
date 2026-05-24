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

    @GetMapping("/correos")
    public ResponseEntity<Map<String, String>> obtenerCorreos() {
        return ResponseEntity.ok(Map.of("correos", configuracionService.obtenerCorreosReporte()));
    }

    @PutMapping("/correos")
    public ResponseEntity<?> actualizarCorreos(@RequestBody Map<String, String> request) {
        configuracionService.guardarCorreosReporte(request.get("correos"));
        return ResponseEntity.ok().build();
    }
}