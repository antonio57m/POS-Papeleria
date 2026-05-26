package com.papeleria.pos.services;

import com.papeleria.pos.models.Configuracion;
import com.papeleria.pos.repositories.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public String obtenerCorreosReporte() {
        return configuracionRepository.findByClave("CORREOS_REPORTE")
                .map(Configuracion::getValor)
                .orElse(""); // Si no hay correos, regresa vacío
    }

    // NUEVO: Obtenemos el día configurado. Por defecto será FRIDAY (Viernes)
    public String obtenerDiaReporteSemanal() {
        return configuracionRepository.findByClave("DIA_REPORTE_SEMANAL")
                .map(Configuracion::getValor)
                .orElse("FRIDAY");
    }

    @Transactional
    public void guardarConfiguracionReportes(String correos, String diaSemana) {
        // 1. Guardar Correos
        Configuracion configCorreos = configuracionRepository.findByClave("CORREOS_REPORTE")
                .orElse(new Configuracion(null, "CORREOS_REPORTE", ""));
        configCorreos.setValor(correos.replaceAll("\\s+", ""));
        configuracionRepository.save(configCorreos);

        // 2. Guardar Día de la Semana
        Configuracion configDia = configuracionRepository.findByClave("DIA_REPORTE_SEMANAL")
                .orElse(new Configuracion(null, "DIA_REPORTE_SEMANAL", "FRIDAY"));
        configDia.setValor(diaSemana);
        configuracionRepository.save(configDia);
    }
}