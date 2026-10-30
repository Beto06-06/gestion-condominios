package com.incondo.repository;

import com.incondo.domain.TipoFilial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoFilialRepository extends JpaRepository<TipoFilial, Integer> {

}
