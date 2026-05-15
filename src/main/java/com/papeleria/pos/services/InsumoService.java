package com.papeleria.pos.services;

import com.papeleria.pos.models.Insumo;
import com.papeleria.pos.repositories.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InsumoService {

    @Autowired
    private InsumoRepository insumoRepository;

    public List<Insumo> obtenerTodosLosInsumos() {
        return insumoRepository.findAll();
    }

    public Optional<Insumo> buscarPorId(Integer id) {
        return insumoRepository.findById(id);
    }

    public List<Insumo> buscarPorNombre(String nombre) {
        return insumoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Insumo guardarInsumo(Insumo insumo) {
        // Regla: No podemos tener cantidades negativas en el almacén
        if (insumo.getCantidadActual() != null && insumo.getCantidadActual().compareTo(BigDecimal.ZERO) < 0) {
            insumo.setCantidadActual(BigDecimal.ZERO);
        }
        return insumoRepository.save(insumo);
    }

    // Método para cuando se compra material al proveedor
    @Transactional
    public void abastecerInsumo(Integer idInsumo, BigDecimal cantidadComprada) {
        Insumo insumo = insumoRepository.findById(idInsumo)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));

        BigDecimal nuevoStock = insumo.getCantidadActual().add(cantidadComprada);
        insumo.setCantidadActual(nuevoStock);
        insumoRepository.save(insumo);
    }

    // Método crítico para cuando se realiza una venta que consume material
    @Transactional
    public void descontarStock(Integer idInsumo, BigDecimal cantidadConsumida) {
        Insumo insumo = insumoRepository.findById(idInsumo)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));

        BigDecimal nuevoStock = insumo.getCantidadActual().subtract(cantidadConsumida);

        if (nuevoStock.compareTo(BigDecimal.ZERO) < 0) {
            // BLINDAJE ESTRICTO: Abortar transacción si no hay material suficiente
            throw new IllegalStateException("Operación rechazada: No hay suficiente '" + insumo.getNombre() + "' en el almacén interno. Stock actual: " + insumo.getCantidadActual() + " " + insumo.getUnidadMedida());
        }

        insumo.setCantidadActual(nuevoStock);
        insumoRepository.save(insumo);
    }

    // Panel de alertas dinámico para el Administrador
    public List<Insumo> obtenerAlertas(BigDecimal umbral) {
        return insumoRepository.findInsumosBajos(umbral);
    }

    @Transactional
    public void alternarEstado(Integer id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo no encontrado"));
        boolean estadoActual = insumo.getActivo() != null ? insumo.getActivo() : true;
        insumo.setActivo(!estadoActual);
        insumoRepository.save(insumo);
    }
}