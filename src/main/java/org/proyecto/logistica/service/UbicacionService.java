package org.proyecto.logistica.service;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.UbicacionHistorial;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UbicacionHistorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UbicacionService {

    @Autowired
    private UbicacionHistorialRepository ubicacionHistorialRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private RutaRepository rutaRepository;

    public UbicacionHistorial guardarUbicacion(Long camionId, Double latitud, Double longitud, Double velocidad, String precision) {
        Camion camion = camionRepository.findById(camionId).orElseThrow(() -> 
            new IllegalArgumentException("Camión no encontrado: " + camionId)
        );

        UbicacionHistorial ubicacion = new UbicacionHistorial(camion, null, latitud, longitud, velocidad);
        ubicacion.setPrecision(precision);

        // Actualizar ubicación actual del camión
        camion.setLatitudActual(latitud);
        camion.setLongitudActual(longitud);
        camion.setVelocidad(velocidad);
        camion.setTiempoUltimaUbicacion(LocalDateTime.now());
        camionRepository.save(camion);

        return ubicacionHistorialRepository.save(ubicacion);
    }

    public UbicacionHistorial guardarUbicacionConRuta(Long camionId, Long rutaId, Double latitud, Double longitud, Double velocidad, String precision) {
        Camion camion = camionRepository.findById(camionId).orElseThrow(() -> 
            new IllegalArgumentException("Camión no encontrado: " + camionId)
        );

        Ruta ruta = rutaRepository.findById(rutaId).orElse(null);

        UbicacionHistorial ubicacion = new UbicacionHistorial(camion, ruta, latitud, longitud, velocidad);
        ubicacion.setPrecision(precision);

        // Actualizar ubicación actual del camión
        camion.setLatitudActual(latitud);
        camion.setLongitudActual(longitud);
        camion.setVelocidad(velocidad);
        camion.setTiempoUltimaUbicacion(LocalDateTime.now());
        camionRepository.save(camion);

        // Actualizar ubicación actual de la ruta si está en progreso
        if (ruta != null && "en_progreso".equals(ruta.getEstado())) {
            ruta.setLatitudActual(latitud);
            ruta.setLongitudActual(longitud);
            ruta.setTiempoActualizacion(LocalDateTime.now());
            rutaRepository.save(ruta);
        }

        return ubicacionHistorialRepository.save(ubicacion);
    }

    public List<UbicacionHistorial> obtenerHistorialCamion(Long camionId) {
        Camion camion = camionRepository.findById(camionId).orElseThrow();
        return ubicacionHistorialRepository.findByCamionOrderByTimestampDesc(camion);
    }

    public List<UbicacionHistorial> obtenerUltimas10Ubicaciones(Long camionId) {
        Camion camion = camionRepository.findById(camionId).orElseThrow();
        return ubicacionHistorialRepository.findTop10ByCamionOrderByTimestampDesc(camion);
    }

    public List<UbicacionHistorial> obtenerHistorialRuta(Long rutaId) {
        Ruta ruta = rutaRepository.findById(rutaId).orElseThrow();
        return ubicacionHistorialRepository.findByRutaOrderByTimestampDesc(ruta);
    }
}
