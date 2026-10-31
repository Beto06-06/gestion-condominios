package com.incondo.repository;

import com.incondo.domain.ParqueoFilial;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParqueoFilialRepository extends JpaRepository<ParqueoFilial, Integer> {

    Optional<ParqueoFilial> findByParqueoIdParqueo(Integer idParqueo);

    List<ParqueoFilial> findByFilialIdFilialOrderByParqueoCodigoAsc(Integer idFilial);
}
