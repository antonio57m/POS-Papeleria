package com.papeleria.pos.services;

import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.CorteCajaRepository;
import com.papeleria.pos.repositories.DevolucionRepository;
import com.papeleria.pos.repositories.VentaRepository;
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

    @Autowired
    private ReporteEmailService emailService;

    @Autowired
    private ConfiguracionService configuracionService;

    @Autowired
    private AuditoriaLogService auditoriaLogService;

    @Autowired
    private DevolucionRepository devolucionRepository;

    // INYECTAMOS EL REPOSITORIO DE VENTAS PARA HACER LA MATEMÁTICA INTERNA
    @Autowired
    private VentaRepository ventaRepository;

    // 1. Apertura de Turno (Al iniciar el día o cambio de turno)
    @Transactional
    public CorteCaja abrirCaja(Usuario cajero) {
        Optional<CorteCaja> cajaAbierta = corteCajaRepository.findCajaAbiertaPorUsuario(cajero);
        if (cajaAbierta.isPresent()) {
            throw new IllegalStateException("El usuario " + cajero.getUsername() + " ya tiene un turno abierto.");
        }

        CorteCaja nuevoCorte = new CorteCaja();
        nuevoCorte.setUsuario(cajero);

        CorteCaja corteGuardado = corteCajaRepository.save(nuevoCorte);

        auditoriaLogService.registrarEventoSilencioso("APERTURA_CAJA", "El cajero abrió un nuevo turno de caja.");

        return corteGuardado;
    }

    // 2. El Corte a Ciegas (Al finalizar el turno)
    @Transactional
    public CorteCaja cerrarCaja(Integer idCorte, BigDecimal montoDeclarado, BigDecimal montoEsperadoFrontend) {
        CorteCaja corte = corteCajaRepository.findById(idCorte)
                .orElseThrow(() -> new IllegalArgumentException("Corte de caja no encontrado."));

        if (corte.getFechaCierre() != null) {
            throw new IllegalStateException("Esta caja ya fue cerrada anteriormente.");
        }

        // --- LÓGICA FINANCIERA BLINDADA (NUNCA CONFIAR EN EL FRONTEND) ---

        // 1. Sumamos todo el EFECTIVO que ingresó en este turno
        BigDecimal ventasEfectivo = ventaRepository.sumarVentasEfectivoPorTurno(corte.getUsuario().getId(), corte.getFechaApertura());
        if (ventasEfectivo == null) ventasEfectivo = BigDecimal.ZERO;

        // 2. Sumamos todo el EFECTIVO que salió por devoluciones en este turno
        BigDecimal efectivoDevuelto = devolucionRepository.sumarEfectivoDevueltoPorTurno(corte.getUsuario().getId(), corte.getFechaApertura());
        if (efectivoDevuelto == null) efectivoDevuelto = BigDecimal.ZERO;

        // 3. Calculamos el Monto Esperado Real (Ingresos - Egresos)
        BigDecimal montoEsperadoReal = ventasEfectivo.subtract(efectivoDevuelto);

        // 4. Matemáticas del corte con el valor REAL, ignorando lo que haya mandado el frontend
        BigDecimal diferencia = montoDeclarado.subtract(montoEsperadoReal);

        // Espía para descuadres (Alerta Roja)
        if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
            auditoriaLogService.registrarEventoSilencioso("CIERRE_CAJA_DESCUADRE", "El cajero cerró con una diferencia de: $" + diferencia);
        }

        corte.setMontoEsperado(montoEsperadoReal);
        corte.setMontoDeclarado(montoDeclarado);
        corte.setDiferencia(diferencia);
        corte.setFechaCierre(LocalDateTime.now());

        CorteCaja corteGuardado = corteCajaRepository.save(corte);

        // --- ESPÍA SILENCIOSO: CIERRE DE CAJA EXITOSO ---
        auditoriaLogService.registrarEventoSilencioso("CIERRE_CAJA",
                "Cierre de turno completado. Monto declarado: $" + montoDeclarado + " | Monto esperado: $" + montoEsperadoReal);

        // --- EL CEREBRO DE LAS NOTIFICACIONES ---
        String correosGuardados = configuracionService.obtenerCorreosReporte();

        if (!correosGuardados.isEmpty()) {
            try {
                String[] destinatarios = correosGuardados.split(",");
                corteGuardado.getUsuario().getUsername();
                emailService.enviarReporteDiarioHtml(destinatarios, corteGuardado);

                String diaConfigurado = configuracionService.obtenerDiaReporteSemanal();
                DayOfWeek diaActual = corteGuardado.getFechaCierre().getDayOfWeek();

                if (diaActual.name().equalsIgnoreCase(diaConfigurado)) {
                    LocalDateTime finSemana = corteGuardado.getFechaCierre();
                    LocalDateTime inicioSemana = finSemana.minusDays(6).with(LocalTime.MIN);
                    emailService.enviarReporteSemanalHtml(destinatarios, inicioSemana, finSemana);
                }
            } catch (Exception e) {
                System.err.println("La caja se cerró localmente, pero falló el envío de correos: " + e.getMessage());
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