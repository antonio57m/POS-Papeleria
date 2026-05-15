package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Devolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Integer> {

    // Nos servirá más adelante para que el Admin vea qué falta por auditar en la "Bandeja Roja"
    List<Devolucion> findByAuditadaFalse();

}