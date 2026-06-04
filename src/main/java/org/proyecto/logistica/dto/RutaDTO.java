package org.proyecto.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private Long id;
    private String origen;
    private String destino;
    private String estado;
    private Long camionId;
    private Double distanciaRecorrida;
    private String startTime;
    private Double latitudActual;
    private Double longitudActual;
}
