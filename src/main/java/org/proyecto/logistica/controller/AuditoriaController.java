package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Historial;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.HistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class AuditoriaController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private HistorialRepository historialRepository;

    @GetMapping("/admin/auditoria")
    public String vistaAuditoria() {
        return "admin/auditoria";
    }

    @GetMapping("/api/exportar/rutas")
    public void exportarRutasCSV(HttpServletResponse response) throws IOException{
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_rutas_completo.csv\"");
        PrintWriter writer = response.getWriter();

        writer.write('\ufeff');

        writer.println("ID_Ruta,Origen,Destino,Estado,Patente_Camion,Nombre_Chofer");

        List<Ruta> rutas = rutaRepository.findAll();

        for (Ruta ruta : rutas){
            String patente = (ruta.getCamion() != null) ? ruta.getCamion().getPatente() : "Sin Asignar";
            String chofer = (ruta.getCamion() != null && ruta.getCamion().getChofer() != null)
                    ? ruta.getCamion().getChofer().getUsername() : "Sin Asignar";

            writer.println(ruta.getId() + "," + ruta.getOrigen() + "," + ruta.getDestino() + "," +
                    ruta.getEstado() + "," + patente + "," + chofer);
        }
    }

    @GetMapping("/api/exportar/logs")
    public void exportarHistorialCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"auditoria_eventos.csv\"");
        PrintWriter writer = response.getWriter();

        writer.write('\ufeff');

        writer.println("Fecha,Usuario,Accion,Detalles");
        List<Historial> todosLosEventos = historialRepository.findAllByOrderByFechaDesc();

        for (Historial evento : todosLosEventos) {

            String accionLimpia = evento.getAccion().replace(",", " ");
            String detalleLimpio = evento.getDetalle().replace(",", " ");

            writer.println(evento.getFecha() + "," +
                    evento.getUsuario() + "," +
                    accionLimpia + "," +
                    detalleLimpio);
        }
    }
}