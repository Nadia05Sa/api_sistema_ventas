package com.example.api_sistema_ventas.service;

import java.lang.reflect.Field;
import com.example.api_sistema_ventas.model.Categoria;
import com.example.api_sistema_ventas.model.Producto;
import com.example.api_sistema_ventas.repository.CategoriaRepository;
import com.example.api_sistema_ventas.repository.ProductoRepository;
import com.example.api_sistema_ventas.repository.ProveedorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, ProveedorRepository proveedorRepository ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    // Validar producto (método reutilizable)
    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("❌ Error: El producto no puede ser nulo.");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Error: El producto debe tener un nombre válido.");
        }
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("❌ Error: El precio del producto debe ser mayor a cero.");
        }
    }

    // Crear un nuevo producto
    public Producto save(Producto producto) {
        validarProducto(producto);

        // Establecer valores por defecto si no están presentes
        if (producto.getEstado() == null) {
            producto.setEstado(true); // Por defecto activo
        }
        if (producto.getStock() == null) {
            producto.setStock(0); // Stock inicial en 0
        }

        System.out.println("💾 Guardando producto: " + producto.getNombre());
        return productoRepository.save(producto);
    }

    // Ver todos los productos
    public List<Producto> getProductos() {
        System.out.println("📋 Obteniendo todos los productos...");
        List<Producto> productos = productoRepository.findAll();

        if (productos.isEmpty()) {
            System.out.println("⚠️ Advertencia: No se encontraron productos.");
        } else {
            System.out.println("✅ Productos encontrados: " + productos.size());
        }

        return productos;
    }

    // Actualizar un producto existente
    public Producto updateProducto(String id, Producto productoPatch) throws IllegalAccessException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Error: El ID del producto no puede estar vacío.");
        }

        Integer productId;
        try {
            productId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("❌ Error: El ID del producto debe ser un número válido.");
        }

        Producto productoToUpdate = productoRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("❌ Error: No se encontró el producto con ID " + id));

        // Copiar solo campos no nulos excepto 'id'
        Field[] fields = Producto.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            Object valor = field.get(productoPatch);
            if (valor != null && !field.getName().equals("id")) {
                field.set(productoToUpdate, valor);
            }
        }

        // Validación especial para relaciones (categoría y proveedor)
        if (productoPatch.getCategoria() != null && productoPatch.getCategoria().getId() != null) {
            categoriaRepository.findById(productoPatch.getCategoria().getId())
                    .ifPresent(productoToUpdate::setCategoria);
        }

        if (productoPatch.getProveedor() != null && productoPatch.getProveedor().getId() != null) {
            proveedorRepository.findById(productoPatch.getProveedor().getId())
                    .ifPresent(productoToUpdate::setProveedor);
        }

        return productoRepository.save(productoToUpdate);
    }

    // Cambiar estado de activo a inactivo
    public Optional<Producto> actualizarEstadoProducto(String id, boolean estado) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Error: El ID del producto no puede estar vacío.");
        }

        // Convertir String a Integer
        Integer productId;
        try {
            productId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("❌ Error: El ID del producto debe ser un número válido.");
        }

        Optional<Producto> productoOptional = productoRepository.findById(productId);

        if (productoOptional.isPresent()) {
            Producto producto = productoOptional.get();
            producto.setEstado(estado);
            System.out.println("🔄 Actualizando estado del producto ID " + id + " a: " + (estado ? "ACTIVO" : "INACTIVO"));
            return Optional.of(productoRepository.save(producto));
        }

        return Optional.empty();
    }

    // Obtener productos por categoría
    public List<Producto> getProductosPorCategoria(String idCategoria) {
        if (idCategoria == null || idCategoria.trim().isEmpty()) {
            throw new IllegalArgumentException("❌ Error: El ID de categoría no puede estar vacío.");
        }

        // Convertir String a Integer
        Integer categoriaId;
        try {
            categoriaId = Integer.parseInt(idCategoria);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("❌ Error: El ID de categoría debe ser un número válido.");
        }

        Optional<Categoria> categoriaOpt = categoriaRepository.findById(categoriaId);
        if (categoriaOpt.isEmpty()) {
            throw new IllegalArgumentException("❌ Error: Categoría no encontrada con ID: " + idCategoria);
        }

        // Corregir el método del repositorio
        return productoRepository.findByCategoria(categoriaOpt.get());
    }

    // Método adicional para obtener un producto por ID
    public Optional<Producto> getProductoById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            Integer productId = Integer.parseInt(id);
            return productoRepository.findById(productId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}