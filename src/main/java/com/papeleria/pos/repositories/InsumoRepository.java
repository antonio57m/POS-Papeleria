package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InsumoRepository extends JpaRepository<Insumo, Integer> {

    // 1. Buscador para la gestión de inventario de suministros internos.
    List<Insumo> findByNombreContainingIgnoreCase(String nombre);

    // 2. Reporte de Alerta de Suministros.
    // Como los insumos no tienen un "stock_minimo" fijo en la tabla,
    // permitimos que el sistema le pregunte a la base de datos qué insumos
    // están por debajo de un umbral específico enviado desde el Service.
    @Query("SELECT i FROM Insumo i WHERE i.cantidadActual <= :umbral")
    List<Insumo> findInsumosBajos(@Param("umbral") BigDecimal umbral);

    // 3. Validación de Insumo por Unidad.
    // Útil para reportes técnicos: saber cuántos insumos tenemos de un tipo (ej. "ml" o "pz").
    List<Insumo> findByUnidadMedida(String unidadMedida);
}