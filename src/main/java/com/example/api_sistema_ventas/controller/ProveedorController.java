package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Proveedor;
import com.example.api_sistema_ventas.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public List<Proveedor> listarTodos() {
        return proveedorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtenerPorId(@PathVariable Integer id) {
        return proveedorService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Proveedor crearProveedor(@RequestBody Proveedor proveedor) {
        proveedor.setEstado(true); // Por defecto activo
        return proveedorService.guardar(proveedor);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Integer id, @RequestBody Proveedor proveedor) {
        Proveedor actualizado = proveedorService.actualizar(id, proveedor);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable Integer id, @RequestBody  Boolean estado) {
        boolean actualizado = proveedorService.cambiarEstado(id, estado);
        return actualizado ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
