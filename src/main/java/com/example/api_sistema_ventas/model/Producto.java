package com.example.api_sistema_ventas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;
    private String nombre;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioVenta;

    private Integer stock;
    private String descripcion;
    private Boolean estado = true;

    private String claveProductoServicio;
    private String unidad;
    private String claveUnidad;
    private Boolean incluyeIva = false;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonBackReference("categoria-productos")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    @JsonBackReference("proveedor-productos")
    private Proveedor proveedor;

    @OneToMany(mappedBy = "producto")
    @JsonManagedReference(value = "producto-detalles")
    private List<DetalleVenta> detalles;
}
