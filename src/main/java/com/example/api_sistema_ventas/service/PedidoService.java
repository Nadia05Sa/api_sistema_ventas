package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.DetallePedido;
import com.example.api_sistema_ventas.model.Pedido;
import com.example.api_sistema_ventas.model.Producto;
import com.example.api_sistema_ventas.model.Proveedor;
import com.example.api_sistema_ventas.repository.PedidoRepository;
import com.example.api_sistema_ventas.repository.ProductoRepository;
import com.example.api_sistema_ventas.repository.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public Pedido crearPedido(Pedido pedido) {
        // Cargar proveedores y productos completos en cada detalle
        if (pedido.getDetalles() != null) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                // Cargar proveedor completo
                Integer idProveedor = detalle.getProveedor().getId();
                Proveedor proveedorCompleto = proveedorRepository.findById(idProveedor)
                        .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + idProveedor));
                detalle.setProveedor(proveedorCompleto);

                // Cargar producto completo
                Integer idProducto = detalle.getProducto().getId();
                Producto productoCompleto = productoRepository.findById(idProducto)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + idProducto));
                detalle.setProducto(productoCompleto);
            }
        }

        // Guarda el pedido con detalles actualizados
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Agrupar detalles por proveedor para enviar correos
        Map<Integer, List<DetallePedido>> detallesPorProveedor = new HashMap<>();
        if (pedidoGuardado.getDetalles() != null) {
            for (DetallePedido detalle : pedidoGuardado.getDetalles()) {
                Integer idProveedor = detalle.getProveedor().getId();
                detallesPorProveedor.computeIfAbsent(idProveedor, k -> new ArrayList<>()).add(detalle);
            }
        }

        log.info("Proveedores a notificar: {}", detallesPorProveedor.size());

        detallesPorProveedor.forEach((idProveedor, detalles) -> {
            Proveedor proveedor = detalles.get(0).getProveedor();
            try {
                log.info("Enviando correo a: {}", proveedor.getCorreo());
                enviarCorreoPedido(proveedor.getCorreo(), pedidoGuardado, detalles);
                log.info("Correo enviado correctamente a: {}", proveedor.getCorreo());
            } catch (Exception e) {
                log.error("Error enviando correo al proveedor: {}", proveedor.getCorreo(), e);
            }
        });

        return pedidoGuardado;
    }

    private void enviarCorreoPedido(String correo, Pedido pedido, List<DetallePedido> detalles) {
        log.info("Preparando contenido del correo para: {}", correo);

        StringBuilder contenido = new StringBuilder();
        contenido.append("Estimado proveedor,\n\nMe pongo en contacto con usted en representación de SOLU EXTINTORES" +
                " para solicitar información y cotización acerca de los siguientes productos:\n\n");

        detalles.forEach(d -> contenido.append(" Producto: ")
                .append(d.getProducto().getNombre())
                .append(", | Cantidad: ").append(d.getCantidad())
                .append("\n")
        );

        contenido.append("\nObservaciones: ").append(
                pedido.getObservaciones() != null ? pedido.getObservaciones() : "Ninguna"
        );
        contenido.append("\n\nGracias por su colaboración.");

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Solicitud de " + pedido.getAsunto());
        mensaje.setText(contenido.toString());

        javaMailSender.send(mensaje);

        log.info("Correo enviado a: {}", correo);
    }
}
