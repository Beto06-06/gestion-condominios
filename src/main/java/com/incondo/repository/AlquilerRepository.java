package com.incondo.repository;

import com.incondo.domain.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Integer> {

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(alquiler) FROM Alquiler alquiler WHERE alquiler.filial.idFilial=:idFilial AND alquiler.fechaSalida IS NULL AND alquiler.fechaInicio<=:fechaConsulta AND alquiler.fechaFin>=:fechaConsulta")
    long contarVigentes(Integer idFilial, java.time.LocalDate fechaConsulta);
}
