package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.Empleado;
import com.example.api_sistema_ventas.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    // Obtener todos los empleados
    public List<Empleado> getAllEmpleados() {
        return empleadoRepository.findAll();
    }

    // Buscar empleado por ID
    public Optional<Empleado> getEmpleadoById(Long id) {
        return empleadoRepository.findById(id);
    }

    // Crear o actualizar un empleado
    public Empleado saveEmpleado(Empleado empleado) {
        return empleadoRepository.save(empleado);
    }

    // Cambiar estado del empleado
    public boolean cambiarEstado(Long id) {
        Optional<Empleado> optionalEmpleado = empleadoRepository.findById(id);
        if (optionalEmpleado.isPresent()) {
            Empleado empleado = optionalEmpleado.get();
            empleado.setEstado(!empleado.getEstado()); // alterna true <-> false
            empleadoRepository.save(empleado);
            return true;
        }
        return false;
    }
}
