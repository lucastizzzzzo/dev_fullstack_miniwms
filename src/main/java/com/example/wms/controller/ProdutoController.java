package com.example.wms.controller;

import com.example.wms.model.Produto;
import com.example.wms.service.ProdutoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listar() {
        return ResponseEntity.ok(produtoService.listarTodos());
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Produto> criar(@RequestBody Produto input) {
        Produto criado = produtoService.criar(input);
        return ResponseEntity.created(URI.create("/api/produtos/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> obter(@PathVariable Long id) {
        return produtoService.obter(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto input) {
        Produto atualizado = produtoService.atualizar(id, input);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
