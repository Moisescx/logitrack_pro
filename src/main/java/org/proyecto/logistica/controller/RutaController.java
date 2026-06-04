package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.service.RutaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
