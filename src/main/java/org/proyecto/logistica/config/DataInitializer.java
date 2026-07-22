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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            System.out.println("🏭 Iniciando inyección de DATOS MASIVOS...");

            // ==========================================
            // 1. TUS CASOS DE PRUEBA ORIGINALES
            // ==========================================
            Usuario admin = crearUsuario("admin", "admin123", "ADMIN");
            Usuario despachador1 = crearUsuario("despachador1", "1234", "DESPACHADOR");

            Usuario juan = crearUsuario("juan", "1234", "CHOFER");
            Usuario pedro = crearUsuario("pedro", "1234", "CHOFER");
            Usuario diego = crearUsuario("diego", "1234", "CHOFER");

            Camion camionJuan = crearCamion("JUAN-99", "Volvo FH", 25.5, "disponible", juan, despachador1);
            Camion camionPedro = crearCamion("PEDR-88", "Scania R", 18.0, "disponible", pedro, despachador1);
            Camion camionTaller = crearCamion("FAIL-00", "Iveco", 15.0, "mantenimiento", null, despachador1);

            crearRuta("Santiago", "Valparaíso", "pendiente", camionJuan);
            crearRuta("Concepción", "Temuco", "pendiente", camionJuan);
            crearRuta("Antofagasta", "Iquique", "pendiente", null);

            // ==========================================
            // 2. INYECCIÓN MASIVA ALEATORIA
            // ==========================================
            System.out.println("🚀 Generando flota y rutas adicionales...");

            Random random = new Random();
            List<Camion> camionesActivos = new ArrayList<>();
            camionesActivos.add(camionJuan);
            camionesActivos.add(camionPedro);

            // A. Crear 15 Choferes y Camiones extra
            String[] marcas = {"Volvo", "Scania", "Mercedes-Benz", "Iveco", "MAN", "Ford"};
            String[] estadosCamion = {"disponible", "en_ruta", "mantenimiento"};

            for (int i = 1; i <= 15; i++) {
                Usuario nuevoChofer = crearUsuario("chofer_" + i, "1234", "CHOFER");

                // Generar patente estilo FLOT-01, FLOT-02...
                String patente = "FLOT-" + String.format("%02d", i);
                String modelo = marcas[random.nextInt(marcas.length)];

                // Capacidad aleatoria entre 10 y 30 toneladas
                double carga = Math.round((10.0 + (random.nextDouble() * 20.0)) * 10.0) / 10.0;
                String estado = estadosCamion[random.nextInt(estadosCamion.length)];

                Camion nuevoCamion = crearCamion(patente, modelo, carga, estado, nuevoChofer, despachador1);

                // Guardamos los camiones que no están en el taller para darles rutas luego
                if (!estado.equals("mantenimiento")) {
                    camionesActivos.add(nuevoCamion);
                }
            }

            // B. Crear 100 Rutas históricas y activas
            String[] ciudades = {"Arica", "Iquique", "Antofagasta", "Copiapó", "La Serena", "Valparaíso", "Santiago", "Rancagua", "Talca", "Chillán", "Concepción", "Temuco", "Valdivia", "Puerto Montt"};

            String[] estadosRuta = {"completado", "pendiente", "pendiente"};
            for (int i = 1; i <= 100; i++) {
                String origen = ciudades[random.nextInt(ciudades.length)];
                String destino = ciudades[random.nextInt(ciudades.length)];

                // Evitar que el origen y destino sean la misma ciudad
                while (origen.equals(destino)) {
                    destino = ciudades[random.nextInt(ciudades.length)];
                }

                String estadoRuta = estadosRuta[random.nextInt(estadosRuta.length)];

                // Asignar un camión aleatorio de la lista de activos
                Camion camionAsignado = null;
                if (!estadoRuta.equals("pendiente") || random.nextBoolean()) {
                    camionAsignado = camionesActivos.get(random.nextInt(camionesActivos.size()));
                }

                crearRuta(origen, destino, estadoRuta, camionAsignado);
            }

            System.out.println("✅ ¡Base de datos cargada con 115 rutas y 18 camiones!");
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