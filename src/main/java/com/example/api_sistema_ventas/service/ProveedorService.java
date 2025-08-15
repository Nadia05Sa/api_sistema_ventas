package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.Proveedor;
import com.example.api_sistema_ventas.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> obtenerPorId(Integer id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor guardar(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public Proveedor actualizar(Integer id, Proveedor proveedorActualizado) {
        Optional<Proveedor> proveedorExistente = proveedorRepository.findById(id);
        if (proveedorExistente.isPresent()) {
            Proveedor proveedor = proveedorExistente.get();

            if (proveedorActualizado.getNombre() != null)
                proveedor.setNombre(proveedorActualizado.getNombre());

            if (proveedorActualizado.getEmpresa() != null)
                proveedor.setEmpresa(proveedorActualizado.getEmpresa());

            if (proveedorActualizado.getTelefono() != null)
                proveedor.setTelefono(proveedorActualizado.getTelefono());

            if (proveedorActualizado.getCorreo() != null)
                proveedor.setCorreo(proveedorActualizado.getCorreo());

            if (proveedorActualizado.getProductosSuministrados() != null)
                proveedor.setProductosSuministrados(proveedorActualizado.getProductosSuministrados());

            return proveedorRepository.save(proveedor);
        }
        return null;
    }


    public boolean cambiarEstado(Integer id, Boolean estado) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (proveedorOpt.isPresent()) {
            Proveedor proveedor = proveedorOpt.get();
            proveedor.setEstado(estado);
            proveedorRepository.save(proveedor);
            return true;
        }
        return false;
    }
}
