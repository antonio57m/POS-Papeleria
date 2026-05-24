package com.papeleria.pos.repositories;

import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CorteCajaRepository extends JpaRepository<CorteCaja, Integer> {

    // 1. Detección de Turno Activo (Edge Case Crítico)
    // Busca si el cajero ya tiene una caja abierta (fecha_cierre es NULL).
    // Evita que un mismo usuario abra dos cajas simultáneas.
    @Query("SELECT c FROM CorteCaja c WHERE c.usuario = :usuario AND c.fechaCierre IS NULL")
    Optional<CorteCaja> findCajaAbiertaPorUsuario(@Param("usuario") Usuario usuario);

    // 2. Auditoría de Fraudes (Reporte de Faltantes/Sobrantes)
    // Permite al dueño buscar todos los cortes donde la diferencia no haya sido cero.
    // SQL: SELECT * FROM cortes_caja WHERE diferencia <> 0.00
    @Query("SELECT c FROM CorteCaja c WHERE c.diferencia <> 0")
    List<CorteCaja> findCortesConDiscrepancia();

    // 3. Reportes Históricos
    // Para ver todos los cortes de caja en una quincena o mes específico.
    List<CorteCaja> findByFechaAperturaBetween(LocalDateTime inicio, LocalDateTime fin);

    // 4. Verificación de Turnos "Zombies"
    // Busca turnos que se abrieron en días anteriores y nunca se cerraron.
    @Query("SELECT c FROM CorteCaja c WHERE c.fechaCierre IS NULL AND c.fechaApertura < :fechaCorte")
    List<CorteCaja> findCortesOlvidados(@Param("fechaCorte") LocalDateTime fechaCorte);

    // NUEVO MÉTODO A AGREGAR PARA EL REPORTE SEMANAL
    List<CorteCaja> findByFechaCierreBetween(LocalDateTime inicio, LocalDateTime fin);
}