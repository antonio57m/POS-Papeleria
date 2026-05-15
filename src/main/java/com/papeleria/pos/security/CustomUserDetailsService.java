package com.papeleria.pos.security;

import com.papeleria.pos.models.Usuario;
import com.papeleria.pos.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos al usuario en nuestra BD
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Regla de Negocio: Si el usuario fue dado de baja (borrado lógico), no lo dejamos pasar
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("El usuario está inactivo.");
        }

        // Traducimos nuestro Usuario al UserDetails de Spring Security
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name()) // Spring le agregará el prefijo "ROLE_" automáticamente
                .build();
    }
}