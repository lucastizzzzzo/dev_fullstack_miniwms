package com.example.wms.service;

import com.example.wms.model.Movimentacao;
import com.example.wms.repository.MovimentacaoRepository;
import com.example.wms.repository.ProdutoRepository;
import com.example.wms.repository.LoteRepository;
import com.example.wms.repository.EnderecoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovimentacaoService {
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;
    private final EnderecoRepository enderecoRepository;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               ProdutoRepository produtoRepository,
                               LoteRepository loteRepository,
                               EnderecoRepository enderecoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.produtoRepository = produtoRepository;
        this.loteRepository = loteRepository;
        this.enderecoRepository = enderecoRepository;
    }

    public List<Movimentacao> listarTodos() { return movimentacaoRepository.findAll(); }

    public List<Movimentacao> listarTodosOrdenado() { return movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc(); }

    public List<Movimentacao> listarPorTipo(String tipo) { return movimentacaoRepository.findByTipoOrderByDataMovimentacaoDesc(tipo); }

    public Optional<Movimentacao> obter(Long id) { return movimentacaoRepository.findById(id); }

    public Movimentacao criar(Movimentacao m) {
        enrichIdentificadores(m);
        return movimentacaoRepository.save(m);
    }

    public Movimentacao atualizar(Long id, Movimentacao m) {
        m.setId(id);
        enrichIdentificadores(m);
        return movimentacaoRepository.save(m);
    }

    public void excluir(Long id) { movimentacaoRepository.deleteById(id); }

    private void enrichIdentificadores(Movimentacao m) {
        if (m == null) return;
        if ((m.getProdutoIdentificador() == null || m.getProdutoIdentificador().isBlank()) && m.getProdutoId() != null) {
            produtoRepository.findById(m.getProdutoId()).ifPresent(p -> {
                String sku = p.getSku();
                String descricao = p.getDescricao();
                String fallback = p.getNome();
                String combined = (sku != null && !sku.isBlank() ? sku : "") + " - " + (descricao != null && !descricao.isBlank() ? descricao : (fallback != null ? fallback : ""));
                m.setProdutoIdentificador(combined);
            });
        }
        if ((m.getLoteIdentificador() == null || m.getLoteIdentificador().isBlank()) && m.getLoteId() != null) {
            loteRepository.findById(m.getLoteId()).ifPresent(l -> m.setLoteIdentificador(l.getCodigoLote()));
        }
        if ((m.getOrigemIdentificador() == null || m.getOrigemIdentificador().isBlank()) && m.getOrigemId() != null) {
            enderecoRepository.findById(m.getOrigemId()).ifPresent(e -> m.setOrigemIdentificador(e.getCodigo()));
        }
        if ((m.getDestinoIdentificador() == null || m.getDestinoIdentificador().isBlank()) && m.getDestinoId() != null) {
            enderecoRepository.findById(m.getDestinoId()).ifPresent(e -> m.setDestinoIdentificador(e.getCodigo()));
        }

        // If this is an ENTRADA movimentacao and produtoIdentificador is present but produtoId is not,
        // try to resolve by SKU and create the Produto if missing.
        if ("ENTRADA".equalsIgnoreCase(m.getTipo()) && (m.getProdutoId() == null || m.getProdutoId() == 0L)
                && m.getProdutoIdentificador() != null && !m.getProdutoIdentificador().isBlank()) {
            String ident = m.getProdutoIdentificador().trim();
            String sku = ident;
            String restante = "";
            // split on first '-' (with or without surrounding spaces)
            int dash = ident.indexOf('-');
            if (dash > 0) {
                sku = ident.substring(0, dash).trim();
                restante = ident.substring(dash + 1).trim();
            }
            final String skuFinal = sku;
            final String restanteFinal = restante;
            produtoRepository.findBySku(skuFinal).ifPresentOrElse(p -> {
                m.setProdutoId(p.getId());
            }, () -> {
                // create a lightweight produto record
                try {
                    com.example.wms.model.Produto novo = new com.example.wms.model.Produto();
                    novo.setSku(skuFinal);
                    if (restanteFinal != null && !restanteFinal.isBlank()) {
                        novo.setDescricao(restanteFinal);
                        novo.setNome(restanteFinal.length() > 80 ? restanteFinal.substring(0,80) : restanteFinal);
                    } else {
                        novo.setNome(skuFinal);
                        novo.setDescricao(skuFinal);
                    }
                    var saved = produtoRepository.save(novo);
                    m.setProdutoId(saved.getId());
                } catch (Exception ignored) {}
            });
        }
    }
}
