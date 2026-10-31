package com.incondo.repository;

import com.incondo.domain.TipoParqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoParqueoRepository extends JpaRepository<TipoParqueo, Integer> {
}
