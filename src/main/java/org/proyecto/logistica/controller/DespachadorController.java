package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/despachador")
public class DespachadorController {
    private final RutaRepository rutaRepository;
    private final CamionRepository camionRepository;
    private final UsuarioRepository usuarioRepository;

    public DespachadorController(RutaRepository rutaRepository, CamionRepository camionRepository, UsuarioRepository usuarioRepository) {
        this.rutaRepository = rutaRepository;
        this.camionRepository = camionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String dashboardDespachador(Model model, Principal principal) {
        Usuario despachadorActual = usuarioRepository.findByUsername(principal.getName()).orElseThrow();

        List<Camion> miFlota = camionRepository.findByDespachadorId(despachadorActual.getId());

        // Rutas pendientes sin camión
        List<Ruta> pendientes = rutaRepository.findByEstadoAndCamionIsNull("pendiente");

        model.addAttribute("miFlota", miFlota);
        model.addAttribute("rutasPendientes", pendientes);

        return "despachador/dashboard_despachador";
    }

    @GetMapping("/asignar/{id}")
    public String mostrarAsignacion(@PathVariable Long id, Model model, Principal principal) {
        Ruta ruta = rutaRepository.findById(id).orElseThrow();
        Usuario despachadorActual = usuarioRepository.findByUsername(principal.getName()).orElseThrow();

        List<Camion> misCamionesDisponibles = camionRepository.findAll().stream()
                .filter(c -> "disponible".equalsIgnoreCase(c.getEstado()))
                .filter(c -> c.getDespachador() != null && c.getDespachador().getId().equals(despachadorActual.getId()))
                .collect(Collectors.toList());

        model.addAttribute("ruta", ruta);
        model.addAttribute("camiones", misCamionesDisponibles);

        return "despachador/asignar";
    }

    @PostMapping("/asignar/{id}")
    public String procesarAsignacion(@PathVariable Long id, @RequestParam("camionId") Long camionId) {
        Ruta ruta = rutaRepository.findById(id).orElseThrow();
        Camion camion = camionRepository.findById(camionId).orElseThrow();

        ruta.setCamion(camion);
        ruta.setEstado("en_progreso");
        camion.setEstado("en_ruta");

        camionRepository.save(camion);
        rutaRepository.save(ruta);

        return "redirect:/despachador";
    }
}
