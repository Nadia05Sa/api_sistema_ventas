package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.DetalleVenta;
import com.example.api_sistema_ventas.model.Producto;
import com.example.api_sistema_ventas.model.Venta;
import com.example.api_sistema_ventas.repository.ClienteRepository;
import com.example.api_sistema_ventas.repository.ProductoRepository;
import com.example.api_sistema_ventas.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Transactional
    public Venta crearVenta(Venta venta) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.getDetalles()) {
            // Asociar la venta a cada detalle
            detalle.setVenta(venta);

            // Obtener el producto desde la BD
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Calcular subtotal del detalle (precio - descuento) * cantidad
            BigDecimal precioUnitario = detalle.getPrecio();
            BigDecimal descuento = detalle.getDescuento() != null ? detalle.getDescuento() : BigDecimal.ZERO;
            BigDecimal totalDetalle = precioUnitario.subtract(descuento)
                    .multiply(new BigDecimal(detalle.getCantidad()));

            subtotal = subtotal.add(totalDetalle);

            // Si la venta está marcada como "vendido" (estado = true), descontar stock
            if (venta.isEstado()) {
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        // Calcular impuesto (ejemplo: 8%)
        BigDecimal impuesto = subtotal.multiply(new BigDecimal("0.08"));
        BigDecimal total = subtotal.add(impuesto);

        venta.setImpuesto(impuesto);
        venta.setTotal(total);

        return ventaRepository.save(venta);
    }


    public List<Venta> getAllVentas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> getVentaById(Integer id) {
        return ventaRepository.findById(id);
    }

    @Transactional
    public Venta actualizarEstadoVenta(Integer idVenta, boolean nuevoEstado) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        // Si ya está vendida y el nuevo estado también es "vendido" (true), no procesar
        if (venta.isEstado() && nuevoEstado) {
            throw new IllegalStateException("La venta ya está marcada como 'vendido'. No se puede procesar nuevamente.");
        }

        // Si el nuevo estado es "vendido" (true)
        if (nuevoEstado) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();

                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                }

                producto.setStock(producto.getStock() - detalle.getCantidad());
            }
        }

        venta.setEstado(nuevoEstado);
        return ventaRepository.save(venta); // Guardamos cambios explícitamente
    }

}
