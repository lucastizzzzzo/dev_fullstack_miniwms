package com.example.wms.controller;

import com.example.wms.model.Lote;
import com.example.wms.service.LoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {
    private final LoteService loteService;

    public LoteController(LoteService loteService) { this.loteService = loteService; }

    @GetMapping
    public ResponseEntity<List<Lote>> listar() { return ResponseEntity.ok(loteService.listarTodos()); }

    @PostMapping
    public ResponseEntity<Lote> criar(@RequestBody Lote input) {
        Lote criado = loteService.criar(input);
        return ResponseEntity.created(URI.create("/api/lotes/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lote> obter(@PathVariable Long id) { return loteService.obter(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PutMapping("/{id}")
    public ResponseEntity<Lote> atualizar(@PathVariable Long id, @RequestBody Lote input) { return ResponseEntity.ok(loteService.atualizar(id, input)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) { loteService.excluir(id); return ResponseEntity.noContent().build(); }
}
