package com.papeleria.pos.services;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyectamos el encriptador de contraseñas de Spring Security
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivo(true);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        // Regla 1: Validar duplicidad en la base de datos (Solo si es un registro nuevo)
        if (usuario.getId() == null && usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + usuario.getUsername() + "' ya está en uso.");
        }

        // Regla 2: Seguridad y Hashing de contraseñas
        // Solo encriptamos si es un usuario nuevo, o si la contraseña no está ya encriptada (BCrypt empieza con $2a$)
        if (usuario.getId() == null || (usuario.getPasswordHash() != null && !usuario.getPasswordHash().startsWith("$2a$"))) {
            String hashSeguro = passwordEncoder.encode(usuario.getPasswordHash());
            usuario.setPasswordHash(hashSeguro);
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void darDeBajaUsuario(Integer id) {
        // Regla 3: Borrado Lógico en lugar de Borrado Físico
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario); // Solo actualizamos su estado
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    // --- MOTOR DE AUTORIZACIÓN PARA DEVOLUCIONES ---
    public Optional<Usuario> autorizarAdministrador(String username, String passwordPlana) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

        if (usuarioOpt.isPresent()) {
            Usuario admin = usuarioOpt.get();

            // 1. Validar que el usuario esté activo y que REALMENTE tenga rol de ADMIN
            if (Boolean.TRUE.equals(admin.getActivo()) && admin.getRol().name().equals("ADMIN")) {

                // 2. Comparar la contraseña tecleada con el Hash encriptado de la base de datos
                if (passwordEncoder.matches(passwordPlana, admin.getPasswordHash())) {
                    return Optional.of(admin); // Autorización exitosa
                }
            }
        }
        // Si no existe, no es admin, o la contraseña es incorrecta, devolvemos vacío
        return Optional.empty();
    }
}