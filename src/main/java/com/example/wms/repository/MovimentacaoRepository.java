package com.example.wms.repository;

import com.example.wms.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
	List<Movimentacao> findByDestinoIdentificador(String destinoIdentificador);
	List<Movimentacao> findByOrigemIdentificador(String origemIdentificador);

	List<Movimentacao> findAllByOrderByDataMovimentacaoDesc();

	List<Movimentacao> findByTipoOrderByDataMovimentacaoDesc(String tipo);
}
