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
}

