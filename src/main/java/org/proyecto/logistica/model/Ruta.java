package org.proyecto.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Table(name = "rutas")
@Data
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origen;
    private String destino;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "camion_id")
    private Camion camion;

    private LocalDateTime start_time;

    // GPS Tracking - Nuevos campos
    @Column(name = "latitud_inicio")
    private Double latitudInicio;

    @Column(name = "longitud_inicio")
    private Double longitudInicio;

    @Column(name = "latitud_actual")
    private Double latitudActual;

    @Column(name = "longitud_actual")
    private Double longitudActual;

    @Column(name = "latitud_fin")
    private Double latitudFin;

    @Column(name = "longitud_fin")
    private Double longitudFin;

    @Column(name = "distancia_recorrida")
    private Double distanciaRecorrida = 0.0; // en km

    @Column(name = "tiempo_actualizacion")
    private LocalDateTime tiempoActualizacion;

}

