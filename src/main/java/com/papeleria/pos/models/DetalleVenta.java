package com.papeleria.pos.models;

import com.papeleria.pos.enums.TipoItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "detalle_ventas")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FetchType.LAZY es CRÍTICO aquí para no saturar la memoria
    @NotNull(message = "El detalle debe estar asociado a una venta")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @NotNull(message = "El tipo de ítem es obligatorio (PRODUCTO o SERVICIO)")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false)
    private TipoItem tipoItem;

    @NotNull(message = "El ID del ítem referenciado no puede ser nulo")
    @Column(name = "id_item", nullable = false)
    private Integer idItem;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    @NotNull
    @Column(name = "cantidad_devuelta", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadDevuelta = BigDecimal.ZERO; // Inicia en cero
}