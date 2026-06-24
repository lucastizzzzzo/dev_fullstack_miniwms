package com.example.wms.service;

import com.example.wms.model.Lote;
import com.example.wms.model.Movimentacao;
import com.example.wms.repository.LoteRepository;
import com.example.wms.repository.MovimentacaoRepository;
import com.example.wms.service.ProdutoService;
import com.example.wms.service.EnderecoService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoteService {
    private final LoteRepository loteRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoService produtoService;
    private final EnderecoService enderecoService;

    public LoteService(LoteRepository loteRepository, MovimentacaoRepository movimentacaoRepository, ProdutoService produtoService, EnderecoService enderecoService) {
        this.loteRepository = loteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoService = produtoService;
        this.enderecoService = enderecoService;
    }

    public List<Lote> listarTodos() { return loteRepository.findAll(); }

    public Optional<Lote> obter(Long id) { return loteRepository.findById(id); }

    public Lote criar(Lote l) {
        // business rule: only allow creating lote for active products
        if (l.getProduto() == null) {
            throw new IllegalArgumentException("Lote deve referenciar um produto");
        }
        var prodOpt = produtoService.obter(l.getProduto().getId());
        if (prodOpt.isEmpty() || prodOpt.get().getAtivo() == null || !prodOpt.get().getAtivo()) {
            throw new IllegalArgumentException("Não é permitido criar lote para produto inativo");
        }

        Lote saved = loteRepository.save(l);
        // create an ENTRADA movimentacao for this lote if quantidadeTotal present and > 0
        if (saved.getQuantidadeTotal() != null && saved.getQuantidadeTotal() > 0) {
            Movimentacao mov = new Movimentacao();
            mov.setTipo("ENTRADA");
            mov.setLote(saved);
            mov.setQuantidade(saved.getQuantidadeTotal());
            mov.setDataMovimentacao(LocalDateTime.now());
            if (saved.getProduto() != null) {
                var pOpt = produtoService.obter(saved.getProduto().getId());
                if (pOpt.isPresent()) {
                    var p = pOpt.get();
                    var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
                    mov.setProdutoIdentificador(ident);
                    mov.setProdutoId(p.getId());
                } else {
                    mov.setProdutoIdentificador(saved.getProduto().getNome());
                    mov.setProdutoId(saved.getProduto().getId());
                }
            }
            if (saved.getCodigoLote() != null) mov.setLoteIdentificador(saved.getCodigoLote());
            if (saved.getId() != null) mov.setLoteId(saved.getId());
            if (saved.getEndereco() != null) {
                mov.setDestinoIdentificador(saved.getEndereco().getCodigo());
                if (saved.getEndereco().getId() != null) mov.setDestinoId(saved.getEndereco().getId());
            }
            movimentacaoRepository.save(mov);
        }
        return saved;
    }

    public Lote atualizarEndereco(Long loteId, Long enderecoId) {
        var loteOpt = loteRepository.findById(loteId);
        if (loteOpt.isEmpty()) throw new IllegalArgumentException("Lote não encontrado");
        var lote = loteOpt.get();
        var enderecoOpt = enderecoService.obter(enderecoId);
        if (enderecoOpt.isEmpty()) throw new IllegalArgumentException("Endereço não encontrado");
        var origem = lote.getEndereco();
        var destino = enderecoOpt.get();
        lote.setEndereco(destino);
        Lote saved = loteRepository.save(lote);

        Movimentacao mov = new Movimentacao();
        mov.setTipo("ENDERECO ALTERADO");
        mov.setQuantidade(0.0);
        mov.setDataMovimentacao(LocalDateTime.now());
        if (saved.getProduto() != null) {
            var p = saved.getProduto();
            var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
            mov.setProdutoIdentificador(ident);
            if (p.getId() != null) mov.setProdutoId(p.getId());
        }
        if (saved.getCodigoLote() != null) mov.setLoteIdentificador(saved.getCodigoLote());
        if (saved.getId() != null) mov.setLoteId(saved.getId());
        if (origem != null) {
            mov.setOrigemIdentificador(origem.getCodigo());
            if (origem.getId() != null) mov.setOrigemId(origem.getId());
        }
        if (destino != null) {
            mov.setDestinoIdentificador(destino.getCodigo());
            if (destino.getId() != null) mov.setDestinoId(destino.getId());
        }
        movimentacaoRepository.save(mov);

        return saved;
    }

    public Lote atualizar(Long id, Lote l) { l.setId(id); return loteRepository.save(l); }

    public void excluir(Long id) {
        loteRepository.findById(id).ifPresent(lote -> { lote.setStatus("BLOQUEADO"); loteRepository.save(lote); });
    }

    public List<Lote> buscarPorProduto(Long produtoId) { return loteRepository.findByProdutoIdOrderByDataFabricacaoAsc(produtoId); }
}
