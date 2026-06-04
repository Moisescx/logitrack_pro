package org.proyecto.logistica.repository;

import org.proyecto.logistica.model.Camion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CamionRepository extends  JpaRepository<Camion, Long> {
	java.util.List<Camion> findByDespachadorId(Long despachadorId);
	java.util.List<Camion> findByChoferId(Long choferId);
}