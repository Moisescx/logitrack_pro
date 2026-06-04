package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chofer")
public class ChoferController {
    private final RutaRepository rutaRepository;
    private final CamionRepository camionRepository;
    private final UsuarioRepository usuarioRepository;

    public ChoferController(RutaRepository rutaRepository, CamionRepository camionRepository, UsuarioRepository usuarioRepository) {
        this.rutaRepository = rutaRepository;
        this.camionRepository = camionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String verDashboardChofer(Model model, Principal principal) {
        Usuario choferActual = usuarioRepository.findByUsername(principal.getName()).orElseThrow();

        java.util.List<Camion> camiones = camionRepository.findByChoferId(choferActual.getId());
        Camion miCamion = camiones.stream().findFirst().orElse(null);

        List<Ruta> misRutas = List.of();
        long pendientes = 0;
        long completadas = 0;

        if (miCamion != null) {
            misRutas = rutaRepository.findAll().stream()
                    .filter(r -> r.getCamion() != null && r.getCamion().getId().equals(miCamion.getId()))
                    .collect(Collectors.toList());

            pendientes = misRutas.stream().filter(r -> "pendiente".equals(r.getEstado())).count();
            completadas = misRutas.stream().filter(r -> "completada".equals(r.getEstado())).count();
        }

        model.addAttribute("rutas", misRutas);
        model.addAttribute("camion", miCamion);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("completadas", completadas);

        return "chofer/dashboard_chofer";
    }

    @PostMapping("/iniciar/{id}")
    public String iniciarRuta(@PathVariable Long id) {
        Ruta ruta = rutaRepository.findById(id).orElseThrow();
        Camion camion = ruta.getCamion();

        ruta.setEstado("en_progreso");

        if (camion != null) {
            camion.setEstado("en_ruta");
            camionRepository.save(camion);
        }
        rutaRepository.save(ruta);

        return "redirect:/chofer";
    }

    @PostMapping("/finalizar/{id}")
    public String finalizarRuta(@PathVariable Long id) {
        Ruta ruta = rutaRepository.findById(id).orElseThrow();
        Camion camion = ruta.getCamion();

        ruta.setEstado("completada");

        if (camion != null) {
            camion.setEstado("disponible");
            camionRepository.save(camion);
        }
        rutaRepository.save(ruta);

        return "redirect:/chofer";
    }
}
