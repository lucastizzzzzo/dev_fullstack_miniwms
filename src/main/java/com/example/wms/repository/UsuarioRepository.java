package com.example.wms.repository;

import com.example.wms.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	java.util.Optional<Usuario> findByEmail(String email);
}
