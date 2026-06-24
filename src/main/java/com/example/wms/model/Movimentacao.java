package com.example.wms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao")
public class Movimentacao extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "lote_id")
    private Long loteId;

    @Column(name = "origem_id")
    private Long origemId;

    @Column(name = "destino_id")
    private Long destinoId;

    private Double quantidade;

    private LocalDateTime dataMovimentacao;

    private Long usuarioId;

    private String observacao;

    @Column(name = "produto_identificador")
    private String produtoIdentificador;

    @Column(name = "lote_identificador")
    private String loteIdentificador;

    @Column(name = "origem_identificador")
    private String origemIdentificador;

    @Column(name = "destino_identificador")
    private String destinoIdentificador;

    @Transient
    private Produto produto;

    @Transient
    private Lote lote;

    @Transient
    private Endereco origem;

    @Transient
    private Endereco destino;

    public Movimentacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public Long getOrigemId() { return origemId; }
    public void setOrigemId(Long origemId) { this.origemId = origemId; }

    public Long getDestinoId() { return destinoId; }
    public void setDestinoId(Long destinoId) { this.destinoId = destinoId; }

    public Double getQuantidade() { return quantidade; }
    public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }

    public LocalDateTime getDataMovimentacao() { return dataMovimentacao; }
    public void setDataMovimentacao(LocalDateTime dataMovimentacao) { this.dataMovimentacao = dataMovimentacao; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public String getProdutoIdentificador() { return produtoIdentificador; }
    public void setProdutoIdentificador(String produtoIdentificador) { this.produtoIdentificador = produtoIdentificador; }

    public String getLoteIdentificador() { return loteIdentificador; }
    public void setLoteIdentificador(String loteIdentificador) { this.loteIdentificador = loteIdentificador; }

    public String getOrigemIdentificador() { return origemIdentificador; }
    public void setOrigemIdentificador(String origemIdentificador) { this.origemIdentificador = origemIdentificador; }

    public String getDestinoIdentificador() { return destinoIdentificador; }
    public void setDestinoIdentificador(String destinoIdentificador) { this.destinoIdentificador = destinoIdentificador; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public Endereco getOrigem() { return origem; }
    public void setOrigem(Endereco origem) { this.origem = origem; }

    public Endereco getDestino() { return destino; }
    public void setDestino(Endereco destino) { this.destino = destino; }
}
