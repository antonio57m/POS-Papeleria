package com.papeleria.pos.config;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class SeguridadInterceptor implements HandlerInterceptor {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Optional<Usuario> userOpt = usuarioService.buscarPorUsername(auth.getName());

            // 1. BLINDAJE: ¿El usuario fue borrado, desactivado o cambió de nombre?
            if (userOpt.isEmpty() || !userOpt.get().getActivo()) {
                return rechazarAcceso(request, response);
            }

            // 2. BLINDAJE: ¿Le cambiaron el rol mientras estaba operando?
            String rolSesion = auth.getAuthorities().iterator().next().getAuthority();
            String rolDb = "ROLE_" + userOpt.get().getRol().name();
            if (!rolSesion.equals(rolDb)) {
                return rechazarAcceso(request, response);
            }

            // 3. BLINDAJE: ¿Le cambiaron la contraseña de imprevisto?
            if (auth.getPrincipal() instanceof UserDetails) {
                String hashSesion = ((UserDetails) auth.getPrincipal()).getPassword();

                // FIX SENIOR: Aislamos la validación. Si es null, lo ignoramos de forma segura.
                if (hashSesion != null) {
                    if (!hashSesion.equals(userOpt.get().getPasswordHash())) {
                        return rechazarAcceso(request, response);
                    }
                }
            }
        }
        return true;
    }

    private boolean rechazarAcceso(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        // FIX SENIOR: Si es una petición de sistema (JSON/API), respondemos un 401 limpio
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Sesión inválida o desactivada\"}");
            return false;
        }

        response.sendRedirect("/login?error=true");
        return false;
    }
}