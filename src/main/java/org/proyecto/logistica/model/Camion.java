package org.proyecto.logistica.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "camiones")
@Data

public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String patente;

    private String modelo;

    @Column(name = "capCarga")
    private Double capCarga;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "despachador_id")
    private Usuario despachador;

    @ManyToOne
    @JoinColumn(name = "chofer_id")
    private Usuario chofer;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    private LocalDateTime fechaActualizacion;

    // GPS Tracking - Nuevos campos
    @Column(name = "latitud_actual")
    private Double latitudActual;

    @Column(name = "longitud_actual")
    private Double longitudActual;

    @Column(name = "velocidad")
    private Double velocidad = 0.0; // km/h

    @Column(name = "distancia_total_recorrida")
    private Double distanciaTotalRecorrida = 0.0; // en km

    @Column(name = "tiempo_ultima_ubicacion")
    private LocalDateTime tiempoUltimaUbicacion;

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
