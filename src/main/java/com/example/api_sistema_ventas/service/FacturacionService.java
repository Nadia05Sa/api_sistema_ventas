package com.example.api_sistema_ventas.service;

import com.example.api_sistema_ventas.dto.FacturacionDTO;
import com.example.api_sistema_ventas.dto.FacturacionCreateDTO;
import com.example.api_sistema_ventas.exception.FacturaNotFoundException;
import com.example.api_sistema_ventas.model.Facturacion;
import com.example.api_sistema_ventas.model.Venta;
import com.example.api_sistema_ventas.repository.FacturacionRepository;
import com.example.api_sistema_ventas.repository.VentaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class FacturacionService {

    @Autowired
    private FacturacionRepository facturacionRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Value("${facturacion.api.url:https://api.facturama.mx/api-lite}")
    private String apiUrl;

    @Value("${facturacion.api.username}")
    private String apiUsername;

    @Value("${facturacion.api.password}")
    private String apiPassword;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Definir fuentes consistentes
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
    private static final Font REGULAR_FONT = new Font(Font.FontFamily.HELVETICA, 9);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8);
    private static final Font TINY_FONT = new Font(Font.FontFamily.HELVETICA, 7);

    @Transactional
    public FacturacionDTO crearYTimbrarFactura(FacturacionCreateDTO createDTO) {
        try {
            Optional<Venta> ventaOpt = ventaRepository.findById(createDTO.getVentaId());
            if (!ventaOpt.isPresent()) {
                throw new FacturaNotFoundException("Venta no encontrada");
            }
            Venta venta = ventaOpt.get();

            Facturacion factura = new Facturacion();
            factura.setVenta(venta);
            factura.setRfc(createDTO.getRfc());
            factura.setFechaEmision(LocalDateTime.now());
            factura.setFechaVencimiento(LocalDateTime.now().plusDays(30));
            factura.setIva(16);
            factura.setObservaciones(createDTO.getObservaciones());
            factura.setUsoCfdi(createDTO.getUsoCfdi() != null ? createDTO.getUsoCfdi() : "P01");
            factura.setFormaPago(createDTO.getFormaPago() != null ? createDTO.getFormaPago() : "03");
            factura.setTipoComprobante("I");
            factura.setRegimenFiscalEmisor("601");
            factura.setSerie("F");

            factura = facturacionRepository.save(factura);
            factura.setFolio(String.valueOf(factura.getId()));
            factura = facturacionRepository.save(factura);

            String xmlTimbrado = timbrarFactura(factura);

            return convertirADTO(factura);

        } catch (Exception e) {
            throw new RuntimeException("Error al crear y timbrar factura: " + e.getMessage());
        }
    }

    private String timbrarFactura(Facturacion factura) {
        try {
            Map<String, Object> facturaData = new HashMap<>();

            Map<String, Object> emisor = new HashMap<>();
            emisor.put("Rfc", "XAXX010101000");
            emisor.put("Name", "Empresa Demo");
            emisor.put("FiscalRegime", factura.getRegimenFiscalEmisor());
            facturaData.put("Issuer", emisor);

            Map<String, Object> receptor = new HashMap<>();
            receptor.put("Rfc", factura.getRfc());
            receptor.put("Name", "Cliente");
            receptor.put("CfdiUse", factura.getUsoCfdi());
            facturaData.put("Receiver", receptor);

            Map<String, Object>[] conceptos = new Map[1];
            Map<String, Object> concepto = new HashMap<>();
            concepto.put("ProductCode", "01010101");
            concepto.put("IdentificationNumber", "001");
            concepto.put("Description", "Venta - ID: " + factura.getVenta().getId());
            concepto.put("Unit", "PZA");
            concepto.put("UnitCode", "H87");
            concepto.put("UnitPrice", factura.getVenta().getTotal().doubleValue());
            concepto.put("Quantity", 1);
            concepto.put("Subtotal", factura.getVenta().getTotal().doubleValue());
            concepto.put("Total", factura.getVenta().getTotal().doubleValue());

            Map<String, Object>[] impuestos = new Map[1];
            Map<String, Object> iva = new HashMap<>();
            iva.put("Name", "IVA");
            iva.put("Rate", 0.16);
            iva.put("IsRetention", false);
            impuestos[0] = iva;
            concepto.put("Taxes", impuestos);

            conceptos[0] = concepto;
            facturaData.put("Items", conceptos);

            facturaData.put("Folio", factura.getFolio());
            facturaData.put("Series", factura.getSerie());
            facturaData.put("Currency", "MXN");
            facturaData.put("Date", factura.getFechaEmision().toString());
            facturaData.put("PaymentForm", factura.getFormaPago());
            facturaData.put("PaymentMethod", "PUE");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(apiUsername, apiPassword);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(facturaData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/cfdis", request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                return responseNode.get("Complement").asText();
            } else {
                throw new RuntimeException("Error en el timbrado: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("Warning: Timbrado falló, continuando sin timbrar: " + e.getMessage());
            return "XML_SIMULADO_PARA_DEMO";
        }
    }

    public FacturacionDTO obtenerFactura(Integer id) {
        Optional<Facturacion> facturaOpt = facturacionRepository.findById(id);
        return facturaOpt.map(this::convertirADTO).orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada"));
    }

    public FacturacionDTO obtenerFacturaPorVenta(Integer ventaId) {
        Optional<Facturacion> facturaOpt = facturacionRepository.findByVentaId(ventaId);
        return facturaOpt.map(this::convertirADTO).orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada para la venta ID: " + ventaId));
    }

    public byte[] generarPDF(Integer facturaId) {
        try {
            Optional<Facturacion> facturaOpt = facturacionRepository.findById(facturaId);
            if (!facturaOpt.isPresent()) {
                throw new FacturaNotFoundException("Factura no encontrada");
            }

            Facturacion factura = facturaOpt.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Encabezado con logo y datos del emisor
            addEncabezadoConLogo(document);

            // Espacio
            document.add(new Paragraph(" ", SMALL_FONT));

            // Tabla de información básica (exacta como en el PDF)
            addInformacionBasicaExacta(document, factura);

            // Espacio
            document.add(new Paragraph(" ", SMALL_FONT));

            // Título de conceptos
            addTituloConceptos(document);

            // Tabla de conceptos principal
            addTablaConceptosPrincipal(document, factura);

            // Conceptos detallados
            addConceptosDetallados(document, factura);

            // Espacio
            document.add(new Paragraph(" ", SMALL_FONT));

            // Información de pago y totales
            addInformacionPagoYTotales(document, factura);

            // Espacio
            document.add(new Paragraph(" ", SMALL_FONT));

            // Sellos digitales
            addSellosDigitales(document);

            // Pie de página
            addPieDePagenaExacto(document, factura);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    private void addEncabezadoConLogo(Document document) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 2});

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

        // Información del emisor (celda derecha)
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.addElement(new Paragraph("Nombre emisor: EMPRESA DEMO", BOLD_FONT));
        infoCell.addElement(new Paragraph("RFC emisor: XAXX010101000", REGULAR_FONT));
        headerTable.addCell(infoCell);

        document.add(headerTable);
    }

    private void addInformacionBasicaExacta(Document document, Facturacion factura) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});

        // Columna izquierda
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(0);

        leftCell.addElement(createInfoParagraph("Folio:", factura.getFolio()));
        leftCell.addElement(createInfoParagraph("RFC receptor:", factura.getRfc()));
        leftCell.addElement(createInfoParagraph("Nombre receptor:", "CLIENTE"));
        leftCell.addElement(createInfoParagraph("Código postal del receptor:", "11000"));
        leftCell.addElement(createInfoParagraph("Régimen fiscal receptor:", "Personas Físicas con Actividades Empresariales y Profesionales"));
        leftCell.addElement(createInfoParagraph("Uso CFDI:", getUsoCfdiDescripcion(factura.getUsoCfdi())));

        // Columna derecha
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(0);

        rightCell.addElement(createInfoParagraph("No. de serie del CSD:", "00001000000510421979"));
        rightCell.addElement(createInfoParagraph("Serie:", factura.getSerie()));
        rightCell.addElement(createInfoParagraph("Código postal, fecha y hora de emisión:",
                "11000 " + factura.getFechaEmision().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        rightCell.addElement(createInfoParagraph("Efecto de comprobante:", "Ingreso"));
        rightCell.addElement(createInfoParagraph("Régimen fiscal:", "Régimen Simplificado de Confianza"));
        rightCell.addElement(createInfoParagraph("Exportación:", "No aplica"));

        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);

        document.add(infoTable);
    }

    private void addTituloConceptos(Document document) throws DocumentException {
        Paragraph titulo = new Paragraph("Conceptos", BOLD_FONT);
        document.add(titulo);
        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 3)));
    }

    private void addTablaConceptosPrincipal(Document document, Facturacion factura) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 0.8f, 0.6f, 0.8f, 0.6f, 0.8f, 0.8f, 0.6f, 1f});

        // Headers
        addConceptHeaderCell(table, "Clave del producto y/o servicio");
        addConceptHeaderCell(table, "No. identificación");
        addConceptHeaderCell(table, "Cantidad");
        addConceptHeaderCell(table, "Clave de unidad");
        addConceptHeaderCell(table, "Unidad");
        addConceptHeaderCell(table, "Valor unitario");
        addConceptHeaderCell(table, "Importe");
        addConceptHeaderCell(table, "Descuento");
        addConceptHeaderCell(table, "Objeto impuesto");

        // Calcular valores
        double total = factura.getVenta().getTotal().doubleValue();
        double subtotal = total / 1.16;

        // Fila de datos
        addConceptDataCell(table, "01010101");
        addConceptDataCell(table, "");
        addConceptDataCell(table, "1.00");
        addConceptDataCell(table, "H87");
        addConceptDataCell(table, "Pieza");
        addConceptDataCell(table, String.format("%.2f", subtotal));
        addConceptDataCell(table, String.format("%.2f", subtotal));
        addConceptDataCell(table, "");
        addConceptDataCell(table, "Sí objeto de impuesto.");

        document.add(table);
    }

    private void addConceptosDetallados(Document document, Facturacion factura) throws DocumentException {
        // Descripción
        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 2)));
        Paragraph descripcion = new Paragraph("Descripción VENTA - ID: " + factura.getVenta().getId(), BOLD_FONT);
        document.add(descripcion);

        // Tabla de impuestos
        double total = factura.getVenta().getTotal().doubleValue();
        double base = total / 1.16;
        double iva = total - base;

        PdfPTable impuestosTable = new PdfPTable(6);
        impuestosTable.setWidthPercentage(100);
        impuestosTable.setWidths(new float[]{1f, 1f, 1f, 1f, 1f, 1f});

        // Headers de impuestos
        addConceptHeaderCell(impuestosTable, "Impuesto");
        addConceptHeaderCell(impuestosTable, "Tipo");
        addConceptHeaderCell(impuestosTable, "Base");
        addConceptHeaderCell(impuestosTable, "Tipo Factor");
        addConceptHeaderCell(impuestosTable, "Tasa o Cuota");
        addConceptHeaderCell(impuestosTable, "Importe");

        // Datos de impuestos
        addConceptDataCell(impuestosTable, "IVA");
        addConceptDataCell(impuestosTable, "Traslado");
        addConceptDataCell(impuestosTable, String.format("%.2f", base));
        addConceptDataCell(impuestosTable, "Tasa");
        addConceptDataCell(impuestosTable, "16.00%");
        addConceptDataCell(impuestosTable, String.format("%.2f", iva));

        document.add(impuestosTable);

        // Línea de números de pedimento
        document.add(new Paragraph(" ", new Font(Font.FontFamily.HELVETICA, 2)));
        Paragraph pedimento = new Paragraph("Número de pedimento                                     Número de cuenta predial", TINY_FONT);
        document.add(pedimento);
    }

    private void addInformacionPagoYTotales(Document document, Facturacion factura) throws DocumentException {
        // Información de moneda y pago
        document.add(new Paragraph("Moneda: Peso Mexicano", REGULAR_FONT));
        document.add(new Paragraph("Forma de pago: " + getFormaPagoDescripcion(factura.getFormaPago()), REGULAR_FONT));
        document.add(new Paragraph("Método de pago: Pago en una sola exhibición", REGULAR_FONT));

        document.add(new Paragraph(" ", SMALL_FONT));

        // Tabla de totales
        PdfPTable totalesTable = new PdfPTable(2);
        totalesTable.setWidthPercentage(40);
        totalesTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        double total = factura.getVenta().getTotal().doubleValue();
        double subtotal = total / 1.16;
        double iva = total - subtotal;

        // Subtotal
        addTotalRow(totalesTable, "Subtotal", "$ " + String.format("%.2f", subtotal));

        // IVA
        addTotalRow(totalesTable, "Impuestos trasladados IVA 16.00%", "$ " + String.format("%.2f", iva));

        // Total
        addTotalRow(totalesTable, "Total", "$ " + String.format("%.2f", total));

        document.add(totalesTable);
    }

    private void addSellosDigitales(Document document) throws DocumentException {
        // Sello digital del CFDI
        Paragraph selloCfdiTitle = new Paragraph("Sello digital del CFDI:", BOLD_FONT);
        document.add(selloCfdiTitle);

        String selloCfdi = "q72TH6x7TuJhpUaxeMNa74m37rwVMHrbjt6n+8/fMTspSuvb7O23fAj5st9wBIJxVlYccrTFlPL33AcVqBs6VvFvh1QQIjL6PhHf4Vtrw64QmT58T4nhCQsdhdHE8t9lDcGy3BWG6OUbxOU0Vpx\n"
                + "yWOt6TE+xb2d2IcMkgcDbcHKbE9RLExAPZ2Y7Pi6/W++u4QsXfwsfWjMElxdM3IYA1L3kZkOcw3AzvRCa4f9VcE0wU8up3KfV7rLuCz3q5oA1oBnnP0l7snAb6j8cXgHPufVhY2M/MUno350f\n"
                + "sHYBoBweTyLZu+oqQ00l31It1A6HtAuTYd60xYwosK8KBK/7HA==";

        Paragraph selloCfdiParagraph = new Paragraph(selloCfdi, new Font(Font.FontFamily.HELVETICA, 6));
        document.add(selloCfdiParagraph);

        document.add(new Paragraph(" ", TINY_FONT));

        // Sello digital del SAT
        Paragraph selloSatTitle = new Paragraph("Sello digital del SAT:", BOLD_FONT);
        document.add(selloSatTitle);

        String selloSat = "A4MPRFbcHEdEYFZP/7/W83OV34HgVYVEsDF2Cql0YTXvEiyvj0sR5//TPci9upZ0G3+KoP2d6LzEzZOm9rZzOazLiUW/imdBeqs98cGPMG6/bfMHAtnUoY1esUaaNplsHQrhOaK4MGeZYC\n"
                + "XB80mwBWwCb/EV4ob1vLF/gTVCKRkGXWas/12XJ+Sltsh5+mNS9Y7R3E1MAr/LjptZRZHjhl5lexspcppmiec6pZUjwaK5dpi0IRRWpN+6FEDPEKx8Q2H9ldKOTkCqYo4h2fEO6459Qm+jhd\n"
                + "Ce3Neg9HDpMjBsXniOaENfb1KRW+Xz4k0DxrZZylI3gi78S0Z7Iy2VaA==";

        Paragraph selloSatParagraph = new Paragraph(selloSat, new Font(Font.FontFamily.HELVETICA, 6));
        document.add(selloSatParagraph);

        document.add(new Paragraph(" ", TINY_FONT));

        // Cadena original
        Paragraph cadenaTitle = new Paragraph("Cadena Original del complemento de certificación digital del SAT:", BOLD_FONT);
        document.add(cadenaTitle);

        String uuid = java.util.UUID.randomUUID().toString().toUpperCase();
        String fechaCert = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        String cadena = "||1.1|" + uuid + "|" + fechaCert + "|SAT970701NN3|q72TH6x7TuJhpUaxeMNa74m37rwVMHrbjt6n+8/fMTspSuvb7O\n"
                + "23fAj5st9wBIJxVlYccrTFlPL33AcVqBs6VvFvh1QQIjL6PhHf4Vtrw64QmT58T4nhCQsdhdHE8t9lDcGy3BWG6OUbxOU0VpxyWOt6TE+xb2d2IcMkgcDbcH\n"
                + "KbE9RLExAPZ2Y7Pi6/W++u4QsXfwsfWjMElxdM3IYA1L3kZkOcw3AzvRCa4f9VcE0wU8up3KfV7rLuCz3q5oA1oBnnP0l7snAb6j8cXgHPufVhY2M/MUno3\n"
                + "50fsHYBoBweTyLZu+oqQ00l31It1A6HtAuTYd60xYwosK8KBK/7HA==|00001000000705250068||";

        Paragraph cadenaParagraph = new Paragraph(cadena, new Font(Font.FontFamily.HELVETICA, 6));
        document.add(cadenaParagraph);

        document.add(new Paragraph(" ", TINY_FONT));

        // Información adicional del SAT
        document.add(new Paragraph("RFC del proveedor de certificación: SAT970701NN3", REGULAR_FONT));
        document.add(new Paragraph("No. de serie del certificado SAT 00001000000705250068", REGULAR_FONT));
        document.add(new Paragraph("Fecha y hora de certificación: " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), REGULAR_FONT));
    }

    private void addPieDePagenaExacto(Document document, Facturacion factura) throws DocumentException {
        document.add(new Paragraph(" ", SMALL_FONT));

        // Nombre de la empresa
        Paragraph empresa = new Paragraph("EMPRESA DEMO", HEADER_FONT);
        empresa.setAlignment(Element.ALIGN_CENTER);
        document.add(empresa);

        // RFC y folio fiscal
        String uuid = java.util.UUID.randomUUID().toString().toUpperCase();
        Paragraph rfcFolio = new Paragraph("RFC emisor: XAXX010101000    Folio fiscal: " + uuid, REGULAR_FONT);
        rfcFolio.setAlignment(Element.ALIGN_CENTER);
        document.add(rfcFolio);

        document.add(new Paragraph(" ", SMALL_FONT));

        // Leyendas
        Paragraph leyenda1 = new Paragraph("Este documento es una representación impresa de un CFDI", REGULAR_FONT);
        leyenda1.setAlignment(Element.ALIGN_CENTER);
        document.add(leyenda1);

        Paragraph leyenda2 = new Paragraph("El logotipo de esta factura es responsabilidad única y exclusiva de quien la emite, en consecuencia,", SMALL_FONT);
        leyenda2.setAlignment(Element.ALIGN_CENTER);
        document.add(leyenda2);

        Paragraph leyenda3 = new Paragraph("el SAT queda relevado de cualquier obligación que derive de ello.", SMALL_FONT);
        leyenda3.setAlignment(Element.ALIGN_CENTER);
        document.add(leyenda3);

        document.add(new Paragraph(" ", SMALL_FONT));

        Paragraph pagina = new Paragraph("Página 1 de 1", SMALL_FONT);
        pagina.setAlignment(Element.ALIGN_RIGHT);
        document.add(pagina);
    }

    // Métodos auxiliares para crear elementos consistentes
    private Paragraph createInfoParagraph(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, BOLD_FONT));
        p.add(new Chunk(" " + value, REGULAR_FONT));
        p.setSpacingAfter(1f);
        return p;
    }

    private void addConceptHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, SMALL_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addConceptDataCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TINY_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, REGULAR_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, REGULAR_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private String getUsoCfdiDescripcion(String codigo) {
        switch (codigo) {
            case "P01": return "Por definir";
            case "G01": return "Adquisición de mercancías";
            case "G02": return "Devoluciones, descuentos o bonificaciones";
            case "G03": return "Gastos en general";
            default: return "Gastos en general";
        }
    }

    private String getFormaPagoDescripcion(String codigo) {
        switch (codigo) {
            case "01": return "Efectivo";
            case "02": return "Cheque nominativo";
            case "03": return "Transferencia electrónica de fondos";
            case "04": return "Tarjeta de crédito";
            default: return "Efectivo";
        }
    }

    private FacturacionDTO convertirADTO(Facturacion factura) {
        FacturacionDTO dto = new FacturacionDTO();
        dto.setId(factura.getId());
        dto.setRfc(factura.getRfc());
        dto.setFechaEmision(factura.getFechaEmision());
        dto.setFechaVencimiento(factura.getFechaVencimiento());
        dto.setIva(factura.getIva());
        dto.setObservaciones(factura.getObservaciones());
        dto.setUsoCfdi(factura.getUsoCfdi());
        dto.setFormaPago(factura.getFormaPago());
        dto.setTipoComprobante(factura.getTipoComprobante());
        dto.setRegimenFiscalEmisor(factura.getRegimenFiscalEmisor());
        dto.setSerie(factura.getSerie());
        dto.setFolio(factura.getFolio());
        dto.setVentaId(factura.getVenta().getId());
        dto.setTotal(factura.getVenta().getTotal());
        return dto;
    }

    public List<FacturacionDTO> obtenerTodasLasFacturas() {
        List<Facturacion> facturas = facturacionRepository.findAll();
        return facturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}