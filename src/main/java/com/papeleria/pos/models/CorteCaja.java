package com.papeleria.pos.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cortes_caja")
public class CorteCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Genera la fecha exacta desde Java al momento de hacer .save()
    @CreationTimestamp
    @Column(name = "fecha_apertura", updatable = false)
    private LocalDateTime fechaApertura;

    // Se queda sin anotaciones extra porque se llenará hasta el final del turno
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @NotNull(message = "El usuario (cajero) responsable del turno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @DecimalMin(value = "0.0", message = "El monto esperado no puede ser negativo")
    @Column(name = "monto_esperado", precision = 10, scale = 2)
    private BigDecimal montoEsperado;

    @DecimalMin(value = "0.0", message = "El monto declarado no puede ser negativo")
    @Column(name = "monto_declarado", precision = 10, scale = 2)
    private BigDecimal montoDeclarado;

    // OJO: Aquí NO ponemos @DecimalMin(0) porque la diferencia SÍ puede ser negativa (faltante de caja)
    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;
}