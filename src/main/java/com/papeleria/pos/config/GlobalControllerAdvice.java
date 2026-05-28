package com.papeleria.pos.config;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute("usuarioLogueado")
    public Usuario addUsuarioLogueado(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            // Buscamos al usuario
            return usuarioService.buscarPorUsername(authentication.getName()).orElse(null);
        }
        return null; // Si no hay autenticación, retorna null
    }
}