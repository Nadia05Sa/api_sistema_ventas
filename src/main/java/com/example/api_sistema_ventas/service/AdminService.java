package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.Admin;
import com.example.api_sistema_ventas.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    // Obtener el único administrador (ID = 1 por ejemplo)
    public Optional<Admin> obtenerAdmin(Integer id) {
        return adminRepository.findById(id);
    }

    // Crear nuevo administrador
    public Admin crearAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    // Editar datos del administrador
    public Admin actualizarAdmin(Integer id, Admin adminActualizado) {
        return adminRepository.findById(id).map(admin -> {
            admin.setNombre(adminActualizado.getNombre());
            admin.setNombre_comercial(adminActualizado.getNombre_comercial());
            admin.setTelefono(adminActualizado.getTelefono());
            admin.setWhatsapp(adminActualizado.getWhatsapp());
            admin.setCorreo(adminActualizado.getCorreo());
            admin.setDireccion(adminActualizado.getDireccion());
            return adminRepository.save(admin);
        }).orElseGet(() -> {
            adminActualizado.setId(id);
            return adminRepository.save(adminActualizado);
        });
    }
}
