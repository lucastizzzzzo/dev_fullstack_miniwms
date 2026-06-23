package com.example.wms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_ordem_saida")
public class ItemOrdemSaida extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ordem_saida_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private OrdemSaida ordemSaida;

    @Transient
    private Long produtoId;

    @Transient
    private Long loteId;

    @Column(name = "produto_identificador")
    private String produtoIdentificador;

    @Column(name = "lote_identificador")
    private String loteIdentificador;

    @Transient
    private Produto produto;

    @Transient
    private Lote lote;

    private Double quantidade;

    private String unidade;

    private String statusItem;

    public ItemOrdemSaida() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrdemSaida getOrdemSaida() { return ordemSaida; }
    public void setOrdemSaida(OrdemSaida ordemSaida) { this.ordemSaida = ordemSaida; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }

    public String getProdutoIdentificador() { return produtoIdentificador; }
    public void setProdutoIdentificador(String produtoIdentificador) { this.produtoIdentificador = produtoIdentificador; }

    public String getLoteIdentificador() { return loteIdentificador; }
    public void setLoteIdentificador(String loteIdentificador) { this.loteIdentificador = loteIdentificador; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Lote getLote() { return lote; }
    public void setLote(Lote lote) { this.lote = lote; }

    public Double getQuantidade() { return quantidade; }
    public void setQuantidade(Double quantidade) { this.quantidade = quantidade; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getStatusItem() { return statusItem; }
    public void setStatusItem(String statusItem) { this.statusItem = statusItem; }
}
