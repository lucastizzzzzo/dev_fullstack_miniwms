package com.example.wms.service;

import com.example.wms.model.UnidadeMedida;
import com.example.wms.repository.ProdutoRepository;
import com.example.wms.repository.UnidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadeService {
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;

    public UnidadeService(UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository) {
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<UnidadeMedida> listarTodos() { return unidadeRepository.findAll(); }
    public Optional<UnidadeMedida> obter(Long id) { return unidadeRepository.findById(id); }
    public UnidadeMedida criar(UnidadeMedida u) { return unidadeRepository.save(u); }
    public UnidadeMedida atualizar(Long id, UnidadeMedida u) { u.setId(id); return unidadeRepository.save(u); }

    public void excluir(Long id) {
        UnidadeMedida u = unidadeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada"));
        if (produtoRepository.existsByUnidadeMedida(u.getCodigo())) {
            throw new IllegalStateException("Não é possível excluir: unidade está associada a produtos");
        }
        unidadeRepository.delete(u);
    }

    public boolean podeExcluir(UnidadeMedida u) {
        if (u == null || u.getCodigo() == null) return true;
        return !produtoRepository.existsByUnidadeMedida(u.getCodigo());
    }
}
