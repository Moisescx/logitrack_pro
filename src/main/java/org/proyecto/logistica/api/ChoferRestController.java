package org.proyecto.logistica.api;

import org.proyecto.logistica.dto.ApiResponse;
import org.proyecto.logistica.dto.RutaDTO;
import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.proyecto.logistica.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/chofer")
@CrossOrigin(origins = "*")
public class ChoferRestController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RutaService rutaService;

    /**
     * Obtener mis rutas (requiere autenticación)
     */
    @GetMapping("/mis-rutas")
    public ResponseEntity<ApiResponse<List<RutaDTO>>> misPendientes(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "No autenticado"));
            }

            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> misCamiones = camionRepository.findByChoferId(chofer.getId());
            
            List<Ruta> misRutas = misCamiones.isEmpty() ? List.of() : 
                rutaRepository.findAll().stream()
                    .filter(r -> r.getCamion() != null && misCamiones.stream()
                        .anyMatch(c -> c.getId().equals(r.getCamion().getId())))
                    .collect(Collectors.toList());

            List<RutaDTO> rutasDTO = misRutas.stream().map(this::convertirADTO).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Mis rutas", rutasDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener rutas"));
        }
    }

    /**
     * Obtener rutas pendientes del chofer
     */
    @GetMapping("/rutas-pendientes")
    public ResponseEntity<ApiResponse<List<RutaDTO>>> rutasPendientes(Principal principal) {
        try {
            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> misCamiones = camionRepository.findByChoferId(chofer.getId());
            
            List<Ruta> rutas = misCamiones.isEmpty() ? List.of() : 
                rutaRepository.findAll().stream()
                    .filter(r -> r.getCamion() != null && misCamiones.stream()
                        .anyMatch(c -> c.getId().equals(r.getCamion().getId())) &&
                        "pendiente".equals(r.getEstado()))
                    .collect(Collectors.toList());

            List<RutaDTO> rutasDTO = rutas.stream().map(this::convertirADTO).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Rutas pendientes", rutasDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener rutas"));
        }
    }

    /**
     * Obtener rutas completadas del chofer
     */
    @GetMapping("/rutas-completadas")
    public ResponseEntity<ApiResponse<List<RutaDTO>>> rutasCompletadas(Principal principal) {
        try {
            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> misCamiones = camionRepository.findByChoferId(chofer.getId());
            
            List<Ruta> rutas = misCamiones.isEmpty() ? List.of() : 
                rutaRepository.findAll().stream()
                    .filter(r -> r.getCamion() != null && misCamiones.stream()
                        .anyMatch(c -> c.getId().equals(r.getCamion().getId())) &&
                        "completada".equals(r.getEstado()))
                    .collect(Collectors.toList());

            List<RutaDTO> rutasDTO = rutas.stream().map(this::convertirADTO).collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponse<>(true, "Rutas completadas", rutasDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener rutas"));
        }
    }

    /**
     * Obtener mi camión asignado
     */
    @GetMapping("/mi-camion")
    public ResponseEntity<ApiResponse<?>> miCamion(Principal principal) {
        try {
            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            List<Camion> misCamiones = camionRepository.findByChoferId(chofer.getId());
            
            if (misCamiones.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "Sin camión asignado"));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Camión del chofer", misCamiones.get(0)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al obtener camión"));
        }
    }

    /**
     * Iniciar ruta
     */
    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<ApiResponse<RutaDTO>> iniciarRuta(@PathVariable Long id, Principal principal) {
        try {
            Ruta ruta = rutaRepository.findById(id).orElseThrow();
            
            // Validar que el chofer tenga este camión
            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            if (ruta.getCamion() == null || !ruta.getCamion().getChofer().getId().equals(chofer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "No autorizado"));
            }

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
    @PostMapping("/rutas/{id}/finalizar")
    public ResponseEntity<ApiResponse<RutaDTO>> finalizarRuta(@PathVariable Long id, Principal principal) {
        try {
            Ruta ruta = rutaRepository.findById(id).orElseThrow();
            
            // Validar que el chofer tenga este camión
            Usuario chofer = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
            if (ruta.getCamion() == null || !ruta.getCamion().getChofer().getId().equals(chofer.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "No autorizado"));
            }

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
