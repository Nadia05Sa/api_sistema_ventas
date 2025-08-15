package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.model.DetalleVenta;
import com.example.api_sistema_ventas.model.Direccion;
import com.example.api_sistema_ventas.model.Venta;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotaVentaPdfService {

    private static final Color HEADER_RED = new Color(220, 53, 69);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);

    public ByteArrayInputStream generarPdf(Venta venta) {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado con logo y título
            crearEncabezado(document);

            // Información de la empresa
            crearInfoEmpresa(document);

            // Número de nota dinámico
            crearNumeroNota(document, obtenerNumeroNota(venta));

            // Información del cliente
            crearInfoCliente(document, venta);

            // Tabla de productos
            crearTablaProductos(document, venta);

            // Total y observaciones
            crearTotalYObservaciones(document, venta);

            // Firma
            crearSeccionFirma(document);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void crearEncabezado(Document document) throws DocumentException {
        try {
            // Tabla para el encabezado
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{2, 3, 2});

            // Logo (celda izquierda)
            PdfPCell logoCell = new PdfPCell();
            try {
                Image logo = Image.getInstance(getClass().getResource("/static/images/logo.png"));
                logo.scaleToFit(80, 80);
                logoCell.addElement(logo);
            } catch (Exception e) {
                logoCell.addElement(new Paragraph("LOGO", HEADER_FONT));
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(logoCell);

            // Título central
            PdfPCell titleCell = new PdfPCell();
            Paragraph soluTitle = new Paragraph("SOLU EXTINTORES", new Font(Font.HELVETICA, 18, Font.BOLD | Font.ITALIC));
            soluTitle.setAlignment(Element.ALIGN_CENTER);
            titleCell.addElement(soluTitle);
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(titleCell);

            // Nota de venta (celda derecha)
            PdfPCell notaCell = new PdfPCell();
            Paragraph notaTitle = new Paragraph("NOTA DE VENTA", new Font(Font.HELVETICA, 14, Font.BOLD));
            notaTitle.setAlignment(Element.ALIGN_CENTER);
            notaCell.addElement(notaTitle);
            notaCell.setBorder(Rectangle.BOX);
            notaCell.setBackgroundColor(Color.WHITE);
            notaCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(notaCell);

            document.add(headerTable);
            document.add(new Paragraph(" ", SMALL_FONT));

        } catch (Exception e) {
            // Fallback si hay problemas con el logo
            Paragraph fallbackTitle = new Paragraph("SOLU EXTINTORES - NOTA DE VENTA", TITLE_FONT);
            fallbackTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(fallbackTitle);
            document.add(new Paragraph(" ", SMALL_FONT));
        }
    }

    private void crearInfoEmpresa(Document document) throws DocumentException {
        Paragraph empresaInfo = new Paragraph();
        empresaInfo.setAlignment(Element.ALIGN_CENTER);
        empresaInfo.add(new Chunk("BLANCA CONSTANCIA SOTELO LUGO\n", HEADER_FONT));
        empresaInfo.add(new Chunk("Manzana 2 No. 37 Fraccionamiento la Misión Emiliano Zapata, Mor.\n", NORMAL_FONT));
        empresaInfo.add(new Chunk("www.soluextintores.com  ventas@soluextintores.com\n", NORMAL_FONT));
        empresaInfo.add(new Chunk("Tel: (777)3684245", NORMAL_FONT));

        document.add(empresaInfo);
        document.add(new Paragraph(" ", SMALL_FONT));
    }

    private void crearNumeroNota(Document document, String numero) throws DocumentException {
        Paragraph numeroNota = new Paragraph("No. " + numero, new Font(Font.HELVETICA, 16, Font.BOLD, Color.RED));
        numeroNota.setAlignment(Element.ALIGN_RIGHT);
        document.add(numeroNota);
        document.add(new Paragraph(" ", SMALL_FONT));
    }

    private void crearInfoCliente(Document document, Venta venta) throws DocumentException {
        // Tabla para información del cliente
        PdfPTable clienteTable = new PdfPTable(4);
        clienteTable.setWidthPercentage(100);
        clienteTable.setWidths(new float[]{2, 2, 1, 1});

        // Fila 1: NOMBRE, TELEFONO, FECHA
        clienteTable.addCell(crearCeldaLabel("NOMBRE"));
        clienteTable.addCell(crearCeldaLabel("TELEFONO"));
        clienteTable.addCell(crearCeldaLabel("FECHA"));
        clienteTable.addCell(new PdfPCell()); // Celda vacía

        // Fila 2: Valores
        clienteTable.addCell(crearCeldaValor(venta.getCliente().getNombre() != null ?
                venta.getCliente().getNombre() : ""));
        clienteTable.addCell(crearCeldaValor(venta.getCliente().getTelefono() != null ?
                venta.getCliente().getTelefono() : ""));
        clienteTable.addCell(crearCeldaValor(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        clienteTable.addCell(new PdfPCell()); // Celda vacía

        // Fila 3: DIRECCION
        PdfPCell direccionLabel = crearCeldaLabel("DIRECCION");
        direccionLabel.setColspan(4);
        clienteTable.addCell(direccionLabel);

        // Fila 4: Valor dirección - construir dirección completa
        String direccionCompleta = construirDireccionCompleta(venta.getCliente().getDireccion());
        PdfPCell direccionValor = crearCeldaValor(direccionCompleta);
        direccionValor.setColspan(4);
        clienteTable.addCell(direccionValor);

        // Fila 5: C.P., COLONIA
        clienteTable.addCell(crearCeldaLabel("C.P."));
        PdfPCell coloniaLabel = crearCeldaLabel("COLONIA");
        coloniaLabel.setColspan(3);
        clienteTable.addCell(coloniaLabel);

        // Fila 6: Valores C.P. y Colonia
        String codigoPostal = venta.getCliente().getDireccion() != null &&
                venta.getCliente().getDireccion().getCodigoPostal() != null ?
                venta.getCliente().getDireccion().getCodigoPostal() : "";
        clienteTable.addCell(crearCeldaValor(codigoPostal));

        String colonia = venta.getCliente().getDireccion() != null &&
                venta.getCliente().getDireccion().getColonia() != null ?
                venta.getCliente().getDireccion().getColonia() : "";
        PdfPCell coloniaValor = crearCeldaValor(colonia);
        coloniaValor.setColspan(3);
        clienteTable.addCell(coloniaValor);

        // Fila 7: Empresa o Negocio (usar razón social si existe)
        PdfPCell empresaLabel = crearCeldaLabel("Empresa o Negocio");
        empresaLabel.setColspan(4);
        clienteTable.addCell(empresaLabel);

        String empresaNegocio = venta.getCliente().getRazonSocial() != null ?
                venta.getCliente().getRazonSocial() : "";
        PdfPCell empresaValor = crearCeldaValor(empresaNegocio);
        empresaValor.setColspan(4);
        clienteTable.addCell(empresaValor);

        document.add(clienteTable);
        document.add(new Paragraph(" ", SMALL_FONT));
    }

    private void crearTablaProductos(Document document, Venta venta) throws DocumentException {
        // Encabezado de productos
        PdfPTable headerProductos = new PdfPTable(1);
        headerProductos.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell(new Phrase("PRODUCTO", new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE)));
        headerCell.setBackgroundColor(HEADER_RED);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        headerProductos.addCell(headerCell);
        document.add(headerProductos);

        // Tabla de productos
        PdfPTable productTable = new PdfPTable(4);
        productTable.setWidthPercentage(100);
        productTable.setWidths(new float[]{2, 4, 2, 2});

        // Encabezados de columnas
        productTable.addCell(crearCeldaHeaderProducto("CANTIDAD"));
        productTable.addCell(crearCeldaHeaderProducto("DESCRIPCIÓN"));
        productTable.addCell(crearCeldaHeaderProducto("P. UNITARIO"));
        productTable.addCell(crearCeldaHeaderProducto("IMPORTE"));

        // Datos de productos
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        for (DetalleVenta det : venta.getDetalles()) {
            productTable.addCell(crearCeldaProducto(det.getCantidad().toString()));
            productTable.addCell(crearCeldaProducto(det.getProducto().getNombre()));
            productTable.addCell(crearCeldaProducto(format.format(det.getPrecio())));
            BigDecimal subtotal = det.getPrecio().multiply(new BigDecimal(det.getCantidad()));
            productTable.addCell(crearCeldaProducto(format.format(subtotal)));
        }

        // Agregar filas vacías si es necesario (para completar el diseño)
        int filasVacias = Math.max(0, 12 - venta.getDetalles().size());
        for (int i = 0; i < filasVacias; i++) {
            productTable.addCell(crearCeldaProducto(""));
            productTable.addCell(crearCeldaProducto(""));
            productTable.addCell(crearCeldaProducto(""));
            productTable.addCell(crearCeldaProducto(""));
        }

        document.add(productTable);
    }

    private void crearTotalYObservaciones(Document document, Venta venta) throws DocumentException {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        // Tabla para total
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{3, 1});

        // Celda de importe con letra
        PdfPCell importeLetraCell = new PdfPCell();
        importeLetraCell.addElement(new Paragraph("IMPORTE CON LETRA", SMALL_FONT));
        importeLetraCell.setMinimumHeight(40);
        importeLetraCell.setBorder(Rectangle.BOX);
        totalTable.addCell(importeLetraCell);

        // Celda del total
        PdfPCell totalCell = new PdfPCell();
        totalCell.addElement(new Paragraph("TOTAL: " + format.format(venta.getTotal()), HEADER_FONT));
        totalCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalCell.setBorder(Rectangle.BOX);
        totalTable.addCell(totalCell);

        document.add(totalTable);
        document.add(new Paragraph(" ", SMALL_FONT));

        // Observaciones
        PdfPTable obsTable = new PdfPTable(1);
        obsTable.setWidthPercentage(100);

        PdfPCell obsHeaderCell = new PdfPCell(new Phrase("OBSERVACIONES:", HEADER_FONT));
        obsHeaderCell.setBorder(Rectangle.NO_BORDER);
        obsTable.addCell(obsHeaderCell);

        PdfPCell obsContentCell = new PdfPCell();
        String observaciones = venta.getObservaciones() != null ? venta.getObservaciones() : "";
        obsContentCell.addElement(new Paragraph(observaciones, NORMAL_FONT));
        obsContentCell.setMinimumHeight(100);
        obsContentCell.setBorder(Rectangle.BOX);
        obsTable.addCell(obsContentCell);

        document.add(obsTable);
    }

    private void crearSeccionFirma(Document document) throws DocumentException {
        document.add(new Paragraph(" ", NORMAL_FONT));
        document.add(new Paragraph(" ", NORMAL_FONT));

        Paragraph firma = new Paragraph("Autorizado por: AURELIO SOTELO LUGO", NORMAL_FONT);
        firma.setAlignment(Element.ALIGN_RIGHT);
        document.add(firma);
    }

    // Métodos auxiliares para crear celdas
    private PdfPCell crearCeldaLabel(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, new Font(Font.HELVETICA, 9, Font.BOLD)));
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell crearCeldaValor(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, NORMAL_FONT));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3);
        cell.setMinimumHeight(20);
        return cell;
    }

    private PdfPCell crearCeldaHeaderProducto(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, new Font(Font.HELVETICA, 10, Font.BOLD)));
        cell.setBackgroundColor(HEADER_RED);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private PdfPCell crearCeldaProducto(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, SMALL_FONT));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3);
        cell.setMinimumHeight(15);
        return cell;
    }

    /**
     * Construye la dirección completa a partir del objeto Direccion
     */
    private String construirDireccionCompleta(Direccion direccion) {
        if (direccion == null) {
            return "";
        }

        StringBuilder direccionCompleta = new StringBuilder();

        // Agregar calle y número si existen
        if (direccion.getCalle() != null && !direccion.getCalle().trim().isEmpty()) {
            direccionCompleta.append(direccion.getCalle().trim());
        }

        if (direccion.getNumeroExterior() != null && !direccion.getNumeroExterior().trim().isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(" ");
            }
            direccionCompleta.append(direccion.getNumeroExterior().trim());
        }

        // Agregar colonia si existe
        if (direccion.getColonia() != null && !direccion.getColonia().trim().isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(", ");
            }
            direccionCompleta.append(direccion.getColonia().trim());
        }

        // Agregar ciudad si existe
        if (direccion.getCiudad() != null && !direccion.getCiudad().trim().isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(", ");
            }
            direccionCompleta.append(direccion.getCiudad().trim());
        }

        // Agregar estado si existe
        if (direccion.getEstado() != null && !direccion.getEstado().trim().isEmpty()) {
            if (direccionCompleta.length() > 0) {
                direccionCompleta.append(", ");
            }
            direccionCompleta.append(direccion.getEstado().trim());
        }

        return direccionCompleta.toString();
    }


    private String obtenerNumeroNota(Venta venta) {

        if (venta.getId() != null) {
            return String.format("%03d", venta.getId());
        }
        return "001";
    }
}