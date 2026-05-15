package com.papeleria.pos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Obligatorio para llaves compuestas en JPA
@Embeddable
public class RecetaInsumoId implements Serializable {

    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "id_insumo")
    private Integer idInsumo;
}