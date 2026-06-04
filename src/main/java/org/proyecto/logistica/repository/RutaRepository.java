package org.proyecto.logistica.repository;

import org.proyecto.logistica.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RutaRepository extends  JpaRepository<Ruta, Long> {
    List<Ruta> findByEstado(String estado);
    List<Ruta> findByCamionId(Long camionId);
    List<Ruta> findByEstadoAndCamionIsNull(String estado);
}
