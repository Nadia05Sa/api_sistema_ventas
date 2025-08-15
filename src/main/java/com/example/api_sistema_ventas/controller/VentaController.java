package com.example.api_sistema_ventas.controller;

import com.example.api_sistema_ventas.model.Venta;
import com.example.api_sistema_ventas.repository.VentaRepository;
import com.example.api_sistema_ventas.service.NotaVentaPdfService;
import com.example.api_sistema_ventas.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private NotaVentaPdfService pdfService;

    @GetMapping("/{id}/nota-pdf")
    public ResponseEntity<byte[]> descargarNotaVenta(@PathVariable Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        ByteArrayInputStream pdfStream = pdfService.generarPdf(venta);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=nota_venta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfStream.readAllBytes());
    }

    @PostMapping
    public ResponseEntity<Venta> crearVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.crearVenta(venta);
        return ResponseEntity.ok(nuevaVenta);
    }

    @GetMapping
    public ResponseEntity<List<Venta>> listarVentas() {
        return ResponseEntity.ok(ventaService.getAllVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVenta(@PathVariable Integer id) {
        Optional<Venta> venta = ventaService.getVentaById(id);
        return venta.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoVenta(@PathVariable Integer id, @RequestParam boolean estado) {
        try {
            Venta ventaActualizada = ventaService.actualizarEstadoVenta(id, estado);
            return ResponseEntity.ok(ventaActualizada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la venta");
        }
    }

}
