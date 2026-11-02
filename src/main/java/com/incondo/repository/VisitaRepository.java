package com.incondo.repository;

import com.incondo.domain.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Integer> {

    boolean existsByVisitanteIdVisitanteAndFechaSalidaIsNull(Integer idVisitante);
}
