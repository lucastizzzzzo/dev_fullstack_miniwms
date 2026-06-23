package com.example.wms.security;

import com.example.wms.model.Usuario;
import com.example.wms.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByEmail(username)
            .orElseGet(() -> usuarioRepository.findAll().stream()
                .filter(x -> username.equalsIgnoreCase(x.getNome()))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado")));

        String role = (u.getPerfil() != null) ? "ROLE_" + u.getPerfil().toUpperCase() : "ROLE_USER";
        GrantedAuthority auth = new SimpleGrantedAuthority(role);

        return User.builder()
            .username(u.getEmail())
            .password(u.getSenhaHash()!=null?u.getSenhaHash():"")
            .authorities(Collections.singletonList(auth))
            .disabled(!Boolean.TRUE.equals(u.getAtivo()))
            .build();
    }
}
