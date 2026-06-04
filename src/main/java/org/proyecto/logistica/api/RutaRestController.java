package org.proyecto.logistica.api;

import org.proyecto.logistica.dto.ApiResponse;
import org.proyecto.logistica.dto.RutaDTO;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/rutas")
@CrossOrigin(origins = "*")
public class RutaRestController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private RutaService rutaService;

    /**
     * Obtener todas las rutas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RutaDTO>>> obtenerTodas() {
        List<Ruta> rutas = rutaRepository.findAll();
        List<RutaDTO> rutasDTO = rutas.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutas obtenidas", rutasDTO));
    }

    /**
     * Obtener ruta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaDTO>> obtenerPorId(@PathVariable Long id) {
        try {
            Ruta ruta = rutaRepository.findById(id).orElseThrow();
            return ResponseEntity.ok(new ApiResponse<>(true, "Ruta encontrada", convertirADTO(ruta)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }

    /**
     * Obtener rutas por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<RutaDTO>>> obtenerPorEstado(@PathVariable String estado) {
        List<Ruta> rutas = rutaRepository.findByEstado(estado);
        List<RutaDTO> rutasDTO = rutas.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Rutas por estado", rutasDTO));
    }

    /**
     * Crear nueva ruta
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RutaDTO>> crearRuta(@RequestBody Ruta ruta) {
        try {
            Ruta rutaGuardada = rutaRepository.save(ruta);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Ruta creada", convertirADTO(rutaGuardada)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error al crear ruta"));
        }
    }

    /**
     * Actualizar ruta
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RutaDTO>> actualizarRuta(@PathVariable Long id, @RequestBody Ruta rutaActualizada) {
        try {
            Ruta ruta = rutaRepository.findById(id).orElseThrow();
            ruta.setOrigen(rutaActualizada.getOrigen());
            ruta.setDestino(rutaActualizada.getDestino());
            ruta.setEstado(rutaActualizada.getEstado());
            
            Ruta rutaGuardada = rutaRepository.save(ruta);
            return ResponseEntity.ok(new ApiResponse<>(true, "Ruta actualizada", convertirADTO(rutaGuardada)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }

    /**
     * Iniciar ruta
     */
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<ApiResponse<RutaDTO>> iniciarRuta(@PathVariable Long id) {
        try {
            Ruta rutaIniciada = rutaService.iniciarRuta(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Ruta iniciada", convertirADTO(rutaIniciada)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }

    /**
     * Finalizar ruta
     */
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ApiResponse<RutaDTO>> finalizarRuta(@PathVariable Long id) {
        try {
            Ruta rutaFinalizada = rutaService.finalizarRuta(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Ruta finalizada", convertirADTO(rutaFinalizada)));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }

    /**
     * Eliminar ruta
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarRuta(@PathVariable Long id) {
        try {
            rutaRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Ruta eliminada"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }

    private RutaDTO convertirADTO(Ruta ruta) {
        return new RutaDTO(
            ruta.getId(),
            ruta.getOrigen(),
            ruta.getDestino(),
            ruta.getEstado(),
            ruta.getCamion() != null ? ruta.getCamion().getId() : null,
            ruta.getDistanciaRecorrida(),
            ruta.getStart_time() != null ? ruta.getStart_time().toString() : null,
            ruta.getLatitudActual(),
            ruta.getLongitudActual()
        );
    }
}
