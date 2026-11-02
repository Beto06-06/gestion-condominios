package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FilialService {

    private final FilialRepository filialRepository;
    private final TorreRepository torreRepository;
    private final TipoFilialRepository tipoFilialRepository;
    private final PersonaFilialRepository personaFilialRepository;
    private final AlquilerRepository alquilerRepository;

    @Transactional(readOnly = true)
    public List<Filial> listar() {
        var filiales = filialRepository.findAllByOrderByNumeroAsc();
        for (Filial filial : filiales) {
            ocupacion(filial);
        }
        return filiales;
    }

    @Transactional(readOnly = true)
    public Filial get(Integer idFilial) {
        var busquedaFilial = filialRepository.findById(idFilial);
        if (!busquedaFilial.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        var filial = busquedaFilial.get();
        ocupacion(filial);
        return filial;
    }

    private void ocupacion(Filial filial) {
        String estado = "ocupacion.desocupada";
        if (alquilerRepository.contarVigentes(filial.getIdFilial(), LocalDate.now()) > 0) {
            estado = "ocupacion.inquilino";
        } else if (personaFilialRepository.existsByFilialIdFilialAndTipoRelacionNombreAndViveEnFilialTrue(filial.getIdFilial(), "Propietario")) {
            estado = "ocupacion.propietario";
        } else if (personaFilialRepository.existsByFilialIdFilialAndViveEnFilialTrue(filial.getIdFilial())) {
            estado = "ocupacion.residente";
        }
        filial.setOcupacion(estado);
    }

    @Transactional
    public Filial save(Filial filialFormulario, Integer idTorre, Integer idTipoFilial) {
        String numero = PersonaService.texto(filialFormulario.getNumero()).toUpperCase(Locale.ROOT);
        var filialExistente = filialRepository.findByNumeroIgnoreCase(numero);
        if (filialExistente.isPresent() && !Objects.equals(filialExistente.get().getIdFilial(), filialFormulario.getIdFilial())) {
            throw new IllegalArgumentException("error.filialDuplicada");
        }
        var filial = filialFormulario.getIdFilial() == null ? new Filial() : get(filialFormulario.getIdFilial());
        filial.setNumero(numero);
        filial.setActivo(filialFormulario.isActivo());
        var busquedaTorre = torreRepository.findById(idTorre);
        if (!busquedaTorre.isPresent()) {
            throw new IllegalArgumentException("error.campos");
        }
        filial.setTorre(busquedaTorre.get());
        var busquedaTipo = tipoFilialRepository.findById(idTipoFilial);
        if (!busquedaTipo.isPresent()) {
            throw new IllegalArgumentException("error.campos");
        }
        filial.setTipoFilial(busquedaTipo.get());
        filial = filialRepository.save(filial);
        filialRepository.flush();
        return filial;
    }

    @Transactional(readOnly = true)
    public List<Torre> getTorres() {
        return torreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Torre> getTorre(Integer idTorre) {
        return torreRepository.findById(idTorre);
    }

    @Transactional(readOnly = true)
    public List<TipoFilial> getTiposFilial() {
        return tipoFilialRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<TipoFilial> getTipoFilial(Integer idTipoFilial) {
        return tipoFilialRepository.findById(idTipoFilial);
    }

    @Transactional
    public void delete(Integer idFilial) {
        if (!filialRepository.existsById(idFilial)) {
            throw new IllegalArgumentException("La filial no existe.");
        }
        try {
            filialRepository.deleteById(idFilial);
            filialRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("La filial tiene datos asociados.", exception);
        }
    }

}
