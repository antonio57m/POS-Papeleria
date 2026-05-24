package com.papeleria.pos.repositories;

import com.papeleria.pos.models.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
    Optional<Configuracion> findByClave(String clave);
}