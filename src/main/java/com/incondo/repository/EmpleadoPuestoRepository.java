package com.incondo.repository;

import com.incondo.domain.EmpleadoPuesto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoPuestoRepository extends JpaRepository<EmpleadoPuesto, Integer> {

    boolean existsByEmpleadoIdEmpleadoAndPuestoIdPuestoAndTurnoIdTurnoAndIdEmpleadoPuestoNot(Integer idEmpleado, Integer idPuesto, Integer idTurno, Integer idEmpleadoPuesto);

    List<EmpleadoPuesto> findByEmpleadoIdEmpleado(Integer idEmpleado);

}
