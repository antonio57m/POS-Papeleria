package com.papeleria.pos.services;

import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.DetalleVenta;
import com.papeleria.pos.models.Venta;
import com.papeleria.pos.repositories.DetalleVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DetalleVentaService {

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    // 1. Recuperar el ticket completo (para imprimir o devoluciones)
    public List<DetalleVenta> obtenerDetallesPorVenta(Venta venta) {
        return detalleVentaRepository.findByVenta(venta);
    }

    // 2. Guardado en lote (Bulk Insert)
    // Es mucho más eficiente guardar los 10 artículos del ticket de golpe
    // que hacer 10 inserts separados en la base de datos.
    @Transactional
    public List<DetalleVenta> guardarDetalles(List<DetalleVenta> detalles) {
        return detalleVentaRepository.saveAll(detalles);
    }

    // 3. El escudo protector del catálogo
    public boolean itemTieneHistorialDeVentas(TipoItem tipoItem, Integer idItem) {
        return detalleVentaRepository.existsByTipoItemAndIdItem(tipoItem, idItem);
    }

    // 4. Analítica de Negocio (Dashboards)
    public List<Object[]> obtenerProductosMasVendidos() {
        return detalleVentaRepository.findTopProductosVendidos();
    }

    public List<Object[]> obtenerServiciosMasRealizados() {
        return detalleVentaRepository.findTopServiciosRealizados();
    }
}