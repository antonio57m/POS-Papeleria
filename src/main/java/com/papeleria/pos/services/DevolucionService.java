package com.papeleria.pos.services;

import com.papeleria.pos.enums.EstadoVenta;
import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.*;
import com.papeleria.pos.repositories.DetalleVentaRepository;
import com.papeleria.pos.repositories.DevolucionRepository;
import com.papeleria.pos.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DevolucionService {

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProductoService productoService;

    @Autowired private AuditoriaLogService auditoriaLogService;

    @Transactional
    public Devolucion procesarDevolucion(Integer idDetalle, Integer idCajero, BigDecimal cantidadADevolver, String motivo, Boolean esMerma) {

        // 1. Validaciones iniciales
        if (cantidadADevolver.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor a cero.");
        }

        DetalleVenta detalle = detalleVentaRepository.findById(idDetalle)
                .orElseThrow(() -> new IllegalArgumentException("La línea del ticket no existe."));
        // --- BLINDAJE ANTI-FRAUDE: Bloquear devolución de intangibles ---
        if (detalle.getTipoItem() == TipoItem.SERVICIO) {
            // FIX SENIOR: Usamos getIdItem() porque la relación es polimórfica
            auditoriaLogService.registrarEventoSilencioso("INTENTO_FRAUDE_DEVOLUCION",
                    "El usuario intentó devolver un servicio (ID del Servicio: " + detalle.getIdItem() + "). Acción bloqueada.");
            throw new IllegalStateException("ALERTA DE SEGURIDAD: Los servicios no son retornables. Acción denegada.");
        }
        // ----------------------------------------------------------------
        Usuario cajero = usuarioService.buscarPorId(idCajero)
                .orElseThrow(() -> new IllegalArgumentException("Cajero no encontrado."));

        // 2. Blindaje Matemático: ¿Está intentando devolver más de lo que compró?
        BigDecimal cantidadRestante = detalle.getCantidad().subtract(detalle.getCantidadDevuelta());
        if (cantidadADevolver.compareTo(cantidadRestante) > 0) {
            throw new IllegalStateException("Intento de fraude bloqueado: Solo quedan " + cantidadRestante + " artículos disponibles para devolver en este ticket.");
        }

        // 3. Cálculos Financieros
        BigDecimal montoAReintegrar = detalle.getPrecioUnitario().multiply(cantidadADevolver);

        // 4. Crear la Bitácora (El ticket que irá a la Bandeja Roja)
        Devolucion devolucion = new Devolucion();
        devolucion.setDetalleVenta(detalle);
        devolucion.setUsuarioCajero(cajero);
        devolucion.setCantidad(cantidadADevolver);
        devolucion.setMontoReintegrado(montoAReintegrar);
        devolucion.setMotivo(motivo);
        devolucion.setEsMerma(esMerma);
        devolucion.setAuditada(false); // Nace pendiente de revisión por el Admin

        devolucionRepository.save(devolucion);

        // 5. Actualizar la memoria del Ticket original
        detalle.setCantidadDevuelta(detalle.getCantidadDevuelta().add(cantidadADevolver));
        detalleVentaRepository.save(detalle);

        Venta venta = detalle.getVenta();

        // 5.1 LÓGICA ESTRICTA: ¿Se devolvió todo el ticket o solo una parte?
        boolean esDevolucionTotal = true;

        // FIX SENIOR: Usamos el repositorio para traer los artículos de forma 100% segura
        java.util.List<DetalleVenta> todosLosDetalles = detalleVentaRepository.findByVenta(venta);

        // Recorremos todos los artículos del ticket
        for (DetalleVenta dv : todosLosDetalles) {
            // Comparamos usando compareTo para BigDecimal
            if (dv.getCantidad().compareTo(dv.getCantidadDevuelta()) > 0) {
                // Si encontramos aunque sea un artículo que no se ha devuelto por completo, es parcial
                esDevolucionTotal = false;
                break;
            }
        }

        // Asignamos el estado correcto
        if (esDevolucionTotal) {
            venta.setEstado(EstadoVenta.DEVUELTA_TOTAL);
        } else {
            venta.setEstado(EstadoVenta.DEVUELTA_PARCIAL);
        }

        ventaRepository.save(venta);

        auditoriaLogService.registrarEventoSilencioso("DEVOLUCION_CREADA", "Se registró devolución de $" + montoAReintegrar + " por motivo: " + motivo);

        // 6. Impacto en el Inventario (Solo aplica para PRODUCTOS FÍSICOS, no servicios)
        if (detalle.getTipoItem() == TipoItem.PRODUCTO && !esMerma) {
            Producto producto = productoService.buscarPorId(detalle.getIdItem())
                    .orElseThrow(() -> new IllegalStateException("Producto no encontrado en inventario."));

            // Regresamos el artículo sano a los estantes
            producto.setStock(producto.getStock() + cantidadADevolver.intValue());
            productoService.guardarProducto(producto);
        }

        return devolucion;
    }
}