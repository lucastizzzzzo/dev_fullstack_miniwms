package com.example.wms.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {
    private final JdbcTemplate jdbc;

    public AuditorAwareImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }
        String principalName = auth.getName();
        try {
            String sql = "select nome from usuario where email = ?";
            String nome = jdbc.queryForObject(sql, new Object[]{principalName}, String.class);
            if (nome != null && !nome.isBlank()) return Optional.of(nome);
        } catch (Exception ignored) {
            // fallback to principalName
        }
        return Optional.ofNullable(principalName);
    }
}
