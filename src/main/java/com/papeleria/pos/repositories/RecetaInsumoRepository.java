package com.papeleria.pos.repositories;

import com.papeleria.pos.models.RecetaInsumo;
import com.papeleria.pos.models.RecetaInsumoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaInsumoRepository extends JpaRepository<RecetaInsumo, RecetaInsumoId> {
    // Necesitamos buscar todas las recetas (ej. "Tinta" y "Hoja") que pertenecen a un servicio (ej. "Copia a Color")
    List<RecetaInsumo> findByIdIdServicio(Integer idServicio);
}