package com.example.wms.controller;

import com.example.wms.model.UnidadeMedida;
import com.example.wms.service.UnidadeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/unidades")
public class UnidadeMvcController {
    private final UnidadeService unidadeService;

    public UnidadeMvcController(UnidadeService unidadeService) { this.unidadeService = unidadeService; }

    @GetMapping
    public String list(Model model) {
        List<UnidadeMedida> list = unidadeService.listarTodos();
        Map<Long, Boolean> podeExcluir = new HashMap<>();
        for (UnidadeMedida u : list) {
            podeExcluir.put(u.getId(), unidadeService.podeExcluir(u));
        }
        model.addAttribute("unidades", list);
        model.addAttribute("podeExcluir", podeExcluir);
        return "unidades/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("unidade", new UnidadeMedida());
        return "unidades/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute UnidadeMedida unidade, BindingResult br) {
        if (br.hasErrors()) return "unidades/form";
        unidadeService.criar(unidade);
        return "redirect:/unidades";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            unidadeService.excluir(id);
            ra.addFlashAttribute("success", "Unidade excluída com sucesso.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/unidades";
    }
}
