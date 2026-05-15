package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    // 1. Buscador ágil para el cajero.
    // Como no hay lector de código de barras, el cajero tecleará "acta" o "luz".
    // Ignora mayúsculas y acentos a nivel base de datos.
    List<Servicio> findByNombreContainingIgnoreCase(String nombre);

    // 2. Prevención de datos duplicados (Caso de Borde).
    // Antes de que el Administrador guarde un nuevo servicio, el sistema debe usar
    // este método para verificar que no exista ya un "Trámite CURP" registrado,
    // evitando ensuciar el catálogo y los reportes financieros.
    Optional<Servicio> findByNombreIgnoreCase(String nombre);

    // 3. Catálogo dinámico de cobros variables.
    // Útil para cuando el frontend necesite renderizar un menú rápido que solo muestre
    // los servicios que requieren que el cajero introduzca el monto manualmente
    // (buscando aquellos donde precioVenta = 0.00).
    List<Servicio> findByPrecioVenta(BigDecimal precioVenta);
}