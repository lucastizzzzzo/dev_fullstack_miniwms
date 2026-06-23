package com.example.wms.controller;

import com.example.wms.model.Lote;
import com.example.wms.model.Produto;
import com.example.wms.service.LoteService;
import com.example.wms.service.ProdutoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/produtos")
public class ProductMvcController {
    private final ProdutoService produtoService;
    private final LoteService loteService;

    public ProductMvcController(ProdutoService produtoService, LoteService loteService) {
        this.produtoService = produtoService;
        this.loteService = loteService;
    }

    @GetMapping
    public String list(Model model) {
        var produtos = produtoService.listarTodos();
        // compute available saldo per produto
        var saldos = new java.util.HashMap<Long, Double>();
        for (var p : produtos) {
            saldos.put(p.getId(), produtoService.quantidadeDisponivel(p.getId()));
        }
        model.addAttribute("produtos", produtos);
        model.addAttribute("saldos", saldos);
        // expose admin flag to template so UI can hide admin-only actions
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        model.addAttribute("isAdmin", isAdmin);
        return "produtos/list";
    }

    @GetMapping("/novo")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String novo(Model model) {
        model.addAttribute("produto", new Produto());
        return "produtos/form";
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String criar(@Valid @ModelAttribute Produto produto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("produto", produto);
            return "produtos/form";
        }
        produtoService.criar(produto);
        return "redirect:/produtos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Produto produto = produtoService.obter(id).orElse(null);
        if (produto == null) return "redirect:/produtos";
        List<Lote> lotes = loteService.buscarPorProduto(id);
        double saldo = lotes.stream().mapToDouble(l -> (l.getQuantidadeTotal()!=null?l.getQuantidadeTotal():0.0) - (l.getQuantidadeReservada()!=null?l.getQuantidadeReservada():0.0)).sum();
        model.addAttribute("produto", produto);
        model.addAttribute("lotes", lotes);
        model.addAttribute("saldo", saldo);
        return "produtos/detail";
    }

    @PostMapping("/{id}/toggle")
    public String toggleAtivo(@PathVariable Long id) {
        produtoService.toggleAtivo(id);
        return "redirect:/produtos";
    }

    @PostMapping("/{id}/delete")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String excluir(@PathVariable Long id) {
        // soft-delete (set ativo = false)
        produtoService.excluir(id);
        return "redirect:/produtos";
    }

    @PostMapping("/{id}/delete-permanent")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMINISTRADOR')")
    public String excluirPermanente(@PathVariable Long id) {
        produtoService.removerComEstoque(id);
        return "redirect:/produtos";
    }
}
