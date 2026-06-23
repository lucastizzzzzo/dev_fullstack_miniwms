package com.example.wms.repository;

import com.example.wms.model.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeRepository extends JpaRepository<UnidadeMedida, Long> {
}
