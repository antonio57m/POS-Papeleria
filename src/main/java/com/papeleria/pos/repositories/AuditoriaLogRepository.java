package com.papeleria.pos.repositories;

import com.papeleria.pos.models.AuditoriaLog;
import com.papeleria.pos.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Integer> {

    // 1. Trazabilidad por Cajero
    // Si hay sospechas sobre un empleado específico, el dueño puede ver
    // absolutamente todos los movimientos que hizo en el sistema.
    // Usamos OrderByFechaHoraDesc para que los eventos más recientes aparezcan primero.
    List<AuditoriaLog> findByUsuarioOrderByFechaHoraDesc(Usuario usuario);

    // 2. Filtro por Eventos Críticos
    // Permite buscar acciones específicas como "TICKET_CANCELADO",
    // "APERTURA_CAJON_SIN_VENTA", o "INTENTO_ACCESO_FALLIDO".
    List<AuditoriaLog> findByAccionOrderByFechaHoraDesc(String accion);

    // 3. Auditoría de Rango de Fechas
    // Para revisiones periódicas (ej. cierre de mes o quincena).
    List<AuditoriaLog> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    // 4. Búsqueda combinada: ¿Cuántos tickets canceló el Cajero X esta semana?
    List<AuditoriaLog> findByUsuarioAndAccionAndFechaHoraBetweenOrderByFechaHoraDesc(
            Usuario usuario, String accion, LocalDateTime inicio, LocalDateTime fin);
}