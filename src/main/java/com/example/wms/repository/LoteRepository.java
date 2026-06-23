package com.example.wms.repository;

import com.example.wms.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    List<Lote> findByProdutoIdOrderByDataFabricacaoAsc(Long produtoId);
    boolean existsByEnderecoId(Long enderecoId);
    List<Lote> findByProdutoIdOrderByDataValidadeAsc(Long produtoId);
}
