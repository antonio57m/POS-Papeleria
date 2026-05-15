package com.papeleria.pos.enums;

public enum EstadoVenta {
    COMPLETADA,       // Venta normal
    DEVUELTA_PARCIAL, // Se regresaron algunos artículos
    DEVUELTA_TOTAL    // Se regresó todo el ticket y se anuló el ingreso
}