package com.incondo.service;

import com.incondo.domain.AreaComun;
import com.incondo.repository.AreaComunRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AreaComunService {

    private final AreaComunRepository areaComunRepository;

    @Transactional(readOnly = true)
    public List<AreaComun> getAreas(boolean soloActivas) {
        if (soloActivas) {
            return areaComunRepository.findByActivoTrueOrderByNombreAsc();
        }
        return areaComunRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public AreaComun get(Integer idAreaComun) {
        var resultado = areaComunRepository.findById(idAreaComun);
        if (!resultado.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return resultado.get();
    }

    @Transactional
    public void save(AreaComun formulario) {
        String nombre = PersonaService.texto(formulario.getNombre());
        var resultado = areaComunRepository.findByNombreIgnoreCase(nombre);
        if (resultado.isPresent() && !Objects.equals(resultado.get().getIdAreaComun(), formulario.getIdAreaComun())) {
            throw new IllegalArgumentException("error.areaDuplicada");
        }
        if (!formulario.getHoraCierre().isAfter(formulario.getHoraApertura())) {
            throw new IllegalArgumentException("error.areaHorario");
        }
        var area = formulario.getIdAreaComun() == null ? new AreaComun() : get(formulario.getIdAreaComun());
        area.setNombre(nombre);
        area.setDescripcion(formulario.getDescripcion().trim());
        area.setCapacidad(formulario.getCapacidad());
        area.setHoraApertura(formulario.getHoraApertura());
        area.setHoraCierre(formulario.getHoraCierre());
        area.setActivo(formulario.isActivo());
        areaComunRepository.save(area);
        areaComunRepository.flush();
    }

    @Transactional
    public void cambiarEstado(Integer idAreaComun) {
        var area = get(idAreaComun);
        area.setActivo(!area.isActivo());
        areaComunRepository.save(area);
        areaComunRepository.flush();
    }

    @Transactional
    public void delete(Integer idAreaComun) {
        if (!areaComunRepository.existsById(idAreaComun)) {
            throw new IllegalArgumentException("error.noExiste");
        }
        try {
            areaComunRepository.deleteById(idAreaComun);
            areaComunRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("error.areaAsociada", exception);
        }
    }
}
