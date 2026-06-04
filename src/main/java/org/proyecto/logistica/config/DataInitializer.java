package org.proyecto.logistica.config;

import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.proyecto.logistica.model.Usuario;
import org.proyecto.logistica.repository.CamionRepository;
import org.proyecto.logistica.repository.RutaRepository;
import org.proyecto.logistica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private CamionRepository camionRepository;
    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (usuarioRepository.count() == 0) {
            System.out.println("🏭 Iniciando inyección de DATOS FIJOS...");

            Usuario admin = crearUsuario("admin", "admin123", "ADMIN");
            Usuario despachador1 = crearUsuario("despachador1", "1234", "DESPACHADOR");

            Usuario juan = crearUsuario("juan", "1234", "CHOFER");     // Tendrá camión y rutas
            Usuario pedro = crearUsuario("pedro", "1234", "CHOFER");   // Tendrá camión, pero 0 rutas (Día libre)
            Usuario diego = crearUsuario("diego", "1234", "CHOFER");   // No tendrá ni camión asignado

            // Camión 1: Para Juan (Listo para trabajar)
            Camion camionJuan = crearCamion("JUAN-99", "Volvo FH", 25.5, "disponible", juan, despachador1);
            // Camión 2: Para Pedro (Disponible en base, sin viajes)
            Camion camionPedro = crearCamion("PEDR-88", "Scania R", 18.0, "disponible", pedro, despachador1);
            // Camión 3: En taller (No se puede usar)
            Camion camionTaller = crearCamion("FAIL-00", "Iveco", 15.0, "mantenimiento", null, despachador1);


            // Caso A: Ruta para Juan que ya está EN PROGRESO (Para probar el botón Finalizar)
            crearRuta("Santiago", "Valparaíso", "en_progreso", camionJuan);

            // Caso B: Ruta para Juan que está PENDIENTE (Para probar el botón Iniciar)
            crearRuta("Concepción", "Temuco", "pendiente", camionJuan);

            // Caso C: Ruta "Huérfana" PENDIENTE sin camión (Para que el despachador la asigne)
            crearRuta("Antofagasta", "Iquique", "pendiente", null);

            System.out.println("✅ Datos fijos inyectados. Todo listo para probar.");
        }
    }


    private Usuario crearUsuario(String username, String password, String role) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        return usuarioRepository.save(u);
    }

    private Camion crearCamion(String patente, String modelo, Double capCarga, String estado, Usuario chofer, Usuario despachador) {
        Camion c = new Camion();
        c.setPatente(patente);
        c.setModelo(modelo);
        c.setCapCarga(capCarga);
        c.setEstado(estado);
        c.setChofer(chofer);
        c.setDespachador(despachador);
        return camionRepository.save(c);
    }

    private void crearRuta(String origen, String destino, String estado, Camion camion) {
        Ruta r = new Ruta();
        r.setOrigen(origen);
        r.setDestino(destino);
        r.setEstado(estado);
        r.setCamion(camion);
        rutaRepository.save(r);
    }
}