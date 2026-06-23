package com.example.wms.service;

import com.example.wms.model.Cliente;
import com.example.wms.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) { this.clienteRepository = clienteRepository; }

    public List<Cliente> listarTodos() { return clienteRepository.findAll(); }

    public Optional<Cliente> obter(Long id) { return clienteRepository.findById(id); }

    public Cliente criar(Cliente c) { return clienteRepository.save(c); }

    public Cliente atualizar(Long id, Cliente c) { c.setId(id); return clienteRepository.save(c); }

    public void excluir(Long id) { clienteRepository.findById(id).ifPresent(c -> { c.setAtivo(false); clienteRepository.save(c); }); }

    public void deletar(Long id) { clienteRepository.deleteById(id); }
}
