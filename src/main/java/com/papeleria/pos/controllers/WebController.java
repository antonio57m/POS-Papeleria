package com.papeleria.pos.controllers;

import com.papeleria.pos.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        // NUEVO: Le decimos a la vista que estamos en el dashboard
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/catalogo/productos")
    public String mostrarCatalogoProductos(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        // NUEVO: Le decimos a la vista que estamos en el catálogo
        model.addAttribute("activePage", "catalogo");
        return "catalogo-productos";
    }

    @GetMapping("/punto-venta")
    public String mostrarPuntoVenta(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        // NUEVO: Le decimos a la vista que estamos en el punto de venta
        model.addAttribute("activePage", "punto-venta");
        return "punto-venta";
    }
    @GetMapping("/corte-caja")
    public String mostrarCorteCaja(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        model.addAttribute("activePage", "corte-caja");
        return "corte-caja";
    }
    @GetMapping("/catalogo/servicios")
    public String mostrarCatalogoServicios(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        model.addAttribute("activePage", "catalogo-servicios");
        return "catalogo-servicios";
    }
    @GetMapping("/catalogo/insumos")
    public String mostrarCatalogoInsumos(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        model.addAttribute("activePage", "catalogo-insumos");
        return "catalogo-insumos";
    }
    @GetMapping("/devoluciones")
    public String mostrarDevoluciones(Model model, Principal principal) {
        if (principal != null) {
            usuarioService.buscarPorUsername(principal.getName())
                    .ifPresent(usuario -> model.addAttribute("usuarioLogueado", usuario));
        }
        model.addAttribute("activePage", "devoluciones");
        return "devoluciones";
    }
}