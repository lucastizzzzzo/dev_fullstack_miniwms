package com.example.wms.controller;

import com.example.wms.model.Movimentacao;
import com.example.wms.service.MovimentacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes")
public class MovimentacaoController {
    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) { this.movimentacaoService = movimentacaoService; }

    @GetMapping
    public ResponseEntity<List<Movimentacao>> listar() { return ResponseEntity.ok(movimentacaoService.listarTodos()); }

    @PostMapping
    public ResponseEntity<Movimentacao> criar(@RequestBody Movimentacao input) {
        Movimentacao criado = movimentacaoService.criar(input);
        return ResponseEntity.created(URI.create("/api/movimentacoes/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movimentacao> obter(@PathVariable Long id) { return movimentacaoService.obter(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<Movimentacao> atualizar(@PathVariable Long id, @RequestBody Movimentacao input) { return ResponseEntity.ok(movimentacaoService.atualizar(id, input)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) { movimentacaoService.excluir(id); return ResponseEntity.noContent().build(); }
}
