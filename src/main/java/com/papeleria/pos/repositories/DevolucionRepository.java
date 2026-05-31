package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {

    // Nos servirá más adelante para que el Admin vea qué falta por auditar en la "Bandeja Roja"
    List<Devolucion> findByAuditadaFalse();
    @Query("SELECT COALESCE(SUM(d.montoReintegrado), 0) FROM Devolucion d WHERE d.usuarioCajero.id = :idCajero AND d.fechaHora >= :fechaApertura")
    BigDecimal sumarEfectivoDevueltoPorTurno(Integer idCajero, LocalDateTime fechaApertura);

}