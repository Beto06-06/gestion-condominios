package com.incondo.repository;

import com.incondo.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    java.util.Optional<Persona> findByCedula(String cedula);

    boolean existsByCedulaAndIdPersonaNot(String cedula, Integer idPersona);

    java.util.List<Persona> findByNombreCompletoContainingIgnoreCaseOrCedulaContainingIgnoreCaseOrderByNombreCompletoAsc(String nombreCompleto, String cedula);
}
