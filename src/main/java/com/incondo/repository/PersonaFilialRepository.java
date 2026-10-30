package com.incondo.repository;

import com.incondo.domain.PersonaFilial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaFilialRepository extends JpaRepository<PersonaFilial, Integer> {

    java.util.List<PersonaFilial> findByFilialIdFilialOrderByPersonaNombreCompletoAsc(Integer idFilial);

    java.util.List<PersonaFilial> findByPersonaIdPersonaOrderByFilialNumeroAsc(Integer idPersona);

    boolean existsByPersonaIdPersonaAndFilialIdFilialAndIdPersonaFilialNot(Integer idPersona, Integer idFilial, Integer idPersonaFilial);

    boolean existsByFilialIdFilialAndViveEnFilialTrue(Integer idFilial);

    boolean existsByFilialIdFilialAndTipoRelacionNombreAndViveEnFilialTrue(Integer idFilial, String nombreTipoRelacion);
}
