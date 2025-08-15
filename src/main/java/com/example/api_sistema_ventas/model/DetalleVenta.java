package com.example.api_sistema_ventas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detalle_venta")
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer cantidad;
    private BigDecimal precio;
    private BigDecimal descuento;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonBackReference(value = "producto-detalles")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    @JsonBackReference(value = "venta-detalles")
    private Venta venta;
}
