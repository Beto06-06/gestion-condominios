package com.incondo.repository;

import com.incondo.domain.Visitante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitanteRepository extends JpaRepository<Visitante, Integer> {

    Optional<Visitante> findByPersonaCedula(String cedula);
}
