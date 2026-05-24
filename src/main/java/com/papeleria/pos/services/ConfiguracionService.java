package com.papeleria.pos.services;

import com.papeleria.pos.models.Configuracion;
import com.papeleria.pos.repositories.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public String obtenerCorreosReporte() {
        return configuracionRepository.findByClave("CORREOS_REPORTE")
                .map(Configuracion::getValor)
                .orElse(""); // Si no hay correos, regresa vacío
    }

    public void guardarCorreosReporte(String correos) {
        Configuracion config = configuracionRepository.findByClave("CORREOS_REPORTE")
                .orElse(new Configuracion(null, "CORREOS_REPORTE", ""));

        // Limpiamos los espacios en blanco extra que el usuario pueda meter por error
        config.setValor(correos.replaceAll("\\s+", ""));
        configuracionRepository.save(config);
    }
}