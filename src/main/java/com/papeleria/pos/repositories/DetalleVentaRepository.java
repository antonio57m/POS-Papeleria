package com.papeleria.pos.repositories;

import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.DetalleVenta;
import com.papeleria.pos.models.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    // 1. Recuperación del Ticket
    // Esencial para cuando el cliente regresa a pedir una reimpresión o una devolución.
    List<DetalleVenta> findByVenta(Venta venta);

    // 2. Integridad de Datos (Protección del Catálogo)
    // Antes de que el administrador intente borrar una "Goma" o un "Servicio de Luz",
    // el sistema debe validar si ese ítem ya existe en algún ticket histórico.
    // Si devuelve true, lanzaremos una excepción para impedir el borrado y sugerir
    // cambiar el estado a "inactivo" para no romper los reportes contables.
    boolean existsByTipoItemAndIdItem(TipoItem tipoItem, Integer idItem);

    // 3. Analítica: Ranking de Productos más vendidos.
    // Un Senior no procesa bucles for() en Java para sumar ventas. Le dice a MariaDB
    // que haga el GROUP BY y el ORDER BY porque el motor SQL está diseñado para eso.
    @Query("SELECT d.idItem, SUM(d.cantidad) FROM DetalleVenta d " +
            "WHERE d.tipoItem = 'PRODUCTO' " +
            "GROUP BY d.idItem ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findTopProductosVendidos();

    // 4. Analítica: Ranking de Servicios (Copias, Trámites, etc).
    @Query("SELECT d.idItem, COUNT(d) FROM DetalleVenta d " +
            "WHERE d.tipoItem = 'SERVICIO' " +
            "GROUP BY d.idItem ORDER BY COUNT(d) DESC")
    List<Object[]> findTopServiciosRealizados();
}