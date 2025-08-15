package com.example.api_sistema_ventas.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String empresa;
    private String telefono;
    private String correo;
    private String productosSuministrados;
    private Boolean estado;

    @OneToMany(mappedBy = "proveedor")
    @JsonManagedReference("proveedor-productos")
    private List<Producto> productos;

    // Proveedor.java
    @OneToMany(mappedBy = "proveedor")
    @JsonManagedReference(value = "proveedor-detalle")
    private List<DetallePedido> detalles;



}

