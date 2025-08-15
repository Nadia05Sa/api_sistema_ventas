package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.Categoria;
import com.example.api_sistema_ventas.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Guardar una nueva categoría
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Obtener todas las categorías
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }


    // Buscar una categoría por ID
    public Optional<Categoria> getCategoriaById(Integer id) {
        return categoriaRepository.findById(id);
    }

    //Actualizar estado de categoria
    public Optional<Categoria> actualizarEstadoCategoria(Integer id, boolean estado) {
        Optional<Categoria> categoriaOptional = categoriaRepository.findById(id);

        if (categoriaOptional.isPresent()) {
            Categoria categoria = categoriaOptional.get();
            categoria.setEstado(estado);
            return Optional.of(categoriaRepository.save(categoria));
        }

        return Optional.empty();
    }

}
