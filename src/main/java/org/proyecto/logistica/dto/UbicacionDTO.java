package org.proyecto.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    private Long camionId;
    private Double latitud;
    private Double longitud;
    private Double velocidad;
    private String precision;
    private String timestamp;

    public UbicacionDTO(Double latitud, Double longitud, Double velocidad) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.velocidad = velocidad;
    }
}
