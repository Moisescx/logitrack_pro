package org.proyecto.logistica.controller;

import org.proyecto.logistica.dto.UbicacionDTO;
import org.proyecto.logistica.model.UbicacionHistorial;
import org.proyecto.logistica.service.GpsService;
import org.proyecto.logistica.service.UbicacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LocationController {

    @Autowired
    private UbicacionService ubicacionService;

    /**
     * Recibe ubicación GPS del chofer/camión en tiempo real
     * Cliente envía a: /app/actualizar-ubicacion
     * Servidor broadcast a: /topic/ubicaciones
     */
    @MessageMapping("/actualizar-ubicacion")
    @SendTo("/topic/ubicaciones")
    public UbicacionDTO actualizarUbicacion(UbicacionDTO ubicacion) throws Exception {
        // Validar coordenadas
        if (!GpsService.validarCoordenadas(ubicacion.getLatitud(), ubicacion.getLongitud())) {
            throw new IllegalArgumentException("Coordenadas GPS inválidas");
        }

        // Validar velocidad si se proporciona
        if (ubicacion.getVelocidad() != null && !GpsService.validarVelocidad(ubicacion.getVelocidad())) {
            throw new IllegalArgumentException("Velocidad fuera de rango permitido");
        }

        // Guardar ubicación en la base de datos
        UbicacionHistorial historial = ubicacionService.guardarUbicacion(
            ubicacion.getCamionId(),
            ubicacion.getLatitud(),
            ubicacion.getLongitud(),
            ubicacion.getVelocidad() != null ? ubicacion.getVelocidad() : 0.0,
            ubicacion.getPrecision()
        );

        // Preparar respuesta con timestamp del servidor
        ubicacion.setTimestamp(historial.getTimestamp().toString());

        return ubicacion;
    }

    /**
     * Recibe ubicación con ruta específica
     * Cliente envía a: /app/actualizar-ubicacion-ruta
     * Servidor broadcast a: /topic/ubicaciones-rutas
     */
    @MessageMapping("/actualizar-ubicacion-ruta")
    @SendTo("/topic/ubicaciones-rutas")
    public UbicacionDTO actualizarUbicacionRuta(UbicacionDTO ubicacion) throws Exception {
        // Validar coordenadas
        if (!GpsService.validarCoordenadas(ubicacion.getLatitud(), ubicacion.getLongitud())) {
            throw new IllegalArgumentException("Coordenadas GPS inválidas");
        }

        // Guardamos ubicación (requiere rutaId en el DTO)
        UbicacionHistorial historial = ubicacionService.guardarUbicacion(
            ubicacion.getCamionId(),
            ubicacion.getLatitud(),
            ubicacion.getLongitud(),
            ubicacion.getVelocidad() != null ? ubicacion.getVelocidad() : 0.0,
            ubicacion.getPrecision()
        );

        ubicacion.setTimestamp(historial.getTimestamp().toString());
        return ubicacion;
    }
}
