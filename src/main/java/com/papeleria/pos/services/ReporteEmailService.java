package com.papeleria.pos.services;

import com.papeleria.pos.enums.TipoItem;
import com.papeleria.pos.models.CorteCaja;
import com.papeleria.pos.models.DetalleVenta;
import com.papeleria.pos.models.Venta;
import com.papeleria.pos.repositories.CorteCajaRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORTANTE
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteEmailService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private TemplateEngine templateEngine;
    @Autowired private CorteCajaRepository corteCajaRepository;
    @Autowired private VentaService ventaService;
    @Autowired private DetalleVentaService detalleVentaService;
    @Autowired private ProductoService productoService;
    @Autowired private ServicioService servicioService;

    // MAGIA SENIOR: Le damos su propia conexión a la BD al hilo asíncrono
    @Async
    @Transactional(readOnly = true)
    public void enviarReporteSemanalHtml(String[] correosDestino, LocalDateTime inicio, LocalDateTime fin) {
        try {
            // 1. Recopilar Cortes de Caja de la semana
            List<CorteCaja> cortes = corteCajaRepository.findByFechaCierreBetween(inicio, fin);

            // 2. Recopilar Ventas de la semana y traducir los nombres de los ítems
            List<Venta> ventas = ventaService.obtenerVentasPorRango(inicio, fin);
            List<Map<String, Object>> ventasDTO = new ArrayList<>();

            for (Venta v : ventas) {
                Map<String, Object> vMap = new HashMap<>();
                vMap.put("id", v.getId());
                vMap.put("fecha", v.getFechaHora());
                vMap.put("metodoPago", v.getMetodoPago().name());
                vMap.put("estado", v.getEstado().name());
                vMap.put("total", v.getTotal());
                // Como tenemos @Transactional(readOnly = true), esta línea ya NO explotará:
                vMap.put("cajero", v.getUsuario().getUsername());

                List<DetalleVenta> detalles = detalleVentaService.obtenerDetallesPorVenta(v);
                List<Map<String, Object>> detallesDTO = new ArrayList<>();

                for (DetalleVenta d : detalles) {
                    String nombreItem = "Desconocido";
                    if (d.getTipoItem() == TipoItem.PRODUCTO) {
                        nombreItem = productoService.buscarPorId(d.getIdItem()).map(p -> p.getNombre()).orElse("Prod. Eliminado");
                    } else {
                        nombreItem = servicioService.buscarPorId(d.getIdItem()).map(s -> s.getNombre()).orElse("Serv. Eliminado");
                    }
                    Map<String, Object> dMap = new HashMap<>();
                    dMap.put("nombre", nombreItem);
                    dMap.put("tipo", d.getTipoItem().name());
                    dMap.put("cantidad", d.getCantidad());
                    dMap.put("precio", d.getPrecioUnitario());
                    dMap.put("subtotal", d.getSubtotal());
                    detallesDTO.add(dMap);
                }
                vMap.put("detalles", detallesDTO);
                ventasDTO.add(vMap);
            }

            // 3. Inyectar datos a Thymeleaf
            Context context = new Context();
            context.setVariable("inicio", inicio);
            context.setVariable("fin", fin);
            context.setVariable("cortes", cortes);
            context.setVariable("ventas", ventasDTO);

            // 4. Renderizar la plantilla HTML
            String contenidoHtml = templateEngine.process("email-reporte", context);

            // 5. Enviar Correo MIME (HTML)
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("no-reply@tupapeleria.com");
            helper.setTo(correosDestino);
            helper.setSubject("📊 Reporte Financiero Semanal - POS Papelería");
            helper.setText(contenidoHtml, true); // true indica que es HTML

            mailSender.send(message);
            System.out.println("[EMAIL HTML] Reporte semanal enviado exitosamente.");

        } catch (Exception e) {
            System.err.println("[EMAIL ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}