package com.papeleria.pos.models;

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
@Table(name = "recetas_insumos")
public class RecetaInsumo {

    @EmbeddedId
    private RecetaInsumoId id = new RecetaInsumoId(); // Inicializamos para evitar NullPointerExceptions

    // FetchType.LAZY optimiza el rendimiento de la base de datos
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idInsumo")
    @JoinColumn(name = "id_insumo")
    private Insumo insumo;

    @NotNull(message = "La cantidad a descontar es obligatoria")
    @DecimalMin(value = "0.0001", message = "La cantidad a descontar debe ser mayor a cero")
    @Column(name = "cantidad_descontar", nullable = false, precision = 10, scale = 4)
    private BigDecimal cantidadDescontar;
}