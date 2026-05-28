package com.papeleria.pos.services;

import com.papeleria.pos.models.AuditoriaLog;
import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.AuditoriaLogRepository;
import com.papeleria.pos.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaLogService {

    @Autowired
    private AuditoriaLogRepository auditoriaLogRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. Registro de Evento (Alta prioridad)
    // Usamos Propagation.REQUIRES_NEW para que, si una venta falla y hace rollback,
    // el log de "Intento de Venta Fallido" SÍ se guarde en la base de datos de todos modos.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(Usuario usuario, String accion, String descripcion) {
        AuditoriaLog log = new AuditoriaLog();
        log.setUsuario(usuario);
        log.setAccion(accion);
        log.setDescripcion(descripcion);
        auditoriaLogRepository.save(log);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEventoSilencioso(String accion, String descripcion) {
        AuditoriaLog log = new AuditoriaLog();
        log.setAccion(accion);
        log.setDescripcion(descripcion);

        // Extraemos quién hizo la petición desde Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            usuarioRepository.findByUsername(auth.getName()).ifPresent(log::setUsuario);
        } // Si es null (ej. Login fallido), se queda en null gracias a tu ALTER TABLE

        auditoriaLogRepository.save(log);
    }
    // 2. Consultas de solo lectura para el Dashboard de Seguridad

    public List<AuditoriaLog> obtenerTodosLosLogs() {
        // En producción, esto debería paginarse para no traer un millón de registros de golpe.
        return auditoriaLogRepository.findAll();
    }

    public List<AuditoriaLog> buscarPorCajero(Usuario cajero) {
        return auditoriaLogRepository.findByUsuarioOrderByFechaHoraDesc(cajero);
    }

    public List<AuditoriaLog> buscarPorAccion(String accionCritica) {
        return auditoriaLogRepository.findByAccionOrderByFechaHoraDesc(accionCritica);
    }

    public List<AuditoriaLog> buscarPorRangoDeFechas(LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaLogRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
    }

    public List<AuditoriaLog> busquedaAvanzada(Usuario cajero, String accion, LocalDateTime inicio, LocalDateTime fin) {
        return auditoriaLogRepository.findByUsuarioAndAccionAndFechaHoraBetweenOrderByFechaHoraDesc(cajero, accion, inicio, fin);
    }
}