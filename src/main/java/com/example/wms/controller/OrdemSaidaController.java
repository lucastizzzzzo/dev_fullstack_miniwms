package com.example.wms.controller;

import com.example.wms.model.OrdemSaida;
import com.example.wms.service.OrdemSaidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordens")
public class OrdemSaidaController {
    private final OrdemSaidaService ordemService;

    public OrdemSaidaController(OrdemSaidaService ordemService) { this.ordemService = ordemService; }

    @GetMapping
    public ResponseEntity<List<OrdemSaida>> listar() { return ResponseEntity.ok(ordemService.listarTodos()); }

    @PostMapping
    public ResponseEntity<OrdemSaida> criar(@RequestBody OrdemSaida input) {
        OrdemSaida criado = ordemService.criar(input);
        return ResponseEntity.created(URI.create("/api/ordens/" + criado.getId())).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemSaida> obter(@PathVariable Long id) { return ordemService.obter(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<OrdemSaida> confirmar(@PathVariable Long id) {
        OrdemSaida confirmado = ordemService.confirmar(id);
        return ResponseEntity.ok(confirmado);
    }
}
