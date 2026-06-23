package com.example.wms.controller;

import com.example.wms.model.Movimentacao;
import com.example.wms.service.MovimentacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/movimentacoes")
public class MovimentacaoMvcController {
    private final MovimentacaoService movimentacaoService;

    public MovimentacaoMvcController(MovimentacaoService movimentacaoService) { this.movimentacaoService = movimentacaoService; }

    @GetMapping
    public String list(@org.springframework.web.bind.annotation.RequestParam(value = "tipo", required = false) String tipo, Model model) {
        List<Movimentacao> list;
        if (tipo != null && !tipo.isBlank()) {
            list = movimentacaoService.listarPorTipo(tipo);
            model.addAttribute("filtroTipo", tipo);
        } else {
            list = movimentacaoService.listarTodosOrdenado();
            model.addAttribute("filtroTipo", null);
        }
        model.addAttribute("movimentacoes", list);
        return "movimentacoes/list";
    }
}
