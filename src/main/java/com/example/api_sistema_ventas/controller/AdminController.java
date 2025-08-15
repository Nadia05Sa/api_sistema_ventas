package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Admin;
import com.example.api_sistema_ventas.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/{id}")
    public ResponseEntity<Admin> obtenerAdmin(@PathVariable Integer id) {
        Optional<Admin> admin = adminService.obtenerAdmin(id);
        return admin.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Admin> crearAdmin(@RequestBody Admin admin) {
        Admin nuevoAdmin = adminService.crearAdmin(admin);
        return ResponseEntity.ok(nuevoAdmin);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> actualizarAdmin(@PathVariable Integer id, @RequestBody Admin admin) {
        Admin actualizado = adminService.actualizarAdmin(id, admin);
        return ResponseEntity.ok(actualizado);
    }
}
