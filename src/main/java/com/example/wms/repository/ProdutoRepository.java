package com.example.wms.repository;

import com.example.wms.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
	boolean existsByUnidadeMedida(String unidadeMedida);

	java.util.Optional<com.example.wms.model.Produto> findBySku(String sku);
}
