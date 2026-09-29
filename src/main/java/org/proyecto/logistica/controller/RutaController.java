package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping("/iniciar/{id}")
    public ResponseEntity<Ruta> iniciarViaje(@PathVariable Long id) {
        try {
            Ruta rutaActualizada = rutaService.iniciarRuta(id);
            return ResponseEntity.ok(rutaActualizada);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/finalizar/{id}")
    public ResponseEntity<Ruta> finalizarViaje(@PathVariable Long id) {
        try {
            Ruta rutaActualizada = rutaService.finalizarRuta(id);
            return ResponseEntity.ok(rutaActualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/descanso")
    public ResponseEntity<Map<String, Object>> estadoDescanso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rutaService.obtenerEstadoDescanso(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/descanso/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarDescanso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rutaService.iniciarDescanso(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/descanso/finalizar")
    public ResponseEntity<Map<String, Object>> finalizarDescanso(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(rutaService.finalizarDescanso(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
