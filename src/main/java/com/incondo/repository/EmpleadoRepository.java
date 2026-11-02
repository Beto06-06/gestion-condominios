package com.incondo.repository;

import com.incondo.domain.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    java.util.Optional<Empleado> findByPersonaIdPersona(Integer idPersona);

    java.util.List<Empleado> findAllByOrderByPersonaNombreCompletoAsc();
}
