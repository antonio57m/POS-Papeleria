package com.papeleria.pos.services;

import com.papeleria.pos.enums.MetodoPago;
import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.*;
import com.papeleria.pos.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private RecetaInsumoService recetaInsumoService;

    @Autowired
    private InsumoService insumoService;

    // INYECTAMOS EL SERVICIO PARA PODER VALIDAR SU ESTADO
    @Autowired
    private ServicioService servicioService;

    // 1. Consultas para el Dashboard Financiero y Conciliación
    public List<Venta> obtenerVentasPorRango(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaHoraBetween(inicio, fin);
    }

    public List<Venta> obtenerVentasPorMetodoPago(MetodoPago metodoPago, LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByMetodoPagoAndFechaHoraBetween(metodoPago, inicio, fin);
    }

    // 2. El motor de auditoría del CorteCajaService
    public BigDecimal calcularEfectivoEnCaja(Integer idUsuario, LocalDateTime inicioTurno, LocalDateTime finTurno) {
        return ventaRepository.calcularTotalEfectivoPorCajero(idUsuario, inicioTurno, finTurno);
    }

    // 3. Búsqueda optimizada (N+1 resuelto) para reimpresión de tickets
    public Optional<Venta> buscarVentaConUsuario(Integer idVenta) {
        return ventaRepository.findVentaConUsuario(idVenta);
    }

    // 4. Guardado del encabezado de la venta
    @Transactional
    public Venta guardarVentaCabecera(Venta venta) {
        // Validación de seguridad financiera
        if (venta.getTotal() == null || venta.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El total de la venta no puede ser negativo o nulo.");
        }
        return ventaRepository.save(venta);
    }

    // EL MOTOR PRINCIPAL DEL PUNTO DE VENTA
// EL MOTOR PRINCIPAL DEL PUNTO DE VENTA
    @Transactional
    public Venta procesarVentaCompleta(Venta cabecera, List<DetalleVenta> detalles) {

        // 1. Guardamos la cabecera del ticket primero para generar el ID de Venta
        Venta ventaGuardada = guardarVentaCabecera(cabecera);

        // 2. Procesamos cada línea del ticket
        for (DetalleVenta detalle : detalles) {
            // Vinculamos el detalle a la venta recién creada
            detalle.setVenta(ventaGuardada);

            // 3. Descontamos el inventario según el tipo de ítem
            if (detalle.getTipoItem() == TipoItem.PRODUCTO) {

                // --- BLINDAJE: VALIDACIÓN DE PRODUCTO ACTIVO ---
                Producto producto = productoService.buscarPorId(detalle.getIdItem())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la base de datos."));

                if (Boolean.FALSE.equals(producto.getActivo())) {
                    throw new IllegalStateException("Operación rechazada: El producto '" + producto.getNombre() + "' fue desactivado y no puede ser vendido.");
                }

                productoService.restarStock(detalle.getIdItem(), detalle.getCantidad().intValue());

            } else if (detalle.getTipoItem() == TipoItem.SERVICIO) {

                // --- BLINDAJE: VALIDACIÓN DE SERVICIO ACTIVO ---
                Servicio servicio = servicioService.buscarPorId(detalle.getIdItem())
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado en la base de datos."));

                if (Boolean.FALSE.equals(servicio.getActivo())) {
                    throw new IllegalStateException("Operación rechazada: El servicio '" + servicio.getNombre() + "' fue desactivado y no puede ser cobrado.");
                }

                // Buscamos la receta del servicio
                List<RecetaInsumo> receta = recetaInsumoService.obtenerInsumosDeServicio(detalle.getIdItem());

                for (RecetaInsumo ingrediente : receta) {

                    // --- NUEVO BLINDAJE: VALIDACIÓN DE INSUMOS ACTIVOS ---
                    Insumo insumoFisico = ingrediente.getInsumo();
                    if (Boolean.FALSE.equals(insumoFisico.getActivo())) {
                        throw new IllegalStateException("Operación rechazada: El servicio '" + servicio.getNombre() +
                                "' no se puede realizar porque requiere el insumo '" + insumoFisico.getNombre() + "', el cual está desactivado en el almacén.");
                    }

                    // Multiplicamos el gasto base por la cantidad vendida
                    BigDecimal consumoTotal = ingrediente.getCantidadDescontar().multiply(detalle.getCantidad());
                    insumoService.descontarStock(insumoFisico.getId(), consumoTotal);
                }
            }
        }

        // 4. Guardamos todas las líneas del ticket en la base de datos de un solo golpe
        detalleVentaService.guardarDetalles(detalles);

        return ventaGuardada;
    }
}