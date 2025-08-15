package com.example.api_sistema_ventas.repository;

import com.example.api_sistema_ventas.model.Categoria;
import com.example.api_sistema_ventas.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Buscar productos por categoría
    List<Producto> findByCategoria(Categoria categoria);

    // Buscar productos activos
    List<Producto> findByEstadoTrue();

    // Buscar productos inactivos
    List<Producto> findByEstadoFalse();

    // Buscar producto por código
    Optional<Producto> findByCodigo(String codigo);

    // Buscar productos por nombre (contiene)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos por categoría y estado
    List<Producto> findByCategoriaAndEstado(Categoria categoria, Boolean estado);

    // Query personalizada para buscar productos con stock bajo
    @Query("SELECT p FROM Producto p WHERE p.stock <= :stockMinimo")
    List<Producto> findProductosConStockBajo(@Param("stockMinimo") Integer stockMinimo);

    // Query para buscar productos por rango de precios
    @Query("SELECT p FROM Producto p WHERE p.precioVenta BETWEEN :precioMin AND :precioMax")
    List<Producto> findByRangoPrecios(@Param("precioMin") java.math.BigDecimal precioMin,
                                      @Param("precioMax") java.math.BigDecimal precioMax);
}