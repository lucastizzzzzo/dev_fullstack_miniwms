package com.example.wms.controller;

import com.example.wms.model.Endereco;
import com.example.wms.service.EnderecoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/enderecos")
public class EnderecoMvcController {
    private final EnderecoService enderecoService;

    public EnderecoMvcController(EnderecoService enderecoService) { this.enderecoService = enderecoService; }

    @GetMapping
    public String list(Model model) {
        List<Endereco> enderecos = enderecoService.listarTodos();
        model.addAttribute("enderecos", enderecos);
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        model.addAttribute("isAdmin", isAdmin);
        return "enderecos/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("endereco", new Endereco());
        return "enderecos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute Endereco endereco, BindingResult br, Model model) {
        if (br.hasErrors()) { return "enderecos/form"; }
        enderecoService.criar(endereco);
        return "redirect:/enderecos";
    }

    @PostMapping("/{id}/desativar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String desativar(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            enderecoService.excluir(id);
            ra.addFlashAttribute("success", "Endereço desativado");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/enderecos";
    }

    @PostMapping("/{id}/deletar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String deletar(@PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            enderecoService.deletar(id);
            ra.addFlashAttribute("success", "Endereço deletado");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/enderecos";
    }
}
