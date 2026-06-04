package org.proyecto.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CamionDTO {
    private Long id;
    private String patente;
    private String modelo;
    private Double capCarga;
    private String estado;
    private Double latitudActual;
    private Double longitudActual;
    private Double velocidad;
    private Double distanciaTotalRecorrida;
    private String tiempoUltimaUbicacion;
}
