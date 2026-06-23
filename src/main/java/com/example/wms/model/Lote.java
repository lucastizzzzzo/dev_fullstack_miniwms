package com.example.wms.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lote")
public class Lote extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @Column(nullable = false)
    private String codigoLote;

    private Double quantidadeTotal = 0.0;

    private Double quantidadeReservada = 0.0;

    private LocalDate dataFabricacao;

    private LocalDate dataValidade;

    private String status;

    public Lote() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }

    public Double getQuantidadeTotal() { return quantidadeTotal; }
    public void setQuantidadeTotal(Double quantidadeTotal) { this.quantidadeTotal = quantidadeTotal; }

    public Double getQuantidadeReservada() { return quantidadeReservada; }
    public void setQuantidadeReservada(Double quantidadeReservada) { this.quantidadeReservada = quantidadeReservada; }

    public LocalDate getDataFabricacao() { return dataFabricacao; }
    public void setDataFabricacao(LocalDate dataFabricacao) { this.dataFabricacao = dataFabricacao; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
