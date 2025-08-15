// FacturacionDTO.java
package com.example.api_sistema_ventas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FacturacionDTO {
    private Integer id;
    private String rfc;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaVencimiento;
    private Integer iva;
    private String observaciones;
    private String usoCfdi;
    private String formaPago;
    private String tipoComprobante;
    private String regimenFiscalEmisor;
    private String serie;
    private String folio;
    private Integer ventaId;
    private BigDecimal total;
    private String estadoTimbrado; // "TIMBRADO", "PENDIENTE", "ERROR"
    private String uuid; // UUID del SAT
}
