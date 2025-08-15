package com.example.api_sistema_ventas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "facturacion")
public class Facturacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String rfc;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaVencimiento;
    private Integer iva;
    private String observaciones;
    private String usoCfdi;           // Uso del CFDI del cliente (Ej: "G01")
    private String formaPago;         // Forma de pago (Ej: "03" - Transferencia electrónica)
    private String tipoComprobante;   // "I" (Ingreso), "E" (Egreso), etc.
    private String regimenFiscalEmisor; // Ej: "601"
    private String serie;             // Serie de la factura (Ej: "F")
    private String folio;             // Folio asignado por tu sistema


    @OneToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;
}

