package org.proyecto.logistica.repository;

import org.proyecto.logistica.model.UbicacionHistorial;
import org.proyecto.logistica.model.Camion;
import org.proyecto.logistica.model.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UbicacionHistorialRepository extends JpaRepository<UbicacionHistorial, Long> {
    List<UbicacionHistorial> findByCamionOrderByTimestampDesc(Camion camion);
    List<UbicacionHistorial> findByRutaOrderByTimestampDesc(Ruta ruta);
    List<UbicacionHistorial> findByCamionAndTimestampBetween(Camion camion, LocalDateTime inicio, LocalDateTime fin);
    List<UbicacionHistorial> findTop10ByCamionOrderByTimestampDesc(Camion camion);
}
