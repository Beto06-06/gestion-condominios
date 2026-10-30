package com.incondo.repository;

import com.incondo.domain.TipoRelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRelacionRepository extends JpaRepository<TipoRelacion, Integer> {

}
