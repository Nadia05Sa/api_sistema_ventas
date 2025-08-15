// FacturacionCreateDTO.java
package com.example.api_sistema_ventas.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class FacturacionCreateDTO {

    @NotNull(message = "El ID de venta es obligatorio")
    private Integer ventaId;

    @NotNull(message = "El RFC es obligatorio")
    @Pattern(regexp = "^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$", message = "RFC inválido")
    private String rfc;

    private String observaciones;

    // Uso CFDI - Catálogo SAT
    // P01 - Por definir, G01 - Adquisición de mercancías, etc.
    @Pattern(regexp = "^[A-Z][0-9]{2}$", message = "Uso CFDI inválido")
    private String usoCfdi = "P01"; // Valor por defecto

    // Forma de pago - Catálogo SAT
    // 01 - Efectivo, 03 - Transferencia electrónica, 04 - Tarjeta de crédito, etc.
    @Pattern(regexp = "^[0-9]{2}$", message = "Forma de pago inválida")
    private String formaPago = "03"; // Transferencia por defecto

    // Método de pago
    // PUE - Pago en una sola exhibición, PPD - Pago en parcialidades o diferido
    private String metodoPago = "PUE";

    // Datos adicionales del cliente
    private String nombreCliente;
    private String codigoPostalCliente;
    private String regimenFiscalCliente;
}