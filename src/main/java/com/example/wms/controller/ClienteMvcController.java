package com.example.wms.controller;

import com.example.wms.model.Cliente;
import com.example.wms.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteMvcController {
    private final ClienteService clienteService;

    public ClienteMvcController(ClienteService clienteService) { this.clienteService = clienteService; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        model.addAttribute("isAdmin", isAdmin);
        return "clientes/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String criar(@Valid @ModelAttribute Cliente cliente, BindingResult br) {
        if (br.hasErrors()) return "clientes/form";
        clienteService.criar(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/{id}")
    public String edit(@PathVariable Long id, Model model) {
        var c = clienteService.obter(id).orElse(null);
        if (c == null) return "redirect:/clientes";
        model.addAttribute("cliente", c);
        return "clientes/form";
    }

    @PostMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute Cliente cliente, BindingResult br) {
        if (br.hasErrors()) return "clientes/form";
        clienteService.atualizar(id, cliente);
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/desativar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String desativar(@PathVariable Long id) {
        clienteService.excluir(id);
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/deletar")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String deletar(@PathVariable Long id) {
        clienteService.deletar(id);
        return "redirect:/clientes";
    }
}
