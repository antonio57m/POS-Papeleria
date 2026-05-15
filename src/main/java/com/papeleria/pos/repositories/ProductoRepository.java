package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // 1. Fundamental para el Punto de Venta: Buscar rápido al "pistolear" el código de barras.
    // Spring crea automáticamente el SQL: SELECT * FROM productos WHERE codigo_barras = ?
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    // 2. Buscador en tiempo real para el cajero (cuando el código de barras no lee o no tiene)
    // SQL: SELECT * FROM productos WHERE nombre LIKE %?%
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // 3. Regla de Negocio Crítica: Reporte de Reabastecimiento
    // Esta consulta nos traerá todos los productos que están a punto de agotarse o ya se agotaron.
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo")
    List<Producto> findProductosConStockCritico();
}