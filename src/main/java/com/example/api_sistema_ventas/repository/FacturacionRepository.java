package com.example.api_sistema_ventas.repository;

import com.example.api_sistema_ventas.model.Facturacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturacionRepository extends JpaRepository<Facturacion, Integer> {

    // Buscar factura por venta
    Optional<Facturacion> findByVentaId(Integer ventaId);

    // Buscar facturas por RFC
    List<Facturacion> findByRfcContainingIgnoreCase(String rfc);

    // Buscar facturas por rango de fechas
    @Query("SELECT f FROM Facturacion f WHERE f.fechaEmision BETWEEN :fechaInicio AND :fechaFin")
    List<Facturacion> findByFechaEmisionBetween(
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );

    // Buscar por serie y folio
    Optional<Facturacion> findBySerieAndFolio(String serie, String folio);

    // Contar facturas por mes
    @Query("SELECT COUNT(f) FROM Facturacion f WHERE YEAR(f.fechaEmision) = :year AND MONTH(f.fechaEmision) = :month")
    Long countByYearAndMonth(@Param("year") int year, @Param("month") int month);

    // Obtener último folio de una serie
    @Query("SELECT MAX(CAST(f.folio AS int)) FROM Facturacion f WHERE f.serie = :serie")
    Integer findMaxFolioBySerie(@Param("serie") String serie);
}
