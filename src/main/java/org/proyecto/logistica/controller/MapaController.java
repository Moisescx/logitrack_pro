package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/mapa")
public class MapaController {

    private final RutaRepository rutaRepository;
    private final UsuarioRepository usuarioRepository;

    public MapaController(RutaRepository rutaRepository, UsuarioRepository usuarioRepository) {
        this.rutaRepository = rutaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String verMapa(Model model, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
        String rol = usuarioActual.getRole().toUpperCase();

        List<Ruta> rutasParaElMapa = new ArrayList<>();

        if ("ADMIN".equals(rol)) {
            rutasParaElMapa = rutaRepository.findAll();
        } else if ("DESPACHADOR".equals(rol)) {
            rutasParaElMapa = rutaRepository.findAll().stream()
                    .filter(r -> (r.getCamion() != null && r.getCamion().getDespachador() != null && r.getCamion().getDespachador().getId().equals(usuarioActual.getId()))
                            || ("pendiente".equals(r.getEstado()) && r.getCamion() == null))
                    .collect(Collectors.toList());
        } else if ("CHOFER".equals(rol)) {
            rutasParaElMapa = rutaRepository.findAll().stream()
                    .filter(r -> r.getCamion() != null && r.getCamion().getChofer() != null && r.getCamion().getChofer().getId().equals(usuarioActual.getId()))
                    .collect(Collectors.toList());
        }

        // Convertir a DTOs simples (Map) para evitar problemas de lazy-loading/serialización en la vista
        List<java.util.Map<String, Object>> rutasDto = rutasParaElMapa.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("origen", r.getOrigen());
            m.put("destino", r.getDestino());
            m.put("estado", r.getEstado());
            if (r.getCamion() != null) {
                java.util.Map<String, Object> cam = new java.util.HashMap<>();
                cam.put("patente", r.getCamion().getPatente());
                cam.put("id", r.getCamion().getId());
                m.put("camion", cam);
            } else {
                m.put("camion", null);
            }
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("rutas", rutasDto);
        model.addAttribute("rol", rol);

        return "mapa";
    }
}
