package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Producto;
import com.example.api_sistema_ventas.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/producto")
@CrossOrigin(origins = "*") // Permitir CORS si es necesario
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Crear un nuevo producto
    @PostMapping
    public ResponseEntity<?> saveProducto(@RequestBody Producto producto) {
        try {
            Producto newProducto = productoService.save(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProducto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Ver todos los productos
    @GetMapping
    public ResponseEntity<List<Producto>> getProductos() {
        return Optional.ofNullable(productoService.getProductos())
                .filter(lista -> !lista.isEmpty())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // Obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductoById(@PathVariable String id) {
        try {
            Optional<Producto> producto = productoService.getProductoById(id);
            return producto.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Actualización parcial (PATCH)
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchProducto(@PathVariable String id, @RequestBody Producto producto) {
        try {
            Producto productoUpdated = productoService.updateProducto(id, producto);
            return ResponseEntity.ok(productoUpdated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Cambiar estado de activo a inactivo
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoProducto(@PathVariable String id, @RequestBody Map<String, Boolean> requestBody) {
        try {
            if (!requestBody.containsKey("estado")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'estado' es requerido"));
            }

            Optional<Producto> productoActualizado = productoService.actualizarEstadoProducto(id, requestBody.get("estado"));
            return productoActualizado.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Obtener productos por categoría
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<?> getProductosPorCategoria(@PathVariable String idCategoria) {
        try {
            List<Producto> productos = productoService.getProductosPorCategoria(idCategoria);
            return productos.isEmpty() ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    // Obtener solo productos activos
    @GetMapping("/activos")
    public ResponseEntity<List<Producto>> getProductosActivos() {
        try {
            List<Producto> productos = productoService.getProductos();
            List<Producto> productosActivos = productos.stream()
                    .filter(p -> p.getEstado() != null && p.getEstado())
                    .toList();

            return productosActivos.isEmpty() ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.ok(productosActivos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}