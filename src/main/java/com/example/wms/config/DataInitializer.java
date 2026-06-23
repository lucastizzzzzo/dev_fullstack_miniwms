package com.example.wms.config;

import com.example.wms.model.Usuario;
import com.example.wms.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner ensureAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Usuario u = new Usuario();
                u.setNome("Admin");
                u.setEmail("admin@example.com");
                u.setPerfil("ADMIN");
                u.setAtivo(true);
                u.setSenhaHash(passwordEncoder.encode("admin"));
                usuarioRepository.save(u);
            } else {
                // if admin exists but password not encoded, ensure it's encoded
                usuarioRepository.findAll().stream()
                        .filter(us -> "admin@example.com".equalsIgnoreCase(us.getEmail()))
                        .findFirst()
                        .ifPresent(admin -> {
                            String pass = admin.getSenhaHash();
                            if (pass == null || !pass.startsWith("$2")) {
                                admin.setSenhaHash(passwordEncoder.encode("admin"));
                                usuarioRepository.save(admin);
                            }
                        });
            }
        };
    }
}
