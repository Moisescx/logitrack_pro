package org.proyecto.logistica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ubicacion_historial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionHistorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    @ManyToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    private Double velocidad;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String precision; // Precisión del GPS en metros

    public UbicacionHistorial(Camion camion, Ruta ruta, Double latitud, Double longitud, Double velocidad) {
        this.camion = camion;
        this.ruta = ruta;
        this.latitud = latitud;
        this.longitud = longitud;
        this.velocidad = velocidad;
        this.timestamp = LocalDateTime.now();
    }
}
