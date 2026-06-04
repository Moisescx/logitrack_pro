package org.proyecto.logistica.controller;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Historial;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.HistorialRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RutaRepository rutaRepository;
    private final CamionRepository camionRepository;
    private final HistorialRepository historialRepository; // Añadimos el repositorio del log

    public AdminController(UsuarioRepository usuarioRepository,
                           RutaRepository rutaRepository,
                           CamionRepository camionRepository,
                           HistorialRepository historialRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rutaRepository = rutaRepository;
        this.camionRepository = camionRepository;
        this.historialRepository = historialRepository;
    }

    // --- MÉTODO AUXILIAR PARA GUARDAR LOGS FÁCILMENTE ---
    private void registrarActividad(String accion, String detalle, Principal principal) {
        String usuario = (principal != null) ? principal.getName() : "Sistema";
        Historial log = new Historial(accion, detalle, usuario);
        historialRepository.save(log);
    }

    // --- DASHBOARD PRINCIPAL ---
    @GetMapping
    public String dashboardAdmin(Model model) {
        List<Camion> todosCamiones = camionRepository.findAll();
        List<Ruta> todasRutas = rutaRepository.findAll();

        // 1. Cálculos de KPIs
        long camionesEnRuta = todosCamiones.stream().filter(c -> "en_ruta".equals(c.getEstado())).count();
        long rutasCompletadas = todasRutas.stream().filter(r -> "completada".equals(r.getEstado())).count();
        long totalChoferes = usuarioRepository.findAll().stream().filter(u -> "CHOFER".equals(u.getRole())).count();
        long totalDespachadores = usuarioRepository.findAll().stream().filter(u -> "DESPACHADOR".equals(u.getRole())).count();

        // 2. Filtros de recientes (Ordenados por fecha usando Streams)
        List<Camion> ultimosCamiones = todosCamiones.stream()
                .sorted(Comparator.comparing(Camion::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Ruta> ultimasRutas = todasRutas.stream()
                .sorted(Comparator.comparing(Ruta::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // 3. Traer últimos 10 logs de actividad
        List<Historial> logs = historialRepository.findTop10ByOrderByFechaDesc();

        // 4. Enviar datos a la vista
        model.addAttribute("total_trucks", todosCamiones.size());
        model.addAttribute("camionesEnRuta", camionesEnRuta);
        model.addAttribute("total_routes", todasRutas.size());
        model.addAttribute("rutasCompletadas", rutasCompletadas);
        model.addAttribute("total_drivers", totalChoferes);
        model.addAttribute("total_dispatchers", totalDespachadores);

        // ¡Cambiamos esto para que envíe solo los más recientes, no todos!
        model.addAttribute("recent_trucks", ultimosCamiones);
        model.addAttribute("recent_routes", ultimasRutas);
        model.addAttribute("logs", logs);

        return "admin/dashboard_admin";
    }

    // ==========================================
    // SECCIÓN CAMIONES (CRUD)
    // ==========================================

    @GetMapping("/camiones")
    public String verCamiones(Model model) {
        model.addAttribute("camiones", camionRepository.findAll());
        return "admin/camiones";
    }

    @PostMapping("/camiones/nuevo")
    public String guardarCamion(@ModelAttribute Camion nuevoCamion, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        // Validaciones básicas
        if (nuevoCamion.getPatente() == null || nuevoCamion.getPatente().trim().isEmpty()) {
            ra.addFlashAttribute("error", "La patente es obligatoria.");
            return "redirect:/admin/camiones";
        }
        if (nuevoCamion.getCapCarga() == null || nuevoCamion.getCapCarga() < 0) {
            ra.addFlashAttribute("error", "La capacidad debe ser un número >= 0.");
            return "redirect:/admin/camiones";
        }

        camionRepository.save(nuevoCamion);
        registrarActividad("Nuevo Camión", "Se registró el camión patente: " + nuevoCamion.getPatente(), principal);
        ra.addFlashAttribute("success", "Camión creado correctamente.");
        return "redirect:/admin/camiones";
    }

    @PostMapping("/camiones/eliminar/{id}")
    public String eliminarCamion(@PathVariable Long id, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        java.util.List<Ruta> rutasAsignadas = rutaRepository.findByCamionId(id);

        if (rutasAsignadas != null && !rutasAsignadas.isEmpty()) {
            // No permitir eliminación si existen rutas asociadas
            ra.addFlashAttribute("error", "No se puede eliminar el camión: existen rutas asociadas. Reasigna o elimina las rutas primero.");
            return "redirect:/admin/camiones";
        }

        camionRepository.deleteById(id);
        registrarActividad("Eliminación", "Se eliminó el camión ID: " + id, principal);
        ra.addFlashAttribute("success", "Camión eliminado correctamente.");
        return "redirect:/admin/camiones";
    }

    @GetMapping("/camiones/editar/{id}")
    public String mostrarEditarCamion(@PathVariable Long id, Model model) {
        Camion camion = camionRepository.findById(id).orElseThrow();
        model.addAttribute("camion", camion);
        return "admin/editar_camion";
    }

    @PostMapping("/camiones/editar/{id}")
    public String actualizarCamion(@PathVariable Long id, @ModelAttribute Camion camionActualizado, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        Camion camionExistente = camionRepository.findById(id).orElseThrow();
        // Validaciones básicas
        if (camionActualizado.getPatente() == null || camionActualizado.getPatente().trim().isEmpty()) {
            ra.addFlashAttribute("error", "La patente es obligatoria.");
            return "redirect:/admin/camiones/editar/" + id;
        }
        if (camionActualizado.getCapCarga() == null || camionActualizado.getCapCarga() < 0) {
            ra.addFlashAttribute("error", "La capacidad debe ser un número >= 0.");
            return "redirect:/admin/camiones/editar/" + id;
        }

        camionExistente.setPatente(camionActualizado.getPatente());
        camionExistente.setModelo(camionActualizado.getModelo());
        camionExistente.setCapCarga(camionActualizado.getCapCarga());

        camionRepository.save(camionExistente);
        registrarActividad("Actualización", "Se actualizó el camión patente: " + camionActualizado.getPatente(), principal);
        ra.addFlashAttribute("success", "Camión actualizado correctamente.");
        return "redirect:/admin/camiones";
    }

    // ==========================================
    // SECCIÓN RUTAS (CRUD)
    // ==========================================

    @GetMapping("/rutas")
    public String verRutas(Model model) {
        model.addAttribute("rutas", rutaRepository.findAll());
        model.addAttribute("camiones", camionRepository.findAll());
        return "admin/rutas";
    }

    @PostMapping("/rutas/nueva")
    public String guardarRuta(@ModelAttribute Ruta nuevaRuta, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        rutaRepository.save(nuevaRuta);
        registrarActividad("Nueva Ruta", "Ruta creada: " + nuevaRuta.getOrigen() + " -> " + nuevaRuta.getDestino(), principal);
        ra.addFlashAttribute("success", "Ruta creada correctamente.");
        return "redirect:/admin/rutas";
    }

    @PostMapping("/rutas/eliminar/{id}")
    public String eliminarRuta(@PathVariable Long id, Principal principal, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        rutaRepository.deleteById(id);
        registrarActividad("Eliminación", "Se eliminó la ruta ID: " + id, principal);
        ra.addFlashAttribute("success", "Ruta eliminada correctamente.");
        return "redirect:/admin/rutas";
    }

    @GetMapping("/rutas/editar/{id}")
    public String mostrarEditarRuta(@PathVariable Long id, Model model) {
        Ruta ruta = rutaRepository.findById(id).orElseThrow();
        model.addAttribute("ruta", ruta);
        model.addAttribute("camiones", camionRepository.findAll());
        return "admin/editar_ruta";
    }

    @PostMapping("/rutas/editar/{id}")
    public String actualizarRuta(@PathVariable Long id,
                                 @ModelAttribute Ruta rutaActualizada,
                                 @RequestParam(value = "camionId", required = false) Long camionId,
                                 Principal principal,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        Ruta rutaExistente = rutaRepository.findById(id).orElseThrow();
        rutaExistente.setOrigen(rutaActualizada.getOrigen());
        rutaExistente.setDestino(rutaActualizada.getDestino());
        rutaExistente.setEstado(rutaActualizada.getEstado());

        if (camionId != null) {
            Camion camion = camionRepository.findById(camionId).orElseThrow();
            rutaExistente.setCamion(camion);
        } else {
            rutaExistente.setCamion(null);
        }

        rutaRepository.save(rutaExistente);
        registrarActividad("Actualización", "Ruta ID " + id + " actualizada", principal);
        ra.addFlashAttribute("success", "Ruta actualizada correctamente.");
        return "redirect:/admin/rutas";
    }
}