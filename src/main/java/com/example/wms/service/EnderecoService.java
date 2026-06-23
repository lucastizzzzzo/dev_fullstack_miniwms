package com.example.wms.service;

import com.example.wms.model.Endereco;
import com.example.wms.repository.EnderecoRepository;
import com.example.wms.repository.MovimentacaoRepository;
import com.example.wms.model.Movimentacao;
import java.time.LocalDateTime;
import com.example.wms.repository.LoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;
    private final LoteRepository loteRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, LoteRepository loteRepository, MovimentacaoRepository movimentacaoRepository) {
        this.enderecoRepository = enderecoRepository;
        this.loteRepository = loteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    public List<Endereco> listarTodos() {
        return enderecoRepository.findAll();
    }

    public Optional<Endereco> obter(Long id) {
        return enderecoRepository.findById(id);
    }

    public Endereco criar(Endereco e) {
        Endereco saved = enderecoRepository.save(e);
        // create a movimentacao recording the cadastro do endereco
        Movimentacao m = new Movimentacao();
        m.setTipo("CADASTRO ENDERECO");
        if (saved.getCodigo() != null) m.setDestinoIdentificador(saved.getCodigo());
        m.setQuantidade(0.0);
        m.setDataMovimentacao(LocalDateTime.now());
        movimentacaoRepository.save(m);
        return saved;
    }

    public Endereco atualizar(Long id, Endereco e) {
        e.setId(id);
        return enderecoRepository.save(e);
    }

    public void excluir(Long id) {
        if (loteRepository.existsByEnderecoId(id)) {
            throw new IllegalArgumentException("Não é possível desativar endereço com lotes associados");
        }
        enderecoRepository.findById(id).ifPresent(endereco -> {
            endereco.setAtivo(false);
            enderecoRepository.save(endereco);
        });
    }

    public void deletar(Long id) {
        var opt = enderecoRepository.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Endereço não encontrado");
        var endereco = opt.get();
        if (endereco.getAtivo() == null || endereco.getAtivo()) {
            throw new IllegalArgumentException("Somente endereços desativados podem ser deletados");
        }
        // Do not allow physical deletion if any movimentacao references this endereco (by identificador)
        var codigo = endereco.getCodigo();
        var destList = (codigo != null) ? movimentacaoRepository.findByDestinoIdentificador(codigo) : java.util.Collections.emptyList();
        var origList = (codigo != null) ? movimentacaoRepository.findByOrigemIdentificador(codigo) : java.util.Collections.emptyList();
        if ((destList != null && !destList.isEmpty()) || (origList != null && !origList.isEmpty())) {
            throw new IllegalArgumentException("Não é possível deletar endereço porque existem movimentações que o referenciam. Mantenha-o desativado para fins de auditoria.");
        }

        // record a deletion movimentacao before removing the endereco
        Movimentacao delMov = new Movimentacao();
        delMov.setTipo("DELETADO ENDERECO");
        delMov.setQuantidade(0.0);
        delMov.setDataMovimentacao(LocalDateTime.now());
        delMov.setObservacao("Endereço deletado: " + (endereco.getCodigo() != null ? endereco.getCodigo() : endereco.getDescricao()));
        if (endereco.getCodigo() != null) delMov.setDestinoIdentificador(endereco.getCodigo());
        movimentacaoRepository.save(delMov);

        enderecoRepository.delete(endereco);
    }
}
