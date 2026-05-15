package com.papeleria.pos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_barras", unique = true, length = 100)
    private String codigoBarras;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "El precio de venta es obligatorio")
    @Min(value = 0, message = "El precio de venta no puede ser negativo")
    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Min(value = 0, message = "El precio de compra no puede ser negativo")
    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer stock = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", columnDefinition = "INT DEFAULT 5")
    private Integer stockMinimo = 5;

    // --- NUEVO CAMPO PARA BORRADO LÓGICO ---
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;
}