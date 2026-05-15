package com.papeleria.pos.services;

import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.CorteCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CorteCajaService {

    @Autowired
    private CorteCajaRepository corteCajaRepository;

    // 1. Apertura de Turno (Al iniciar el día o cambio de turno)
    @Transactional
    public CorteCaja abrirCaja(Usuario cajero) {
        // Regla de Negocio: Un cajero no puede abrir dos cajas al mismo tiempo.
        Optional<CorteCaja> cajaAbierta = corteCajaRepository.findCajaAbiertaPorUsuario(cajero);
        if (cajaAbierta.isPresent()) {
            throw new IllegalStateException("El usuario " + cajero.getUsername() + " ya tiene un turno abierto.");
        }

        CorteCaja nuevoCorte = new CorteCaja();
        nuevoCorte.setUsuario(cajero);
        // fechaApertura se genera automáticamente en MariaDB/Java por @CreationTimestamp

        return corteCajaRepository.save(nuevoCorte);
    }

    // 2. El Corte a Ciegas (Al finalizar el turno)
    @Transactional
    public CorteCaja cerrarCaja(Integer idCorte, BigDecimal montoDeclarado, BigDecimal montoEsperado) {
        CorteCaja corte = corteCajaRepository.findById(idCorte)
                .orElseThrow(() -> new IllegalArgumentException("Corte de caja no encontrado."));

        // Regla de Negocio: No se puede cerrar una caja dos veces.
        if (corte.getFechaCierre() != null) {
            throw new IllegalStateException("Esta caja ya fue cerrada anteriormente.");
        }

        // Matemáticas del corte (Declarado - Esperado = Diferencia)
        // Ejemplo Faltante: 500 declarados - 550 esperados = -50 de diferencia
        // Ejemplo Sobrante: 600 declarados - 550 esperados = +50 de diferencia
        BigDecimal diferencia = montoDeclarado.subtract(montoEsperado);

        corte.setMontoEsperado(montoEsperado);
        corte.setMontoDeclarado(montoDeclarado);
        corte.setDiferencia(diferencia);
        corte.setFechaCierre(LocalDateTime.now());

        return corteCajaRepository.save(corte);
    }

    // 3. Consultas para el Dashboard del Administrador
    public Optional<CorteCaja> obtenerCajaActiva(Usuario cajero) {
        return corteCajaRepository.findCajaAbiertaPorUsuario(cajero);
    }

    public List<CorteCaja> obtenerCortesConDiscrepancia() {
        return corteCajaRepository.findCortesConDiscrepancia();
    }

    public List<CorteCaja> buscarTurnosZombies(LocalDateTime fechaLimite) {
        return corteCajaRepository.findCortesOlvidados(fechaLimite);
    }
}