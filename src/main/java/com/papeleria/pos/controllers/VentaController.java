package com.papeleria.pos.controllers;

import com.papeleria.pos.enums.MetodoPago;
import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.DetalleVenta;
import com.papeleria.pos.models.Venta;
import com.papeleria.pos.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final DetalleVentaService detalleVentaService;
    private final UsuarioService usuarioService;
    private final CorteCajaService corteCajaService;

    // Inyectamos esto para formatear la respuesta del ticket
    private final ProductoService productoService;
    private final ServicioService servicioService;

    public record ItemVentaDTO(TipoItem tipoItem, Integer idItem, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
    public record NuevaVentaRequest(Integer idUsuario, BigDecimal total, MetodoPago metodoPago, List<ItemVentaDTO> detalles) {}

    @PostMapping
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody NuevaVentaRequest request) {
        return usuarioService.buscarPorId(request.idUsuario())
                .map(cajero -> {
                    if (corteCajaService.obtenerCajaActiva(cajero).isEmpty()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Operación rechazada: Debes abrir un turno de caja antes de procesar ventas.");
                    }

                    try {
                        Venta cabecera = new Venta();
                        cabecera.setUsuario(cajero);
                        cabecera.setTotal(request.total());
                        cabecera.setMetodoPago(request.metodoPago());

                        List<DetalleVenta> detalles = request.detalles().stream().map(dto -> {
                            DetalleVenta detalle = new DetalleVenta();
                            detalle.setTipoItem(dto.tipoItem());
                            detalle.setIdItem(dto.idItem());
                            detalle.setCantidad(dto.cantidad());
                            detalle.setPrecioUnitario(dto.precioUnitario());
                            detalle.setSubtotal(dto.subtotal());
                            return detalle;
                        }).collect(Collectors.toList());

                        Venta ventaProcesada = ventaService.procesarVentaCompleta(cabecera, detalles);
                        return new ResponseEntity<>(ventaProcesada, HttpStatus.CREATED);

                    } catch (IllegalArgumentException | IllegalStateException e) {
                        return ResponseEntity.badRequest().body(e.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.badRequest().body("El usuario cajero especificado no existe."));
    }

    // --- RUTA ÚNICA PARA OBTENER TICKET (Resuelve duplicidad y tipado) ---
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerTicketCompleto(@PathVariable Integer id) {
        return ventaService.buscarVentaConUsuario(id)
                .map(venta -> {
                    // CORRECCIÓN 1: Pasamos el objeto 'venta' entero, no el 'id'
                    List<DetalleVenta> detalles = detalleVentaService.obtenerDetallesPorVenta(venta);

                    List<Map<String, Object>> detallesDTO = detalles.stream().map(d -> {
                        String nombreItem = "Desconocido";
                        if (d.getTipoItem() == TipoItem.PRODUCTO) {
                            nombreItem = productoService.buscarPorId(d.getIdItem()).map(p -> p.getNombre()).orElse("Prod. Eliminado");
                        } else {
                            nombreItem = servicioService.buscarPorId(d.getIdItem()).map(s -> s.getNombre()).orElse("Serv. Eliminado");
                        }

                        // CORRECCIÓN 2: Usamos HashMap clásico para evitar el error de tipado estricto
                        Map<String, Object> map = new HashMap<>();
                        map.put("idDetalle", d.getId());
                        map.put("tipo", d.getTipoItem().name());
                        map.put("nombre", nombreItem);
                        map.put("cantidadOriginal", d.getCantidad());
                        map.put("cantidadDevueltaHistorial", d.getCantidadDevuelta());
                        map.put("precioUnitario", d.getPrecioUnitario());
                        map.put("subtotal", d.getSubtotal());

                        return map;
                    }).toList();

                    Map<String, Object> respuestaFinal = new HashMap<>();
                    respuestaFinal.put("idTicket", venta.getId());
                    respuestaFinal.put("fecha", venta.getFechaHora());
                    respuestaFinal.put("cajero", venta.getUsuario().getUsername());
                    respuestaFinal.put("total", venta.getTotal());
                    respuestaFinal.put("estado", venta.getEstado().name());
                    respuestaFinal.put("detalles", detallesDTO);

                    return ResponseEntity.ok(respuestaFinal);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/reporte/fechas")
    public ResponseEntity<List<Venta>> obtenerVentasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.obtenerVentasPorRango(inicio, fin));
    }

    @GetMapping("/reporte/metodo-pago")
    public ResponseEntity<List<Venta>> obtenerVentasPorMetodoPago(
            @RequestParam MetodoPago metodoPago,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ventaService.obtenerVentasPorMetodoPago(metodoPago, inicio, fin));
    }

    @GetMapping("/analitica/top-productos")
    public ResponseEntity<List<Object[]>> obtenerTopProductos() {
        return ResponseEntity.ok(detalleVentaService.obtenerProductosMasVendidos());
    }

    @GetMapping("/analitica/top-servicios")
    public ResponseEntity<List<Object[]>> obtenerTopServicios() {
        return ResponseEntity.ok(detalleVentaService.obtenerServiciosMasRealizados());
    }
}