package com.incondo.repository;

import com.incondo.domain.Parqueo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ParqueoRepository extends JpaRepository<Parqueo, Integer> {

    List<Parqueo> findAllByOrderByCodigoAsc();

    Optional<Parqueo> findByCodigoIgnoreCase(String codigo);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM visita_parqueo INNER JOIN visita ON visita.id_visita=visita_parqueo.id_visita WHERE visita_parqueo.id_parqueo=:idParqueo AND visita.fecha_salida IS NULL")
    long contarVisitasAbiertas(Integer idParqueo);
}
