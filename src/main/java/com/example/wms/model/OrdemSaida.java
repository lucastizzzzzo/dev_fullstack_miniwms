package com.example.wms.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_saida")
public class OrdemSaida extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataConfirmacao;

    @Column(name = "cliente_id")
    private Long clienteId;

    private String status;

    @OneToMany(mappedBy = "ordemSaida", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemOrdemSaida> itens = new ArrayList<>();

    public OrdemSaida() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ItemOrdemSaida> getItens() { return itens; }
    public void setItens(List<ItemOrdemSaida> itens) { this.itens = itens; }
}
