package com.papeleria.pos.models;

import jakarta.persistence.*;
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
@Table(name = "devoluciones")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_venta", nullable = false)
    private DetalleVenta detalleVenta;

    // 1. QUIÉN LO HIZO EN EL MOSTRADOR (El Cajero)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cajero", nullable = false)
    private Usuario usuarioCajero;

    // 2. QUIÉN REVISÓ LA BANDEJA ROJA (El Admin - Es opcional al momento de crear)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_admin")
    private Usuario usuarioAdmin;

    @NotNull
    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @NotNull
    @Column(name = "monto_reintegrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoReintegrado;

    @CreationTimestamp
    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;

    @Column(length = 255)
    private String motivo;

    // ¿El producto regresó a los estantes o se fue a la basura?
    @NotNull
    @Column(name = "es_merma", nullable = false)
    private Boolean esMerma = false;

    // EL SEMÁFORO DE AUDITORÍA (Falso = Pendiente de revisión, True = Evidencia validada)
    @NotNull
    @Column(name = "auditada", nullable = false)
    private Boolean auditada = false;
}