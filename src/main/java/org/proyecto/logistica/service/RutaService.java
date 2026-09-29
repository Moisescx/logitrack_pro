package org.proyecto.logistica.service;

import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RutaService {

    public static final long MAX_CONDUCCION_CONTINUA_SEGUNDOS = 5 * 60 * 60;
    public static final long DESCANSO_OBLIGATORIO_SEGUNDOS = 2 * 60 * 60;
    public static final long DESCANSO_DIARIO_SEGUNDOS = 8 * 60 * 60;

    @Autowired
    private RutaRepository rutaRepository;

    public Ruta iniciarRuta(Long idRuta) {
        Optional<Ruta> rutaOpt = rutaRepository.findById(idRuta);

        if (rutaOpt.isPresent()) {
            Ruta ruta = rutaOpt.get();
            if ("pendiente".equals(ruta.getEstado())) {
                ruta.setEstado("en_progreso");
                ruta.setStart_time(LocalDateTime.now());
                ruta.setInicioDescanso(null);
                ruta.setTiempoDescansoSegundos(0L);
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
                ruta.setInicioDescanso(null);
                ruta.setTiempoDescansoSegundos(0L);
                return rutaRepository.save(ruta);
            } else {
                throw new IllegalStateException("La ruta debe estar en progreso para finalizarla");
            }
        }
        throw new IllegalStateException("La ruta no existe");
    }

    public Map<String, Object> obtenerEstadoDescanso(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalStateException("La ruta no existe"));

        LocalDateTime ahora = LocalDateTime.now();
        long descansoAcumulado = ruta.getTiempoDescansoSegundos() == null
                ? 0L : ruta.getTiempoDescansoSegundos();
        long descansoActual = ruta.getInicioDescanso() == null
                ? 0L : Math.max(0L, Duration.between(ruta.getInicioDescanso(), ahora).getSeconds());
        long conduccion = 0L;

        if (ruta.getStart_time() != null && "en_progreso".equals(ruta.getEstado())) {
            long transcurrido = Math.max(0L, Duration.between(ruta.getStart_time(), ahora).getSeconds());
            conduccion = Math.max(0L, transcurrido - descansoAcumulado - descansoActual);
        }

        boolean enDescanso = ruta.getInicioDescanso() != null;
        boolean descansoRequerido = conduccion >= MAX_CONDUCCION_CONTINUA_SEGUNDOS && !enDescanso;
        long restanteConduccion = Math.max(0L, MAX_CONDUCCION_CONTINUA_SEGUNDOS - conduccion);
        long restanteDescanso = enDescanso
                ? Math.max(0L, DESCANSO_OBLIGATORIO_SEGUNDOS - descansoActual)
                : 0L;

        Map<String, Object> estado = new HashMap<>();
        estado.put("rutaId", ruta.getId());
        estado.put("estadoRuta", ruta.getEstado());
        estado.put("conduccionSegundos", conduccion);
        estado.put("restanteConduccionSegundos", restanteConduccion);
        estado.put("descansoRequerido", descansoRequerido);
        estado.put("enDescanso", enDescanso);
        estado.put("descansoSegundos", descansoActual);
        estado.put("restanteDescansoSegundos", restanteDescanso);
        estado.put("descansoMinimoDiarioSegundos", DESCANSO_DIARIO_SEGUNDOS);
        return estado;
    }

    public Map<String, Object> iniciarDescanso(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalStateException("La ruta no existe"));
        Map<String, Object> estado = obtenerEstadoDescanso(idRuta);

        if (!"en_progreso".equals(ruta.getEstado())) {
            throw new IllegalStateException("La ruta no está en progreso");
        }
        if (ruta.getInicioDescanso() != null) {
            throw new IllegalStateException("El descanso ya está en curso");
        }
        if (!(Boolean) estado.get("descansoRequerido")) {
            throw new IllegalStateException("Aún no corresponde el descanso obligatorio");
        }

        ruta.setInicioDescanso(LocalDateTime.now());
        rutaRepository.save(ruta);
        return obtenerEstadoDescanso(idRuta);
    }

    public Map<String, Object> finalizarDescanso(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
                .orElseThrow(() -> new IllegalStateException("La ruta no existe"));
        if (ruta.getInicioDescanso() == null) {
            throw new IllegalStateException("No hay un descanso en curso");
        }

        long duracion = Math.max(0L, Duration.between(ruta.getInicioDescanso(), LocalDateTime.now()).getSeconds());
        if (duracion < DESCANSO_OBLIGATORIO_SEGUNDOS) {
            throw new IllegalStateException("El descanso debe durar al menos 2 horas");
        }

        long acumulado = ruta.getTiempoDescansoSegundos() == null ? 0L : ruta.getTiempoDescansoSegundos();
        ruta.setTiempoDescansoSegundos(acumulado + duracion);
        ruta.setInicioDescanso(null);
        rutaRepository.save(ruta);
        return obtenerEstadoDescanso(idRuta);
    }
}
