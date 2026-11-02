package com.incondo.repository;

import com.incondo.domain.Visita;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Integer> {

    boolean existsByVisitanteIdVisitanteAndFechaSalidaIsNull(Integer idVisitante);

    List<Visita> findByFechaSalidaIsNullOrderByFechaIngresoDescIdVisitaDesc();

    long countByFechaIngresoBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT visita FROM Visita visita WHERE (:desde IS NULL OR visita.fechaIngreso >= :desde) AND (:hasta IS NULL OR visita.fechaIngreso <= :hasta) AND (:idFilial IS NULL OR visita.filial.idFilial = :idFilial) AND (:cedula = '' OR visita.visitante.persona.cedula = :cedula) ORDER BY visita.fechaIngreso DESC, visita.idVisita DESC")
    List<Visita> consultarHistorial(LocalDateTime desde, LocalDateTime hasta, Integer idFilial, String cedula);
}
