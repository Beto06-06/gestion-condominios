package com.incondo.repository;

import com.incondo.domain.AlquilerInquilino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlquilerInquilinoRepository extends JpaRepository<AlquilerInquilino, Integer> {

    java.util.List<AlquilerInquilino> findByPersonaIdPersona(Integer idPersona);
}
