package com.incondo.repository;

import com.incondo.domain.Torre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TorreRepository extends JpaRepository<Torre, Integer> {

}
