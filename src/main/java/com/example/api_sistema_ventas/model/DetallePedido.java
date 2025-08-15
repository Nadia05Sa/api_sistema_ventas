package com.example.api_sistema_ventas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer cantidad;

    // DetallePedido.java
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    @JsonBackReference(value = "pedido-detalle")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonBackReference(value = "producto-detalle")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    @JsonBackReference(value = "proveedor-detalle")
    private Proveedor proveedor;

}

