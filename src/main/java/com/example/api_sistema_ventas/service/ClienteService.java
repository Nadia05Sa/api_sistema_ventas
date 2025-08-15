package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.Cliente;
import com.example.api_sistema_ventas.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Obtener todos los clientes
    public List<Cliente> getAllClientes() {
        return clienteRepository.findAll();
    }

    // Buscar cliente por ID
    public Optional<Cliente> getClienteById(Integer id) {
        return clienteRepository.findById(id);
    }

    // Crear o actualizar un cliente
    public Cliente saveCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    // Cambiar el estado del cliente (activo/inactivo)
    public boolean cambiarEstado(Integer id) {
        Optional<Cliente> optionalCliente = clienteRepository.findById(id);
        if (optionalCliente.isPresent()) {
            Cliente cliente = optionalCliente.get();
            cliente.setEstado(!cliente.getEstado()); // Alterna entre true/false
            clienteRepository.save(cliente);
            return true;
        }
        return false;
    }
}
