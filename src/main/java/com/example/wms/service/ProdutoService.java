package com.example.wms.service;

import com.example.wms.model.Produto;
import com.example.wms.repository.ProdutoRepository;
import com.example.wms.repository.LoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;

    public ProdutoService(ProdutoRepository produtoRepository, LoteRepository loteRepository) {
        this.produtoRepository = produtoRepository;
        this.loteRepository = loteRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Optional<Produto> obter(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto criar(Produto p) {
        return produtoRepository.save(p);
    }

    public Produto atualizar(Long id, Produto p) {
        p.setId(id);
        return produtoRepository.save(p);
    }

    public void excluir(Long id) {
        produtoRepository.findById(id).ifPresent(produto -> {
            produto.setAtivo(false);
            produtoRepository.save(produto);
        });
    }

    public void toggleAtivo(Long id) {
        produtoRepository.findById(id).ifPresent(produto -> {
            produto.setAtivo(produto.getAtivo()==null?Boolean.FALSE:!produto.getAtivo());
            produtoRepository.save(produto);
        });
    }

    public void removerComEstoque(Long id) {
        produtoRepository.findById(id).ifPresent(produto -> {
            // delete related lotes then product
            var lotes = loteRepository.findByProdutoIdOrderByDataFabricacaoAsc(id);
            loteRepository.deleteAll(lotes);
            produtoRepository.delete(produto);
        });
    }

    public double quantidadeDisponivel(Long produtoId) {
        var lotes = loteRepository.findByProdutoIdOrderByDataFabricacaoAsc(produtoId);
        return lotes.stream().mapToDouble(l -> (l.getQuantidadeTotal()!=null?l.getQuantidadeTotal():0.0) - (l.getQuantidadeReservada()!=null?l.getQuantidadeReservada():0.0)).sum();
    }
}
