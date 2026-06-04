package org.proyecto.logistica.api;

import org.proyecto.logistica.dto.ApiResponse;
import org.proyecto.logistica.dto.UbicacionDTO;
import org.proyecto.logistica.service.UbicacionService;
import org.proyecto.logistica.model.UbicacionHistorial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ubicaciones")
@CrossOrigin(origins = "*")
public class UbicacionRestController {

    @Autowired
    private UbicacionService ubicacionService;

    /**
     * Enviar ubicación GPS del camión (Endpoint REST)
     * POST /api/v1/ubicaciones
     * Body: { "camionId": 1, "latitud": -40.5, "longitud": -74.5, "velocidad": 50, "precision": "5m" }
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UbicacionDTO>> guardarUbicacion(@RequestBody UbicacionDTO ubicacion) {
        try {
            UbicacionHistorial historial = ubicacionService.guardarUbicacion(
                ubicacion.getCamionId(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud(),
                ubicacion.getVelocidad() != null ? ubicacion.getVelocidad() : 0.0,
                ubicacion.getPrecision()
            );

            ubicacion.setTimestamp(historial.getTimestamp().toString());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Ubicación guardada", ubicacion));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error al guardar ubicación"));
        }
    }

    /**
     * Obtener histórico de ubicaciones del camión
     * GET /api/v1/ubicaciones/camion/1
     */
    @GetMapping("/camion/{camionId}")
    public ResponseEntity<ApiResponse<List<UbicacionHistorial>>> obtenerHistorialCamion(@PathVariable Long camionId) {
        try {
            List<UbicacionHistorial> historial = ubicacionService.obtenerHistorialCamion(camionId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Histórico de ubicaciones", historial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    /**
     * Obtener últimas 10 ubicaciones del camión
     * GET /api/v1/ubicaciones/camion/1/ultimas
     */
    @GetMapping("/camion/{camionId}/ultimas")
    public ResponseEntity<ApiResponse<List<UbicacionHistorial>>> obtenerUltimasUbicaciones(@PathVariable Long camionId) {
        try {
            List<UbicacionHistorial> historial = ubicacionService.obtenerUltimas10Ubicaciones(camionId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Últimas ubicaciones", historial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Camión no encontrado"));
        }
    }

    /**
     * Obtener histórico de ubicaciones por ruta
     * GET /api/v1/ubicaciones/ruta/1
     */
    @GetMapping("/ruta/{rutaId}")
    public ResponseEntity<ApiResponse<List<UbicacionHistorial>>> obtenerHistorialRuta(@PathVariable Long rutaId) {
        try {
            List<UbicacionHistorial> historial = ubicacionService.obtenerHistorialRuta(rutaId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Histórico de ruta", historial));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Ruta no encontrada"));
        }
    }
}
