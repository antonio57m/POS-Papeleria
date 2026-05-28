package com.papeleria.pos.services;

import com.papeleria.pos.models.Producto;
import com.papeleria.pos.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    // 1. Agrega esta inyección al inicio de la clase
    @Autowired
    private DetalleVentaService detalleVentaService;
    @Autowired private AuditoriaLogService auditoriaLogService;

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Optional<Producto> buscarPorCodigoBarras(String codigoBarras) {
        return productoRepository.findByCodigoBarras(codigoBarras);
    }

    // Reemplaza tu método guardarProducto por este:
    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getStock() < 0) producto.setStock(0);
        if (producto.getPrecioVenta().compareTo(producto.getPrecioCompra()) < 0) throw new IllegalArgumentException("El precio de venta no puede ser menor al costo.");

        if (producto.getId() != null) {
            // Es una edición: Verificamos si cambió el precio o el stock manualmente
            Producto viejo = productoRepository.findById(producto.getId()).orElse(producto);
            if (viejo.getPrecioVenta().compareTo(producto.getPrecioVenta()) != 0) {
                auditoriaLogService.registrarEventoSilencioso("MODIFICACION_PRECIO", "Cambió precio de $" + viejo.getPrecioVenta() + " a $" + producto.getPrecioVenta() + " en: " + producto.getNombre());
            }
            if (!viejo.getStock().equals(producto.getStock())) {
                auditoriaLogService.registrarEventoSilencioso("AJUSTE_STOCK_MANUAL", "Stock alterado de " + viejo.getStock() + " a " + producto.getStock() + " en: " + producto.getNombre());
            }
        } else {
            if (productoRepository.findByCodigoBarras(producto.getCodigoBarras()).isPresent()) throw new IllegalStateException("Código duplicado.");
        }
        return productoRepository.save(producto);
    }

    // Método Transaccional: El corazón del Punto de Venta
    @Transactional
    public void restarStock(Integer idProducto, int cantidadVendida) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // --- NUEVA REGLA DE NEGOCIO: Bloquear productos inactivos ---
        if (producto.getActivo() != null && !producto.getActivo()) {
            throw new IllegalStateException("El producto '" + producto.getNombre() + "' está inactivo y no puede ser vendido.");
        }

        if (producto.getStock() < cantidadVendida) {
            throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
        }

        producto.setStock(producto.getStock() - cantidadVendida);
        productoRepository.save(producto);
    }

    // Método Transaccional: Para el módulo de compras/proveedores
    @Transactional
    public void agregarStock(Integer idProducto, int cantidadRecibida) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        producto.setStock(producto.getStock() + cantidadRecibida);
        auditoriaLogService.registrarEventoSilencioso("ABASTECIMIENTO_STOCK",
                "Se agregaron " + cantidadRecibida + " unidades al producto: " + producto.getNombre());
        productoRepository.save(producto);
    }

    // 2. Modifica tu método eliminarProducto
    @Transactional
    public void eliminarProducto(Integer id) {
        // Usamos el ENUM directamente importando com.papeleria.pos.enums.TipoItem;
        if (detalleVentaService.itemTieneHistorialDeVentas(com.papeleria.pos.enums.TipoItem.PRODUCTO, id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque ya existe en el historial de ventas. Se sugiere desactivarlo.");
        }

        // Buscamos cómo se llamaba el producto ANTES de destruirlo
        Producto productoABorrar = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        productoRepository.deleteById(id);

        // --- ESPÍA SILENCIOSO: ELIMINACIÓN DE CATÁLOGO ---
        auditoriaLogService.registrarEventoSilencioso("PRODUCTO_ELIMINADO", "Se eliminó definitivamente el producto: " + productoABorrar.getNombre());
    }

    public List<Producto> obtenerAlertasDeStock() {
        return productoRepository.findProductosConStockCritico();
    }

    @Transactional
    public void alternarEstadoProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Si es null por algún registro viejo, asumimos que era true
        boolean estadoActual = producto.getActivo() != null ? producto.getActivo() : true;
        producto.setActivo(!estadoActual);
        auditoriaLogService.registrarEventoSilencioso("ITEM_DESACTIVADO", "Estado cambiado a " + !estadoActual + " en: " + producto.getNombre());
        productoRepository.save(producto);
    }

    @Transactional
    public void registrarMerma(Integer idProducto, int cantidad, String motivo) {
        if(cantidad <= 0) throw new IllegalArgumentException("La cantidad de merma debe ser mayor a 0");

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para reportar esta merma.");
        }

        producto.setStock(producto.getStock() - cantidad);

        // ESPIONAJE SILENCIOSO
        auditoriaLogService.registrarEventoSilencioso("AJUSTE_MERMA",
                "Merma de " + cantidad + " unidades en '" + producto.getNombre() + "'. Motivo: " + motivo);

        productoRepository.save(producto);
    }
}