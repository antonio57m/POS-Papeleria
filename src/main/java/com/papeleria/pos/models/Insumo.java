package com.papeleria.pos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del insumo no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La unidad de medida es obligatoria (ej. ml, hojas, gramos)")
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @NotNull(message = "La cantidad actual es obligatoria")
    @DecimalMin(value = "0.0", message = "La cantidad de insumo no puede ser negativa")
    @Column(name = "cantidad_actual", precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0")
    private BigDecimal cantidadActual = BigDecimal.ZERO;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;
}