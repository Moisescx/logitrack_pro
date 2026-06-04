package org.proyecto.logistica.api;

import org.proyecto.logistica.dto.ApiResponse;
import org.proyecto.logistica.dto.RutaDTO;
import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/despachador")
@CrossOrigin(origins = "*")
public class DespachadorRestController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener mi flota de camiones
     */
    @GetMapping("/mi-flota")
    public ResponseEntity<ApiResponse<List<Camion>>> miFlota(Principal principal) {
        try {
            Usuario despachador = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> miFlota = camionRepository.findByDespachadorId(despachador.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Mi flota", miFlota));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener flota"));
        }
    }

    /**
     * Obtener camiones disponibles de mi flota
     */
    @GetMapping("/camiones-disponibles")
    public ResponseEntity<ApiResponse<List<Camion>>> camionesDisponibles(Principal principal) {
        try {
            Usuario despachador = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> disponibles = camionRepository.findAll().stream()
                    .filter(c -> c.getDespachador() != null && 
                            c.getDespachador().getId().equals(despachador.getId()) &&
                            "disponible".equalsIgnoreCase(c.getEstado()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Camiones disponibles", disponibles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener camiones"));
        }
    }

    /**
     * Obtener rutas pendientes sin camión
     */
    @GetMapping("/rutas-pendientes")
    public ResponseEntity<ApiResponse<List<RutaDTO>>> rutasPendientes() {
        try {
            List<Ruta> pendientes = rutaRepository.findByEstadoAndCamionIsNull("pendiente");
            List<RutaDTO> pendientesDTO = pendientes.stream().map(this::convertirADTO).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Rutas pendientes", pendientesDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener rutas"));
        }
    }

    /**
     * Asignar camión a ruta
     */
    @PostMapping("/asignar")
    public ResponseEntity<ApiResponse<RutaDTO>> asignarCamionARuta(
            @RequestParam Long rutaId,
            @RequestParam Long camionId,
            Principal principal) {
        try {
            Ruta ruta = rutaRepository.findById(rutaId).orElseThrow();
            Camion camion = camionRepository.findById(camionId).orElseThrow();

            // Validar que el despachador tenga este camión
            Usuario despachador = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            if (camion.getDespachador() == null || !camion.getDespachador().getId().equals(despachador.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "No autorizado"));
            }

            // Validar estado
            if (!camion.getEstado().equalsIgnoreCase("disponible")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Camión no disponible"));
            }

            ruta.setCamion(camion);
            ruta.setEstado("en_progreso");
            camion.setEstado("en_ruta");

            camionRepository.save(camion);
            Ruta rutaActualizada = rutaRepository.save(ruta);

            return ResponseEntity.ok(new ApiResponse<>(true, "Asignación completada", convertirADTO(rutaActualizada)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta o camión no encontrados"));
        }
    }

    /**
     * Desasignar camión de ruta
     */
    @PostMapping("/desasignar/{rutaId}")
    public ResponseEntity<ApiResponse<RutaDTO>> desasignarCamion(@PathVariable Long rutaId, Principal principal) {
        try {
            Ruta ruta = rutaRepository.findById(rutaId).orElseThrow();
            Camion camion = ruta.getCamion();

            // Validar que el despachador tenga este camión
            Usuario despachador = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            if (camion == null || camion.getDespachador() == null || 
                !camion.getDespachador().getId().equals(despachador.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "No autorizado"));
            }

            ruta.setCamion(null);
            ruta.setEstado("pendiente");
            if (camion != null) {
                camion.setEstado("disponible");
                camionRepository.save(camion);
            }

            Ruta rutaActualizada = rutaRepository.save(ruta);

            return ResponseEntity.ok(new ApiResponse<>(true, "Desasignación completada", convertirADTO(rutaActualizada)));
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
