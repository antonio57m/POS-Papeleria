package com.papeleria.pos.controllers;

import com.papeleria.pos.models.AuditoriaLog;
import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.AuditoriaLogRepository;
import com.papeleria.pos.services.AuditoriaLogService;
import com.papeleria.pos.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaLogController {

    private final AuditoriaLogService auditoriaLogService;
    // Inyectamos UsuarioService para poder validar qué empleado hizo la acción
    private final UsuarioService usuarioService;

    // Inyectamos el repositorio para la búsqueda por fechas ordenada
    private final AuditoriaLogRepository auditoriaLogRepository;

    // DTO limpio usando Records para recibir datos desde el Frontend
    public record EventoAuditoriaRequest(Integer idUsuario, String accion, String descripcion) {}

    // --- RUTAS DE LECTURA (GET) ---

    @GetMapping
    public ResponseEntity<List<AuditoriaLog>> obtenerTodos() {
        return ResponseEntity.ok(auditoriaLogService.obtenerTodosLosLogs());
    }

    // Filtrar por tipo de evento (ej. /api/auditoria/accion/TICKET_CANCELADO)
    @GetMapping("/accion/{accion}")
    public ResponseEntity<List<AuditoriaLog>> obtenerPorAccion(@PathVariable String accion) {
        return ResponseEntity.ok(auditoriaLogService.buscarPorAccion(accion));
    }

    // FIX SENIOR: Único método mapeado a "/fechas" para evitar ambigüedad (Mapeo 404/500)
    @GetMapping("/fechas")
    public ResponseEntity<List<AuditoriaLog>> obtenerAuditoriaPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        // Llamamos al repositorio usando las fechas recibidas para que lo devuelva ordenado
        return ResponseEntity.ok(auditoriaLogRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin));
    }

    // --- RUTAS DE ESCRITURA (POST) ---

    @PostMapping
    public ResponseEntity<?> registrarEvento(@RequestBody EventoAuditoriaRequest request) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(request.idUsuario());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No se puede auditar el evento: Usuario no encontrado.");
        }

        // Llamamos al método transaccional autónomo (REQUIRES_NEW)
        auditoriaLogService.registrarEvento(usuarioOpt.get(), request.accion(), request.descripcion());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Historial completo de un cajero en específico
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<AuditoriaLog>> obtenerPorCajero(@PathVariable Integer idUsuario) {
        return usuarioService.buscarPorId(idUsuario)
                // Si lo encuentra, lo mapea a un 200 OK con su historial
                .map(cajero -> ResponseEntity.ok(auditoriaLogService.buscarPorCajero(cajero)))
                // Si no lo encuentra, devuelve un 404 Not Found
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Búsqueda Avanzada combinada (Usuario + Acción + Fechas)
    @GetMapping("/busqueda-avanzada")
    public ResponseEntity<?> busquedaAvanzada(
            @RequestParam Integer idUsuario,
            @RequestParam String accion,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        return usuarioService.buscarPorId(idUsuario)
                // Forzamos el tipo genérico <ResponseEntity<?>> para que el compilador no se confunda
                .<ResponseEntity<?>>map(cajero -> ResponseEntity.ok(auditoriaLogService.busquedaAvanzada(cajero, accion, inicio, fin)))
                .orElseGet(() -> ResponseEntity.badRequest().body("El usuario especificado no existe."));
    }

    // 🛑 ATENCIÓN: Intencionalmente NO se incluyen métodos @PutMapping ni @DeleteMapping.
    // Un sistema de auditoría real debe ser inmutable.
}