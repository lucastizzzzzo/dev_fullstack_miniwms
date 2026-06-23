package com.example.wms.controller;

import com.example.wms.model.Lote;
import com.example.wms.service.LoteService;
import com.example.wms.service.EnderecoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/lotes")
public class LoteMvcController {
    private final LoteService loteService;
    private final com.example.wms.service.ProdutoService produtoService;
    private final EnderecoService enderecoService;

    public LoteMvcController(LoteService loteService, com.example.wms.service.ProdutoService produtoService, EnderecoService enderecoService) {
        this.loteService = loteService;
        this.produtoService = produtoService;
        this.enderecoService = enderecoService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("lotes", loteService.listarTodos());
        return "lotes/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("lote", new Lote());
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("enderecos", enderecoService.listarTodos());
        return "lotes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute Lote lote, BindingResult br, Model model) {
        if (br.hasErrors()) { model.addAttribute("lote", lote); model.addAttribute("produtos", produtoService.listarTodos()); return "lotes/form"; }
        loteService.criar(lote);
        return "redirect:/lotes";
    }

    @GetMapping("/{id}")
    public String obter(@PathVariable Long id, Model model) {
        var opt = loteService.obter(id);
        if (opt.isEmpty()) return "redirect:/lotes";
        model.addAttribute("lote", opt.get());
        model.addAttribute("enderecos", enderecoService.listarTodos());
        return "lotes/detail";
    }

    @PostMapping("/{id}/endereco")
    public String atualizarEndereco(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestParam(required = false) Long enderecoId, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            if (enderecoId == null) throw new IllegalArgumentException("Selecione um endereço");
            loteService.atualizarEndereco(id, enderecoId);
            ra.addFlashAttribute("success", "Endereço atualizado com sucesso");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/lotes/" + id;
    }
}
