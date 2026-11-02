package com.incondo.repository;

import com.incondo.domain.VisitaParqueo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitaParqueoRepository extends JpaRepository<VisitaParqueo, Integer> {
}
