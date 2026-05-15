package com.papeleria.pos.repositories;

import com.papeleria.pos.enums.MetodoPago;
import com.papeleria.pos.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // 1. Reporte Financiero Diario/Mensual
    // Permite al administrador ver todas las ventas en un rango de fechas.
    List<Venta> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    // 2. Conciliación Bancaria y Gubernamental
    // Crítico para separar el dinero físico del digital (tarjetas de gobierno, QR, etc).
    List<Venta> findByMetodoPagoAndFechaHoraBetween(MetodoPago metodoPago, LocalDateTime inicio, LocalDateTime fin);

    // 3. El Motor del Corte a Ciegas
    // Esta consulta es vital. Cuando el cajero cierra turno, calculamos cuánto dinero
    // debería tener físicamente en la caja (solo efectivo) basándonos en su usuario y turno.
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuario.id = :usuarioId " +
            "AND v.metodoPago = 'EFECTIVO' AND v.fechaHora BETWEEN :inicio AND :fin")
    BigDecimal calcularTotalEfectivoPorCajero(@Param("usuarioId") Integer usuarioId,
                                              @Param("inicio") LocalDateTime inicio,
                                              @Param("fin") LocalDateTime fin);

    // 4. Auditoría Rápida de Ticket
    // Para buscar un ticket específico si un cliente viene a reclamar.
    // Usamos 'JOIN FETCH' para traer el usuario y no hacer múltiples consultas a la BD (Optimización N+1).
    @Query("SELECT v FROM Venta v JOIN FETCH v.usuario WHERE v.id = :idVenta")
    Optional<Venta> findVentaConUsuario(@Param("idVenta") Integer idVenta);
}