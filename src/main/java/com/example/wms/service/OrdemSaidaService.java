package com.example.wms.service;

import com.example.wms.model.ItemOrdemSaida;
import com.example.wms.model.Lote;
import com.example.wms.model.Movimentacao;
import com.example.wms.model.OrdemSaida;
import com.example.wms.repository.LoteRepository;
import com.example.wms.repository.MovimentacaoRepository;
import com.example.wms.repository.OrdemSaidaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrdemSaidaService {
    private final OrdemSaidaRepository ordemRepo;
    private final LoteRepository loteRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final com.example.wms.repository.UsuarioRepository usuarioRepository;
    private final com.example.wms.repository.ProdutoRepository produtoRepository;

    public OrdemSaidaService(OrdemSaidaRepository ordemRepo, LoteRepository loteRepository, MovimentacaoRepository movimentacaoRepository, com.example.wms.repository.UsuarioRepository usuarioRepository, com.example.wms.repository.ProdutoRepository produtoRepository) {
        this.ordemRepo = ordemRepo;
        this.loteRepository = loteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<OrdemSaida> listarTodos() { return ordemRepo.findAll(); }

    public Optional<OrdemSaida> obter(Long id) { return ordemRepo.findById(id); }

    public OrdemSaida criar(OrdemSaida o) {
        o.setDataCriacao(LocalDateTime.now());
        // ensure codigo exists
        if (o.getCodigo() == null || o.getCodigo().isBlank()) {
            o.setCodigo("OS-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        }

        // business rule: only allow orders for active products
        if (o.getItens() != null) {
            for (var itCheck : o.getItens()) {
                if (itCheck.getProduto() == null) continue;
                var produto = itCheck.getProduto();
                if (produto.getAtivo() == null || !produto.getAtivo()) {
                    throw new IllegalArgumentException("Não é permitido criar ordem para produto inativo: " + produto.getSku());
                }
            }
        }

        // try to allocate/reserve quantities on lotes for each item
        if (o.getItens() != null) {
            for (var item : o.getItens()) {
                if (item.getProduto() == null || item.getQuantidade() == null) continue;
                double needed = item.getQuantidade();
                // find lots for product in FIFO order
                var lotes = loteRepository.findByProdutoIdOrderByDataFabricacaoAsc(item.getProduto().getId());
                boolean allocated = false;
                for (var lote : lotes) {
                    double available = (lote.getQuantidadeTotal() != null ? lote.getQuantidadeTotal() : 0.0) - (lote.getQuantidadeReservada() != null ? lote.getQuantidadeReservada() : 0.0);
                    if (available >= needed) {
                        lote.setQuantidadeReservada((lote.getQuantidadeReservada()!=null?lote.getQuantidadeReservada():0.0) + needed);
                        loteRepository.save(lote);
                        item.setLote(lote);
                        item.setStatusItem("RESERVADA");
                        allocated = true;
                        // create a reservation movimentacao
                        Movimentacao mov = new Movimentacao();
                        mov.setTipo("RESERVA");
                        mov.setQuantidade(item.getQuantidade());
                        mov.setDataMovimentacao(LocalDateTime.now());
                        if (item.getProduto() != null) {
                            var pOpt = produtoRepository.findById(item.getProduto().getId());
                            if (pOpt.isPresent()) {
                                var p = pOpt.get();
                                var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
                                mov.setProdutoIdentificador(ident);
                                mov.setProdutoId(p.getId());
                            } else {
                                mov.setProdutoIdentificador(item.getProduto().getNome());
                                mov.setProdutoId(item.getProduto().getId());
                            }
                        }
                        if (lote != null) {
                            mov.setLoteIdentificador(lote.getCodigoLote());
                            mov.setLoteId(lote.getId());
                            if (lote.getEndereco() != null) {
                                mov.setOrigemIdentificador(lote.getEndereco().getCodigo());
                            }
                        }
                        // set usuario_id from current authenticated user if available
                        try {
                            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                                String principal = auth.getName();
                                usuarioRepository.findByEmail(principal).ifPresent(u -> mov.setUsuarioId(u.getId()));
                            }
                        } catch (Exception ignored) {}
                        movimentacaoRepository.save(mov);
                        break;
                    }
                }
                if (!allocated) {
                    throw new IllegalArgumentException("Saldo insuficiente para produto " + (item.getProduto()!=null?item.getProduto().getSku():"?"));
                }
            }
        }

        // populate identificadores antes de persistir (desnormalização)
        if (o.getItens() != null) {
            for (var item : o.getItens()) {
                if ((item.getProdutoIdentificador() == null || item.getProdutoIdentificador().isBlank()) && item.getProduto() != null) {
                    var p = item.getProduto();
                    var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
                    item.setProdutoIdentificador(ident);
                    item.setProdutoId(p.getId());
                }
                if ((item.getLoteIdentificador() == null || item.getLoteIdentificador().isBlank()) && item.getLote() != null) {
                    item.setLoteIdentificador(item.getLote().getCodigoLote());
                    item.setLoteId(item.getLote().getId());
                }
            }
        }

        o.setStatus("RESERVADA");
        return ordemRepo.save(o);
    }

    public OrdemSaida criar(OrdemSaida o, String allocationStrategy, Long selectedLoteId) {
        o.setDataCriacao(LocalDateTime.now());
        if (o.getCodigo() == null || o.getCodigo().isBlank()) {
            o.setCodigo("OS-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        }

        if (o.getItens() != null) {
            for (var itCheck : o.getItens()) {
                if (itCheck.getProduto() == null) continue;
                var produto = itCheck.getProduto();
                if (produto.getAtivo() == null || !produto.getAtivo()) {
                    throw new IllegalArgumentException("Não é permitido criar ordem para produto inativo: " + produto.getSku());
                }
            }
        }

        if (o.getItens() != null) {
            for (var item : o.getItens()) {
                if (item.getProduto() == null || item.getQuantidade() == null) continue;
                double needed = item.getQuantidade();
                boolean allocated = false;
                if ("MANUAL".equalsIgnoreCase(allocationStrategy)) {
                    if (selectedLoteId == null) throw new IllegalArgumentException("Selecione um lote para alocação manual");
                    var lote = loteRepository.findById(selectedLoteId).orElse(null);
                    if (lote == null) throw new IllegalArgumentException("Lote selecionado não encontrado");
                    double available = (lote.getQuantidadeTotal() != null ? lote.getQuantidadeTotal() : 0.0) - (lote.getQuantidadeReservada() != null ? lote.getQuantidadeReservada() : 0.0);
                    if (available >= needed) {
                        lote.setQuantidadeReservada((lote.getQuantidadeReservada()!=null?lote.getQuantidadeReservada():0.0) + needed);
                        loteRepository.save(lote);
                        item.setLote(lote);
                        item.setStatusItem("RESERVADA");
                        allocated = true;
                    }
                } else {
                    List<Lote> lotes;
                    if ("FEFO".equalsIgnoreCase(allocationStrategy)) {
                        lotes = loteRepository.findByProdutoIdOrderByDataValidadeAsc(item.getProduto().getId());
                    } else {
                        lotes = loteRepository.findByProdutoIdOrderByDataFabricacaoAsc(item.getProduto().getId());
                    }
                    for (var lote : lotes) {
                        double available = (lote.getQuantidadeTotal() != null ? lote.getQuantidadeTotal() : 0.0) - (lote.getQuantidadeReservada() != null ? lote.getQuantidadeReservada() : 0.0);
                        if (available >= needed) {
                            lote.setQuantidadeReservada((lote.getQuantidadeReservada()!=null?lote.getQuantidadeReservada():0.0) + needed);
                            loteRepository.save(lote);
                            item.setLote(lote);
                            item.setStatusItem("RESERVADA");
                            allocated = true;
                                // create a reservation movimentacao
                                Movimentacao mov = new Movimentacao();
                                mov.setTipo("RESERVA");
                                mov.setProduto(item.getProduto());
                                mov.setLote(lote);
                                mov.setQuantidade(item.getQuantidade());
                                mov.setDataMovimentacao(LocalDateTime.now());
                                if (item.getProduto() != null) {
                                    var p = item.getProduto();
                                    var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
                                    mov.setProdutoIdentificador(ident);
                                }
                                if (lote != null) {
                                    mov.setLoteIdentificador(lote.getCodigoLote());
                                    if (lote.getEndereco() != null) mov.setOrigemIdentificador(lote.getEndereco().getCodigo());
                                }
                                movimentacaoRepository.save(mov);
                            break;
                        }
                    }
                }
                if (!allocated) {
                    throw new IllegalArgumentException("Saldo insuficiente para produto " + (item.getProduto()!=null?item.getProduto().getSku():"?"));
                }
            }
        }

        // populate identificadores antes de persistir (desnormalização)
        if (o.getItens() != null) {
            for (var item : o.getItens()) {
                if ((item.getProdutoIdentificador() == null || item.getProdutoIdentificador().isBlank()) && item.getProduto() != null) {
                    var p = item.getProduto();
                    var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + p.getNome()) : p.getNome();
                    item.setProdutoIdentificador(ident);
                    item.setProdutoId(p.getId());
                }
                if ((item.getLoteIdentificador() == null || item.getLoteIdentificador().isBlank()) && item.getLote() != null) {
                    item.setLoteIdentificador(item.getLote().getCodigoLote());
                    item.setLoteId(item.getLote().getId());
                }
            }
        }

        o.setStatus("RESERVADA");
        return ordemRepo.save(o);
    }

    public OrdemSaida atualizar(Long id, OrdemSaida o) { o.setId(id); return ordemRepo.save(o); }

    public void excluir(Long id) { ordemRepo.findById(id).ifPresent(ordem -> { ordem.setStatus("CANCELADA"); ordemRepo.save(ordem); }); }

    public OrdemSaida confirmar(Long id) {
        OrdemSaida ordem = ordemRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Ordem não encontrada"));
        if (ordem.getStatus() == null || !ordem.getStatus().equalsIgnoreCase("RESERVADA")) {
            throw new IllegalStateException("Ordem não está em estado RESERVADA e não pode ser despachada");
        }
        List<ItemOrdemSaida> itens = ordem.getItens();
        for (ItemOrdemSaida item : itens) {
            Lote lote = null;
            Long loteId = item.getLoteId() != null ? item.getLoteId() : (item.getLote() != null ? item.getLote().getId() : null);
            if (loteId != null) {
                lote = loteRepository.findById(loteId).orElse(null);
            } else if (item.getLote() != null) {
                lote = item.getLote();
            }
            if (lote != null) {
                // ensure we operate on the freshest instance from DB when possible
                if (lote.getId() != null) {
                    lote = loteRepository.findById(lote.getId()).orElse(lote);
                }
                item.setLote(lote);
                double q = item.getQuantidade() != null ? item.getQuantidade() : 0.0;
                double total = lote.getQuantidadeTotal() != null ? lote.getQuantidadeTotal() : 0.0;
                double reservada = lote.getQuantidadeReservada() != null ? lote.getQuantidadeReservada() : 0.0;
                lote.setQuantidadeTotal(Math.max(0.0, total - q));
                lote.setQuantidadeReservada(Math.max(0.0, reservada - q));
                loteRepository.save(lote);
            }
            item.setStatusItem("ATENDIDO");

            Movimentacao mov = new Movimentacao();
            mov.setTipo("SAIDA");
            mov.setQuantidade(item.getQuantidade());
            mov.setDataMovimentacao(LocalDateTime.now());
            // ensure identificadores and ids are populated on movimentacao
            if (item.getProdutoIdentificador() != null && !item.getProdutoIdentificador().isBlank()) {
                mov.setProdutoIdentificador(item.getProdutoIdentificador());
            } else if (item.getProduto() != null) {
                var pOpt = produtoRepository.findById(item.getProduto().getId());
                if (pOpt.isPresent()) {
                    var p = pOpt.get();
                    var ident = (p.getSku() != null && !p.getSku().isBlank()) ? (p.getSku() + " - " + (p.getDescricao()!=null && !p.getDescricao().isBlank() ? p.getDescricao() : p.getNome())) : (p.getDescricao()!=null? p.getDescricao(): p.getNome());
                    mov.setProdutoIdentificador(ident);
                    mov.setProdutoId(p.getId());
                } else {
                    mov.setProdutoIdentificador(item.getProduto()!=null?item.getProduto().getNome():null);
                    if (item.getProduto()!=null) mov.setProdutoId(item.getProduto().getId());
                }
            }
            if (item.getLoteIdentificador() != null && !item.getLoteIdentificador().isBlank()) {
                mov.setLoteIdentificador(item.getLoteIdentificador());
            } else if (item.getLote() != null) {
                mov.setLoteIdentificador(item.getLote().getCodigoLote());
            }
            if (item.getLote() != null) {
                mov.setLoteId(item.getLote().getId());
                if (item.getLote().getEndereco() != null) mov.setOrigemIdentificador(item.getLote().getEndereco().getCodigo());
            }
            movimentacaoRepository.save(mov);
        }
        ordem.setStatus("DESPACHADA");
        ordem.setDataConfirmacao(LocalDateTime.now());
        return ordemRepo.save(ordem);
    }
}
