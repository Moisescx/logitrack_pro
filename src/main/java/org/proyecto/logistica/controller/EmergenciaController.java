package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Historial;
import org.proyecto.logistica.repository.HistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class EmergenciaController {

    @Autowired
    private HistorialRepository historialRepository;

    public static class AlertaMensaje{
        public String chofer;
        public String patente;
        public String tipoProblema;
    }

    @MessageMapping("/alerta")
    @SendTo("/topic/emergencias")
    public AlertaMensaje procesarEmergencia(AlertaMensaje alerta) {
        Historial registro = new Historial();
        registro.setUsuario(alerta.chofer);
        registro.setAccion("EMERGENCIA_REPORTADA");
        registro.setDetalle("Falla critica: " + alerta.tipoProblema + " en vehiculo " + alerta.patente);
        registro.setFecha(LocalDateTime.now());
        historialRepository.save(registro);
        return alerta;
    }
}
