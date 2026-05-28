package com.papeleria.pos.controllers;

import com.papeleria.pos.models.Producto;
import com.papeleria.pos.services.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor // Sustituye al @Autowired, creando un constructor seguro automáticamente
public class ProductoController {

    // Al poner 'final', obligamos a Spring a inyectarlo al inicio de forma segura
    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodosLosProductos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        Optional<Producto> producto = productoService.buscarPorId(id);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigoBarras}")
    public ResponseEntity<Producto> obtenerPorCodigoBarras(@PathVariable String codigoBarras) {
        Optional<Producto> producto = productoService.buscarPorCodigoBarras(codigoBarras);
        return producto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/alertas-stock")
    public ResponseEntity<List<Producto>> obtenerAlertasStock() {
        return ResponseEntity.ok(productoService.obtenerAlertasDeStock());
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@Valid @RequestBody Producto producto) {
        try {
            producto.setId(null);
            Producto nuevoProducto = productoService.guardarProducto(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id, @Valid @RequestBody Producto producto) {
        // Solución de Clean Code: Usamos isEmpty() en lugar de !isPresent()
        if (productoService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            producto.setId(id);
            Producto productoActualizado = productoService.guardarProducto(producto);
            return ResponseEntity.ok(productoActualizado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/abastecer")
    public ResponseEntity<?> abastecerInventario(@PathVariable Integer id, @RequestParam int cantidad) {
        if (cantidad <= 0) {
            return ResponseEntity.badRequest().body("La cantidad a abastecer debe ser mayor a 0");
        }
        try {
            productoService.agregarStock(id, cantidad);
            return ResponseEntity.ok().body("Stock actualizado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Integer id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> alternarEstado(@PathVariable Integer id) {
        try {
            productoService.alternarEstadoProducto(id);
            return ResponseEntity.ok().body("Estado del producto actualizado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PatchMapping("/{id}/merma")
    public ResponseEntity<?> registrarMerma(@PathVariable Integer id, @RequestParam int cantidad, @RequestParam String motivo) {
        try {
            productoService.registrarMerma(id, cantidad, motivo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}