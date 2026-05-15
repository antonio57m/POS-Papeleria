package com.papeleria.pos.services;

import com.papeleria.pos.models.Servicio;
import com.papeleria.pos.repositories.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ServicioService {

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private DetalleVentaService detalleVentaService;

    public List<Servicio> obtenerTodosLosServicios() {
        return servicioRepository.findAll();
    }

    public Optional<Servicio> buscarPorId(Integer id) {
        return servicioRepository.findById(id);
    }

    // Buscador ágil: El cajero teclea "engargolado" y salen las opciones
    public List<Servicio> buscarPorNombre(String nombre) {
        return servicioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // Método Estratégico: Devuelve solo los servicios que no tienen precio fijo.
    // El frontend usará esto para saber cuándo desplegar un campo de texto extra
    // pidiendo al cajero que ingrese el monto a cobrar (ej. Recarga Telcel de $50 o $100).
    public List<Servicio> obtenerServiciosPrecioVariable() {
        return servicioRepository.findByPrecioVenta(BigDecimal.ZERO);
    }

    @Transactional
    public Servicio guardarServicio(Servicio servicio) {
        // 1. Blindaje contra errores de dedo
        if (servicio.getPrecioVenta() != null && servicio.getPrecioVenta().compareTo(BigDecimal.ZERO) < 0) {
            servicio.setPrecioVenta(BigDecimal.ZERO);
        }

        // 2. Protección de Duplicidad Estricta
        // Verificamos si el nombre ya existe. Si existe y NO es el mismo servicio
        // que estamos editando actualmente, lanzamos un error para proteger el catálogo.
        Optional<Servicio> existente = servicioRepository.findByNombreIgnoreCase(servicio.getNombre());
        if (existente.isPresent() && !existente.get().getId().equals(servicio.getId())) {
            throw new IllegalArgumentException("El servicio '" + servicio.getNombre() + "' ya está registrado.");
        }

        return servicioRepository.save(servicio);
    }
    @Transactional
    public void eliminarServicio(Integer id) {
        if (detalleVentaService.itemTieneHistorialDeVentas(com.papeleria.pos.enums.TipoItem.SERVICIO, id)) {
            throw new IllegalStateException("No se puede eliminar el servicio porque ya existe en el historial de ventas. Se sugiere desactivarlo.");
        }
        servicioRepository.deleteById(id);
    }

    @Transactional
    public void alternarEstadoServicio(Integer id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        boolean estadoActual = servicio.getActivo() != null ? servicio.getActivo() : true;
        servicio.setActivo(!estadoActual);

        servicioRepository.save(servicio);
    }
}