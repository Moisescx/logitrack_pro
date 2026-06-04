package org.proyecto.logistica.api;

import org.proyecto.logistica.dto.ApiResponse;
import org.proyecto.logistica.dto.CamionDTO;
import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.repository.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/camiones")
@CrossOrigin(origins = "*")
public class CamionRestController {

    @Autowired
    private CamionRepository camionRepository;

    /**
     * Obtener todos los camiones
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CamionDTO>>> obtenerTodos() {
        List<Camion> camiones = camionRepository.findAll();
        List<CamionDTO> camionesDTO = camiones.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Camiones obtenidos", camionesDTO));
    }

    /**
     * Obtener camión por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CamionDTO>> obtenerPorId(@PathVariable Long id) {
        try {
            Camion camion = camionRepository.findById(id).orElseThrow();
            return ResponseEntity.ok(new ApiResponse<>(true, "Camión encontrado", convertirADTO(camion)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    /**
     * Obtener camiones por estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<CamionDTO>>> obtenerPorEstado(@PathVariable String estado) {
        List<Camion> camiones = camionRepository.findAll().stream()
                .filter(c -> c.getEstado().equalsIgnoreCase(estado))
                .collect(Collectors.toList());
        List<CamionDTO> camionesDTO = camiones.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Camiones por estado", camionesDTO));
    }

    /**
     * Obtener camiones disponibles
     */
    @GetMapping("/estado/disponible")
    public ResponseEntity<ApiResponse<List<CamionDTO>>> obtenerDisponibles() {
        List<Camion> camiones = camionRepository.findAll().stream()
                .filter(c -> "disponible".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());
        List<CamionDTO> camionesDTO = camiones.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Camiones disponibles", camionesDTO));
    }

    /**
     * Obtener ubicación actual del camión
     */
    @GetMapping("/{id}/ubicacion")
    public ResponseEntity<ApiResponse<CamionDTO>> obtenerUbicacion(@PathVariable Long id) {
        try {
            Camion camion = camionRepository.findById(id).orElseThrow();
            return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación del camión", convertirADTO(camion)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    /**
     * Crear nuevo camión
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CamionDTO>> crearCamion(@RequestBody Camion camion) {
        try {
            if (camion.getPatente() == null || camion.getPatente().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "La patente es obligatoria"));
            }
            Camion camionGuardado = camionRepository.save(camion);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Camión creado", convertirADTO(camionGuardado)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error al crear camión"));
        }
    }

    /**
     * Actualizar camión
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CamionDTO>> actualizarCamion(@PathVariable Long id, @RequestBody Camion camionActualizado) {
        try {
            Camion camion = camionRepository.findById(id).orElseThrow();
            camion.setPatente(camionActualizado.getPatente());
            camion.setModelo(camionActualizado.getModelo());
            camion.setCapCarga(camionActualizado.getCapCarga());
            
            Camion camionGuardado = camionRepository.save(camion);
            return ResponseEntity.ok(new ApiResponse<>(true, "Camión actualizado", convertirADTO(camionGuardado)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    /**
     * Eliminar camión
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarCamion(@PathVariable Long id) {
        try {
            camionRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Camión eliminado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    private CamionDTO convertirADTO(Camion camion) {
        return new CamionDTO(
            camion.getId(),
            camion.getPatente(),
            camion.getModelo(),
            camion.getCapCarga(),
            camion.getEstado(),
            camion.getLatitudActual(),
            camion.getLongitudActual(),
            camion.getVelocidad(),
            camion.getDistanciaTotalRecorrida(),
            camion.getTiempoUltimaUbicacion() != null ? camion.getTiempoUltimaUbicacion().toString() : null
        );
    }
}
