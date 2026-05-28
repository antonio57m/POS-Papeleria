package com.papeleria.pos.services;

import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.CorteCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CorteCajaService {

    @Autowired
    private CorteCajaRepository corteCajaRepository;

    // INYECTAMOS EL SERVICIO DE CORREOS
    @Autowired
    private ReporteEmailService emailService;

    @Autowired
    private ConfiguracionService configuracionService;

    @Autowired private AuditoriaLogService auditoriaLogService;

    // 1. Apertura de Turno (Al iniciar el día o cambio de turno)
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

        CorteCaja corteGuardado = corteCajaRepository.save(nuevoCorte);

        // --- ESPÍA SILENCIOSO: APERTURA DE CAJA ---
        auditoriaLogService.registrarEventoSilencioso("APERTURA_CAJA", "El cajero abrió un nuevo turno de caja.");

        return corteGuardado;
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

        // Matemáticas del corte
        BigDecimal diferencia = montoDeclarado.subtract(montoEsperado);

        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            auditoriaLogService.registrarEventoSilencioso("CIERRE_CAJA_DESCUADRE", "El cajero cerró con una diferencia de: $" + diferencia);
        }

        corte.setMontoEsperado(montoEsperado);
        corte.setMontoDeclarado(montoDeclarado);
        corte.setDiferencia(diferencia);
        corte.setFechaCierre(LocalDateTime.now());

        CorteCaja corteGuardado = corteCajaRepository.save(corte);

        // --- EL CEREBRO DE LAS NOTIFICACIONES ---
        String correosGuardados = configuracionService.obtenerCorreosReporte();

        if (!correosGuardados.isEmpty()) {
            String[] destinatarios = correosGuardados.split(",");

            // TRUCO SENIOR: Forzamos la inicialización del nombre del usuario mientras la conexión
            // a la base de datos sigue abierta, para evitar el LazyInitializationException en los correos.
            corteGuardado.getUsuario().getUsername();

            // 1. ENVÍO DIARIO (Se dispara SIEMPRE que se cierra una caja e incluye el Stock)
            emailService.enviarReporteDiarioHtml(destinatarios, corteGuardado);

            // 2. ENVÍO SEMANAL DINÁMICO
            String diaConfigurado = configuracionService.obtenerDiaReporteSemanal(); // Ej. "FRIDAY"
            DayOfWeek diaActual = corteGuardado.getFechaCierre().getDayOfWeek();

            // Si hoy es el día que el cliente eligió en la configuración...
            if (diaActual.name().equalsIgnoreCase(diaConfigurado)) {

                // Calculamos desde hace 6 días a las 00:00:00 hasta este preciso momento
                LocalDateTime finSemana = corteGuardado.getFechaCierre();
                LocalDateTime inicioSemana = finSemana.minusDays(6).with(LocalTime.MIN);

                // Disparamos el reporte pesado
                emailService.enviarReporteSemanalHtml(destinatarios, inicioSemana, finSemana);
            }
        }

        return corteGuardado;
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