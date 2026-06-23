package com.example.wms.controller;

import com.example.wms.model.OrdemSaida;
import com.example.wms.service.OrdemSaidaService;
import com.example.wms.service.ProdutoService;
import com.example.wms.service.LoteService;
import com.example.wms.model.ItemOrdemSaida;
import com.example.wms.model.Produto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/ordens")
public class OrdemMvcController {
    private final OrdemSaidaService ordemService;
    private final ProdutoService produtoService;
    private final LoteService loteService;
    private final com.example.wms.service.ClienteService clienteService;

    public OrdemMvcController(OrdemSaidaService ordemService, ProdutoService produtoService, LoteService loteService, com.example.wms.service.ClienteService clienteService) {
        this.ordemService = ordemService;
        this.produtoService = produtoService;
        this.loteService = loteService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String list(Model model) {
        List<OrdemSaida> ordens = ordemService.listarTodos();
        // compute total quantity per order
        var totals = new java.util.HashMap<Long, Double>();
        var produtosInfo = new java.util.HashMap<Long, String>();
        for (OrdemSaida o : ordens) {
            double sum = 0.0;
            if (o.getItens() != null) {
                for (var it : o.getItens()) { sum += (it.getQuantidade() != null ? it.getQuantidade() : 0.0); }
                // build product info string: SKU - Nome (for multiple items join with ", ")
                var parts = new java.util.ArrayList<String>();
                for (var it : o.getItens()) {
                    if (it.getProdutoIdentificador() != null && !it.getProdutoIdentificador().isBlank()) {
                        parts.add(it.getProdutoIdentificador());
                    } else if (it.getProduto() != null) {
                        var sku = it.getProduto().getSku() != null ? it.getProduto().getSku() : "";
                        var nome = it.getProduto().getNome() != null ? it.getProduto().getNome() : "";
                        parts.add((sku.isBlank() ? nome : sku + " - " + nome));
                    }
                }
                produtosInfo.put(o.getId(), String.join(", ", parts));
            }
            totals.put(o.getId(), sum);
        }
        // build cliente names map
        var clientesInfo = new java.util.HashMap<Long, String>();
        for (OrdemSaida o : ordens) {
            if (o.getClienteId() != null) {
                var c = clienteService.obter(o.getClienteId()).orElse(null);
                clientesInfo.put(o.getId(), c != null ? c.getNome() : "-");
            } else {
                clientesInfo.put(o.getId(), "-");
            }
        }
        model.addAttribute("ordens", ordens);
        model.addAttribute("totais", totals);
        model.addAttribute("produtosInfo", produtosInfo);
        model.addAttribute("clientesInfo", clientesInfo);
        return "ordens/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("ordem", new OrdemSaida());
        model.addAttribute("produtos", produtoService.listarTodos());
        model.addAttribute("lotes", loteService.listarTodos());
        model.addAttribute("clientes", clienteService.listarTodos());
        return "ordens/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute OrdemSaida ordem, BindingResult br,
                        @RequestParam(required = false) Long produtoId,
                        @RequestParam(required = false) Double quantidade,
                        @RequestParam(required = false, defaultValue = "FIFO") String allocationStrategy,
                        @RequestParam(required = false) Long loteId,
                        @RequestParam(required = false) Long clienteId,
                        Model model) {
        if (br.hasErrors()) { model.addAttribute("produtos", produtoService.listarTodos()); return "ordens/form"; }

        // If product and quantity provided, add a single item to the ordem
        if (produtoId != null && quantidade != null) {
            Produto p = produtoService.obter(produtoId).orElse(null);
            if (p != null) {
                ItemOrdemSaida item = new ItemOrdemSaida();
                item.setProduto(p);
                item.setQuantidade(quantidade);
                item.setUnidade(p.getUnidadeMedida());
                item.setStatusItem("PENDENTE");
                item.setOrdemSaida(ordem);
                ordem.getItens().add(item);
            }
        }
        if (clienteId != null) ordem.setClienteId(clienteId);
        ordemService.criar(ordem, allocationStrategy, loteId);
        return "redirect:/ordens";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id) {
        ordemService.confirmar(id);
        return "redirect:/ordens";
    }
}
