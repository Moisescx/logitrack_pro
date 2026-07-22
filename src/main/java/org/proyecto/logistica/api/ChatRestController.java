package org.proyecto.logistica.api;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Historial;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.HistorialRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    @Autowired
    private CamionRepository camionRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistorialRepository historialRepository;

    private final ChatClient chatClient;

    public ChatRestController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("Eres el 'Copiloto IA', un asistente virtual experto en logística de transporte. " +
                        "Tu trabajo es ayudar a los choferes de camiones con emergencias en la ruta, " +
                        "protocolos de entrega, y fallas mecánicas básicas. " +
                        "Responde siempre en español, de forma muy breve, clara, directa y profesional.")
                .build();
    }

    @PostMapping("/chofer")
    public Map<String, String> chatearConPiloto(@RequestBody Map<String, String> request, Principal principal) {
        String mensajeChofer = request.get("mensaje");

        if (principal == null) {
            return Map.of("respuesta", "No hay sesión autenticada.");
        }

        Usuario choferActual = usuarioRepository.findByUsername(principal.getName()).orElse(null);
        if (choferActual == null) {
            return Map.of("respuesta", "No se encontró el chofer autenticado.");
        }

        List<Camion> misCamiones = camionRepository.findByChoferId(choferActual.getId());
        Camion miCamion = misCamiones.isEmpty() ? null : misCamiones.get(0);

        Ruta miRutaActiva = null;
        if (miCamion != null) {
            miRutaActiva = rutaRepository.findAll().stream()
                    .filter(ruta -> ruta.getCamion() != null && ruta.getCamion().getId().equals(miCamion.getId()))
                    .filter(ruta -> "pendiente".equalsIgnoreCase(ruta.getEstado()) || "en_progreso".equalsIgnoreCase(ruta.getEstado()))
                    .findFirst()
                    .orElse(null);
        }

        String patente = miCamion != null && miCamion.getPatente() != null ? miCamion.getPatente() : "sin asignar";
        String destino = miRutaActiva != null && miRutaActiva.getDestino() != null ? miRutaActiva.getDestino() : "sin ruta activa";
        String carga = miCamion != null && miCamion.getCapCarga() != null
                ? "capacidad de carga " + miCamion.getCapCarga() + " toneladas"
                : "sin datos de carga";
        String estadoRuta = miRutaActiva != null && miRutaActiva.getEstado() != null ? miRutaActiva.getEstado() : "sin estado";

        String contextoSecreto = "Eres el 'Copiloto IA'. Estás hablando con el chofer " + choferActual.getUsername() + ". " +
                "REGLA DE ORO: Usa EXCLUSIVAMENTE esta información de su viaje actual: " +
                "Su camión asignado es patente " + patente + ". " +
                "Su destino actual es " + destino + ". " +
                "El estado de la ruta es " + estadoRuta + ". " +
                "La carga disponible es " + carga + ". " +
                "Si te pregunta sobre su viaje, responde basándote en estos datos. Responde breve y directo.";

        String respuestaIA = chatClient.prompt()
                .system(contextoSecreto)
                .user(mensajeChofer)
                .call()
                .content();

        return Map.of("respuesta", respuestaIA);
    }

    @PostMapping("/despachador")
    public Map<String, String> chatearConDespachador(@RequestBody Map<String, String> request) {
        String mensajeDespachador = request.get("mensaje");

        long totalCamiones = camionRepository.count();
        long totalRutas = rutaRepository.count();

        String contextoSecreto = "Eres 'TrackIA', el asistente principal del Despachador de la flota logística. " +
                "REGLA DE ORO: Si te preguntan por cantidades, usa EXCLUSIVAMENTE estos datos reales de la base de datos: " +
                "Actualmente tenemos " + totalCamiones + " camiones registrados y " + totalRutas + " rutas en el sistema. " +
                "Tu objetivo es ayudar a optimizar rutas. Responde de forma clara y analítica.";

        String respuestaIA = chatClient.prompt()
                .system(contextoSecreto)
                .user(mensajeDespachador)
                .call()
                .content();

        return Map.of("respuesta", respuestaIA);
    }

    @PostMapping("/admin")
    public Map<String, String> chatearConAdmin(@RequestBody Map<String, String> request, Principal principal) {
        String mensajeAdmin = request.get("mensaje");

        if (principal == null) {
            return Map.of("respuesta", "No hay sesión autenticada.");
        }

        Usuario adminActual = usuarioRepository.findByUsername(principal.getName()).orElse(null);
        if (adminActual == null) {
            return Map.of("respuesta", "No se encontró el administrador autenticado.");
        }

        // Métricas generales del sistema
        long totalCamiones = camionRepository.count();
        long totalRutas = rutaRepository.count();
        long totalChoferes = usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().equals("ROLE_CHOFER"))
                .count();
        long totalDespachadores = usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().equals("ROLE_DESPACHADOR"))
                .count();

        // Métricas de rutas
        long rutasPendientes = rutaRepository.findByEstado("pendiente").size();
        long rutasEnProgreso = rutaRepository.findByEstado("en_progreso").size();
        long rutasCompletadas = rutaRepository.findByEstado("completada").size();

        // Métricas de camiones
        long camionesDisponibles = camionRepository.findAll().stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("disponible"))
                .count();
        long camionesEnRuta = camionRepository.findAll().stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("en_ruta"))
                .count();
        long camionesMantenimiento = camionRepository.findAll().stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("mantenimiento"))
                .count();

        // Cálculo de distancia total y capacidad
        double distanciaTotalRecorrida = camionRepository.findAll().stream()
                .mapToDouble(c -> c.getDistanciaTotalRecorrida() != null ? c.getDistanciaTotalRecorrida() : 0.0)
                .sum();

        double capacidadTotalFlota = camionRepository.findAll().stream()
                .mapToDouble(c -> c.getCapCarga() != null ? c.getCapCarga() : 0.0)
                .sum();

        // Historial reciente
        List<Historial> historialReciente = historialRepository.findAll().stream()
                .limit(10)
                .toList();
        String ultimasAcciones = historialReciente.isEmpty() ? "Sin actividad reciente" : 
                String.join(" | ", historialReciente.stream()
                        .map(h -> h.getAccion() + ": " + h.getDetalle())
                        .toList());

        // Probabilidades y análisis
        double porcentajeRutasCompletas = totalRutas > 0 ? (rutasCompletadas * 100.0 / totalRutas) : 0;
        double porcentajeCapacidadPromedio = totalCamiones > 0 ? (capacidadTotalFlota / (totalCamiones * 5)) * 100 : 0;
        double porcentajeCamionesActivos = totalCamiones > 0 ? ((camionesEnRuta + camionesDisponibles) * 100.0 / totalCamiones) : 0;

        String contextoAdmin = "Eres 'AdminIA', el asistente inteligente del Administrador de Flota Logística. " +
                "Tu rol es proporcionar análisis estratégico, métricas en tiempo real y recomendaciones de optimización. " +
                "DATOS EN TIEMPO REAL DEL SISTEMA:" +
                "\n📊 VISIÓN GENERAL:" +
                "\n  - Total de Camiones: " + totalCamiones + " unidades" +
                "\n  - Total de Rutas: " + totalRutas + " rutas" +
                "\n  - Personal: " + totalChoferes + " choferes, " + totalDespachadores + " despachadores" +
                "\n\n🚚 ESTADO DE FLOTA:" +
                "\n  - Camiones Disponibles: " + camionesDisponibles + " (" + String.format("%.1f%%", (camionesDisponibles * 100.0 / totalCamiones)) + ")" +
                "\n  - En Ruta: " + camionesEnRuta + " (" + String.format("%.1f%%", (camionesEnRuta * 100.0 / totalCamiones)) + ")" +
                "\n  - En Mantenimiento: " + camionesMantenimiento + " (" + String.format("%.1f%%", (camionesMantenimiento * 100.0 / totalCamiones)) + ")" +
                "\n\n📍 ESTADO DE RUTAS:" +
                "\n  - Pendientes: " + rutasPendientes +
                "\n  - En Progreso: " + rutasEnProgreso +
                "\n  - Completadas: " + rutasCompletadas + " (" + String.format("%.1f%%", porcentajeRutasCompletas) + " de eficiencia)" +
                "\n\n💾 CAPACIDAD Y DISTANCIA:" +
                "\n  - Capacidad Total de Flota: " + String.format("%.2f", capacidadTotalFlota) + " toneladas" +
                "\n  - Distancia Total Recorrida: " + String.format("%.2f", distanciaTotalRecorrida) + " km" +
                "\n\n📈 ANÁLISIS PREDICTIVO:" +
                "\n  - Eficiencia de Rutas: " + String.format("%.1f%%", porcentajeRutasCompletas) +
                "\n  - Utilización de Camiones: " + String.format("%.1f%%", porcentajeCamionesActivos) +
                "\n  - Capacidad Promedio Utilizada: " + String.format("%.1f%%", porcentajeCapacidadPromedio) +
                "\n\n📝 ÚLTIMAS ACTIVIDADES: " + ultimasAcciones +
                "\n\nOBJETIVO: Proporciona análisis profundos, identifica cuellos de botella, sugiere optimizaciones de rutas, " +
                "alertas de mantenimiento y decisiones estratégicas basadas en estos datos. " +
                "Responde en español, de forma profesional, clara y con recomendaciones accionables.";

        String respuestaIA = chatClient.prompt()
                .system(contextoAdmin)
                .user(mensajeAdmin)
                .call()
                .content();

        return Map.of("respuesta", respuestaIA);
    }
}
