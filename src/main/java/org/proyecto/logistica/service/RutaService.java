package org.proyecto.logistica.service;

import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    public Ruta iniciarRuta(Long idRuta) {
        Optional<Ruta> rutaOpt = rutaRepository.findById(idRuta);

        if (rutaOpt.isPresent()) {
            Ruta ruta = rutaOpt.get();
            if ("pendiente".equals(ruta.getEstado())) {
                ruta.setEstado("en_progreso");
                ruta.setStart_time(LocalDateTime.now());
                return rutaRepository.save(ruta);
            } else {
                throw new IllegalStateException("Solo se pueden iniciar rutas pendientes");
            }
        }
        throw new IllegalStateException("La ruta no existe");
    }

    public Ruta finalizarRuta(Long idRuta) {
        Optional<Ruta> rutaOpt = rutaRepository.findById(idRuta);

        if (rutaOpt.isPresent()) {
            Ruta ruta = rutaOpt.get();

            if ("en_progreso".equals(ruta.getEstado())) {
                ruta.setEstado("completada");
                ruta.setStart_time(null);
                return rutaRepository.save(ruta);
            } else {
                throw new IllegalStateException("La ruta debe estar en progreso para finalizarla");
            }
        }
        throw new IllegalStateException("La ruta no existe");
    }
}
