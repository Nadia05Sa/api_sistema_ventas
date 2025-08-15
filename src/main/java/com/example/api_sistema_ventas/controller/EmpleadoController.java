package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Empleado;
import com.example.api_sistema_ventas.service.EmpleadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {

    @Autowired
    private EmpleadoService empleadoService;

    // Obtener todos los empleados
    @GetMapping
    public ResponseEntity<List<Empleado>> getAllEmpleados() {
        return ResponseEntity.ok(empleadoService.getAllEmpleados());
    }

    // Obtener empleado por ID
    @GetMapping("/{id}")
    public ResponseEntity<Empleado> getEmpleadoById(@PathVariable Long id) {
        Optional<Empleado> empleado = empleadoService.getEmpleadoById(id);
        return empleado.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear nuevo empleado
    @PostMapping
    public ResponseEntity<Empleado> crearEmpleado(@RequestBody Empleado empleado) {
        Empleado nuevo = empleadoService.saveEmpleado(empleado);
        return ResponseEntity.ok(nuevo);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Empleado> actualizarEmpleado(@PathVariable Long id, @RequestBody Empleado empleado) {
        Optional<Empleado> existenteOpt = empleadoService.getEmpleadoById(id);

        if (existenteOpt.isPresent()) {
            Empleado existente = existenteOpt.get();

            // Solo actualiza si no es null
            if (empleado.getNombre() != null) existente.setNombre(empleado.getNombre());
            if (empleado.getApellido() != null) existente.setApellido(empleado.getApellido());
            if (empleado.getTelefono() != null) existente.setTelefono(empleado.getTelefono());
            if (empleado.getCorreo() != null) existente.setCorreo(empleado.getCorreo());
            if (empleado.getCargo() != null) existente.setCargo(empleado.getCargo());
            if (empleado.getEstado() != null) existente.setEstado(empleado.getEstado());
            if (empleado.getContrasena() != null) existente.setContrasena(empleado.getContrasena());
            // agrega más campos según tu modelo

            Empleado actualizado = empleadoService.saveEmpleado(existente);
            return ResponseEntity.ok(actualizado);
        }

        return ResponseEntity.notFound().build();
    }


    // Cambiar estado (activo/inactivo)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id) {
        boolean actualizado = empleadoService.cambiarEstado(id);
        if (actualizado) {
            return ResponseEntity.ok("Estado actualizado correctamente.");
        }
        return ResponseEntity.notFound().build();
    }
}
