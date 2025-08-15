package com.example.api_sistema_ventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String telefono;
    private String cargo;               // Corregido el nombre
    private String correo;
    private Boolean estado;
    private String rfc;
    private String razonSocial;
    private String regimenFiscal;
    private String usoCfdi;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "direccion_id")
    private Direccion direccion;


}
