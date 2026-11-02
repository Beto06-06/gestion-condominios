package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RelacionService {

    private final PersonaFilialRepository personaFilialRepository;
    private final AlquilerInquilinoRepository alquilerInquilinoRepository;
    private final PersonaRepository personaRepository;
    private final FilialRepository filialRepository;
    private final TipoRelacionRepository tipoRelacionRepository;

    @Transactional(readOnly = true)
    public List<PersonaFilial> porPersona(Integer idPersona) {
        return personaFilialRepository.findByPersonaIdPersonaOrderByFilialNumeroAsc(idPersona);
    }

    @Transactional(readOnly = true)
    public List<PersonaFilial> porFilial(Integer idFilial) {
        return personaFilialRepository.findByFilialIdFilialOrderByPersonaNombreCompletoAsc(idFilial);
    }

    @Transactional(readOnly = true)
    public List<AlquilerInquilino> alquileresVigentes(Integer idPersona) {
        var hoy = java.time.LocalDate.now();
        List<AlquilerInquilino> vigentes = new ArrayList<>();
        for (AlquilerInquilino relacion : alquilerInquilinoRepository.findByPersonaIdPersona(idPersona)) {
            Alquiler alquiler = relacion.getAlquiler();
            if (alquiler.getFechaSalida() == null
                    && !alquiler.getFechaInicio().isAfter(hoy)
                    && !alquiler.getFechaFin().isBefore(hoy)) {
                vigentes.add(relacion);
            }
        }
        return vigentes;
    }

    @Transactional
    public void save(Integer idPersonaFilial, Integer idPersona, Integer idFilial, Integer idTipoRelacion, boolean viveEnFilial) {
        if (personaFilialRepository.existsByPersonaIdPersonaAndFilialIdFilialAndIdPersonaFilialNot(idPersona, idFilial, idPersonaFilial == null ? 0 : idPersonaFilial)) {
            throw new IllegalArgumentException("error.relacionDuplicada");
        }
        PersonaFilial relacion;
        if (idPersonaFilial == null) {
            relacion = new PersonaFilial();
        } else {
            var busquedaRelacion = personaFilialRepository.findById(idPersonaFilial);
            if (!busquedaRelacion.isPresent()) {
                throw new IllegalArgumentException("error.noExiste");
            }
            relacion = busquedaRelacion.get();
        }
        if (idPersonaFilial != null && !relacion.getFilial().getIdFilial().equals(idFilial)) {
            throw new IllegalArgumentException("error.campos");
        }
        var busquedaPersona = personaRepository.findById(idPersona);
        if (!busquedaPersona.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        relacion.setPersona(busquedaPersona.get());
        var busquedaFilial = filialRepository.findById(idFilial);
        if (!busquedaFilial.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        relacion.setFilial(busquedaFilial.get());
        var busquedaTipo = tipoRelacionRepository.findById(idTipoRelacion);
        if (!busquedaTipo.isPresent()) {
            throw new IllegalArgumentException("error.campos");
        }
        relacion.setTipoRelacion(busquedaTipo.get());
        relacion.setViveEnFilial(viveEnFilial);
        personaFilialRepository.save(relacion);
        personaFilialRepository.flush();
    }

    @Transactional
    public void eliminar(Integer idPersonaFilial, Integer idFilial) {
        var busquedaRelacion = personaFilialRepository.findById(idPersonaFilial);
        if (!busquedaRelacion.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        var relacion = busquedaRelacion.get();
        if (!relacion.getFilial().getIdFilial().equals(idFilial)) {
            throw new IllegalArgumentException("error.campos");
        }
        personaFilialRepository.delete(relacion);
        personaFilialRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<TipoRelacion> getTiposRelacion() {
        return tipoRelacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TipoRelacion> getTipoRelacion(Integer idTipoRelacion) {
        return tipoRelacionRepository.findById(idTipoRelacion);
    }

    @Transactional(readOnly = true)
    public PersonaFilial getRelacion(Integer idPersonaFilial, Integer idFilial) {
        var busquedaRelacion = personaFilialRepository.findById(idPersonaFilial);
        if (!busquedaRelacion.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        var relacion = busquedaRelacion.get();
        if (!relacion.getFilial().getIdFilial().equals(idFilial)) {
            throw new IllegalArgumentException("error.campos");
        }
        return relacion;
    }
}
