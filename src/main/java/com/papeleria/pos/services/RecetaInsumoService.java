package com.papeleria.pos.services;

import com.papeleria.pos.models.Insumo;
import com.papeleria.pos.models.RecetaInsumo;
import com.papeleria.pos.models.RecetaInsumoId;
import com.papeleria.pos.models.Servicio;
import com.papeleria.pos.repositories.RecetaInsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RecetaInsumoService {

    @Autowired
    private RecetaInsumoRepository recetaInsumoRepository;

    // Obtiene todos los materiales necesarios para ejecutar un servicio
    public List<RecetaInsumo> obtenerInsumosDeServicio(Integer idServicio) {
        return recetaInsumoRepository.findByIdIdServicio(idServicio);
    }

    @Transactional
    public RecetaInsumo vincularInsumoAServicio(Servicio servicio, Insumo insumo, BigDecimal cantidadADescontar) {
        // 1. Validar regla de negocio matemática
        if (cantidadADescontar.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0");
        }

        // 2. Construir la llave compuesta
        RecetaInsumoId id = new RecetaInsumoId(servicio.getId(), insumo.getId());

        // 3. Ensamblar la entidad
        RecetaInsumo receta = new RecetaInsumo();
        receta.setId(id);
        receta.setServicio(servicio);
        receta.setInsumo(insumo);
        receta.setCantidadDescontar(cantidadADescontar);

        return recetaInsumoRepository.save(receta);
    }

    @Transactional
    public void desvincularInsumoDeServicio(Integer idServicio, Integer idInsumo) {
        RecetaInsumoId id = new RecetaInsumoId(idServicio, idInsumo);
        recetaInsumoRepository.deleteById(id);
    }
}