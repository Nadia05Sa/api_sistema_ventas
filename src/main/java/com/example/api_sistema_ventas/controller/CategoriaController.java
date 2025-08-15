package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Categoria;
import com.example.api_sistema_ventas.service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categoria")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // Crear una nueva categoría
    @PostMapping
    public ResponseEntity<Categoria> saveCategoria(@RequestBody Categoria categoria) {
        Categoria newCategoria = categoriaService.save(categoria);
        return ResponseEntity.ok(newCategoria);
    }

    // Obtener todas las categorías
    @GetMapping
    public ResponseEntity<List<Categoria>> getCategorias() {
        List<Categoria> categorias = categoriaService.getCategorias();
        return categorias.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(categorias);
    }

    // Buscar una categoría por ID
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> getCategoriaById(@PathVariable Integer id) {
        Optional<Categoria> categoria = categoriaService.getCategoriaById(id);
        return categoria.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Categoria> actualizarEstadoCategoria(@PathVariable Integer id, @RequestBody Map<String, Boolean> estado) {
        if (!estado.containsKey("estado")) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Categoria> categoriaActualizada = categoriaService.actualizarEstadoCategoria(id, estado.get("estado"));
        return categoriaActualizada.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
