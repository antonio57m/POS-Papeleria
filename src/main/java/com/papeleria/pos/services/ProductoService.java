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

    // Método Senior: Guardado con validaciones de negocio estrictas
    @Transactional
    public Producto guardarProducto(Producto producto) {
        // 1. Evitar inventario negativo
        if (producto.getStock() < 0) {
            producto.setStock(0);
        }

        // 2. Protección financiera: El margen de ganancia no puede ser negativo
        if (producto.getPrecioVenta().compareTo(producto.getPrecioCompra()) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser menor al costo.");
        }

        // 3. Protección de duplicidad (Solo si es un producto nuevo)
        if (producto.getId() == null) {
            Optional<Producto> existente = productoRepository.findByCodigoBarras(producto.getCodigoBarras());
            if (existente.isPresent()) {
                throw new IllegalStateException("El código de barras ya está registrado en otro producto.");
            }
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
        productoRepository.save(producto);
    }

    // 2. Modifica tu método eliminarProducto
    public void eliminarProducto(Integer id) {
        // Usamos el ENUM directamente importando com.papeleria.pos.enums.TipoItem;
        if (detalleVentaService.itemTieneHistorialDeVentas(com.papeleria.pos.enums.TipoItem.PRODUCTO, id)) {
            throw new IllegalStateException("No se puede eliminar el producto porque ya existe en el historial de ventas. Se sugiere desactivarlo.");
        }
        productoRepository.deleteById(id);
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

        productoRepository.save(producto);
    }
}