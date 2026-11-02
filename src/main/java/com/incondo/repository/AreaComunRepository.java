package com.incondo.repository;

import com.incondo.domain.AreaComun;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaComunRepository extends JpaRepository<AreaComun, Integer> {

    List<AreaComun> findAllByOrderByNombreAsc();

    List<AreaComun> findByActivoTrueOrderByNombreAsc();

    Optional<AreaComun> findByNombreIgnoreCase(String nombre);
}
