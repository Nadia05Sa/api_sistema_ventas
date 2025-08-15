package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.dto.FacturacionDTO;
import com.example.api_sistema_ventas.dto.FacturacionCreateDTO;
import com.example.api_sistema_ventas.exception.FacturaNotFoundException; // Nueva excepción personalizada
import com.example.api_sistema_ventas.service.FacturacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturacion")
@CrossOrigin(origins = "*")
public class FacturacionController {

    @Autowired
    private FacturacionService facturacionService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearFactura(@RequestBody FacturacionCreateDTO createDTO) {
        try {
            FacturacionDTO factura = facturacionService.crearYTimbrarFactura(createDTO);
            return ResponseEntity.ok(factura);
        } catch (FacturaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la factura: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodasLasFacturas() {
        try {
            return ResponseEntity.ok(facturacionService.obtenerTodasLasFacturas());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las facturas: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerFactura(@PathVariable Integer id) {
        try {
            FacturacionDTO factura = facturacionService.obtenerFactura(id);
            return ResponseEntity.ok(factura);
        } catch (FacturaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la factura: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> descargarPDF(@PathVariable Integer id) {
        try {
            byte[] pdfBytes = facturacionService.generarPDF(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "factura_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (FacturaNotFoundException e) {
            // Devuelve JSON con mensaje de error y estado 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Factura no encontrada", "detalle", e.getMessage()));

        } catch (Exception e) {
            // Devuelve JSON con mensaje de error y estado 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al descargar el PDF", "detalle", e.getMessage()));
        }
    }


    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<?> obtenerFacturaPorVenta(@PathVariable Integer ventaId) {
        try {
            FacturacionDTO factura = facturacionService.obtenerFacturaPorVenta(ventaId);
            return ResponseEntity.ok(factura);
        } catch (FacturaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la factura: " + e.getMessage());
        }
    }
}
