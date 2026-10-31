package com.incondo.repository;

import com.incondo.domain.Alquiler;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Integer> {

    @Query("SELECT COUNT(a) FROM Alquiler a WHERE a.filial.idFilial = :idFilial AND a.fechaSalida IS NULL AND a.fechaInicio <= :fechaConsulta AND a.fechaFin >= :fechaConsulta")
    long contarVigentes(Integer idFilial, LocalDate fechaConsulta);

    @Query("SELECT COUNT(a) FROM Alquiler a WHERE a.filial.idFilial = :idFilial AND a.idAlquiler <> :idAlquiler AND a.fechaInicio <= :fin AND ((a.fechaSalida IS NULL AND a.fechaFin >= :inicio) OR a.fechaSalida > :inicio)")
    long contarTraslapes(Integer idFilial, Integer idAlquiler, LocalDate inicio, LocalDate fin);

    java.util.List<Alquiler> findAllByOrderByFechaInicioDescIdAlquilerDesc();

    java.util.List<Alquiler> findByFilialIdFilialOrderByFechaInicioDescIdAlquilerDesc(Integer idFilial);

}
