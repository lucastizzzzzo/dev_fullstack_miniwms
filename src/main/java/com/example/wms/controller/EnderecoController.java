package com.example.wms.controller;

import com.example.wms.model.Endereco;
import com.example.wms.service.EnderecoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {
    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @GetMapping
    public ResponseEntity<List<Endereco>> listar() {
        return ResponseEntity.ok(enderecoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Endereco> criar(@RequestBody Endereco input) {
        Endereco criado = enderecoService.criar(input);
        return ResponseEntity.created(URI.create("/api/enderecos/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endereco> obter(@PathVariable Long id) {
        return enderecoService.obter(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Endereco> atualizar(@PathVariable Long id, @RequestBody Endereco input) {
        Endereco atualizado = enderecoService.atualizar(id, input);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        enderecoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
