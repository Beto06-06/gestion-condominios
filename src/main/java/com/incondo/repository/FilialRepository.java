package com.incondo.repository;

import com.incondo.domain.Filial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilialRepository extends JpaRepository<Filial, Integer> {

    java.util.Optional<Filial> findByNumeroIgnoreCase(String numero);

    java.util.List<Filial> findAllByOrderByNumeroAsc();
}
