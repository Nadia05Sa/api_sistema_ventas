package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Cliente;
import com.example.api_sistema_ventas.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Obtener todos los clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClientes() {
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    // Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClienteById(@PathVariable Integer id) {
        Optional<Cliente> cliente = clienteService.getClienteById(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo cliente
    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        Cliente nuevoCliente = clienteService.saveCliente(cliente);
        return ResponseEntity.ok(nuevoCliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Integer id, @RequestBody Cliente cliente) {
        Optional<Cliente> existenteOpt = clienteService.getClienteById(id);

        if (existenteOpt.isPresent()) {
            Cliente existente = existenteOpt.get();

            // Solo actualizar si no es null
            if (cliente.getNombre() != null) existente.setNombre(cliente.getNombre());
            if (cliente.getTelefono() != null) existente.setTelefono(cliente.getTelefono());
            if (cliente.getCargo() != null) existente.setCargo(cliente.getCargo()); // ojo, "cargo"
            if (cliente.getCorreo() != null) existente.setCorreo(cliente.getCorreo());
            if (cliente.getEstado() != null) existente.setEstado(cliente.getEstado());
            if (cliente.getRfc() != null) existente.setRfc(cliente.getRfc());
            if (cliente.getRazonSocial() != null) existente.setRazonSocial(cliente.getRazonSocial());
            if (cliente.getRegimenFiscal() != null) existente.setRegimenFiscal(cliente.getRegimenFiscal());
            if (cliente.getUsoCfdi() != null) existente.setUsoCfdi(cliente.getUsoCfdi());
            if (cliente.getDireccion() != null) existente.setDireccion(cliente.getDireccion());

            Cliente actualizado = clienteService.saveCliente(existente);
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    // Cambiar el estado de un cliente (activo/inactivo)
    @PutMapping ("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable Integer id) {
        boolean actualizado = clienteService.cambiarEstado(id);
        if (actualizado) {
            return ResponseEntity.ok("Estado actualizado correctamente.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
