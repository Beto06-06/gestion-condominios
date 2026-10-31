package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParqueoService {

    private final ParqueoRepository parqueoRepository;
    private final TipoParqueoRepository tipoParqueoRepository;
    private final ParqueoFilialRepository parqueoFilialRepository;
    private final FilialRepository filialRepository;

    private Parqueo buscar(Integer idParqueo) {
        var resultado = parqueoRepository.findById(idParqueo);
        if (!resultado.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return resultado.get();
    }

    private void preparar(Parqueo parqueo) {
        var asignacion = parqueoFilialRepository.findByParqueoIdParqueo(parqueo.getIdParqueo());
        parqueo.setFilialAsignada(asignacion.isPresent() ? asignacion.get().getFilial() : null);
        boolean ocupado = parqueoRepository.contarVisitasAbiertas(parqueo.getIdParqueo()) > 0;
        parqueo.setBloqueado(asignacion.isPresent() || ocupado);
        if (ocupado) {
            parqueo.setEstado("parqueo.ocupado");
        } else if (!parqueo.isActivo()) {
            parqueo.setEstado("estado.inactivo");
        } else if (asignacion.isPresent()) {
            parqueo.setEstado("parqueo.asignado");
        } else {
            parqueo.setEstado("parqueo.disponible");
        }
    }

    @Transactional(readOnly = true)
    public List<Parqueo> listar() {
        var parqueos = parqueoRepository.findAllByOrderByCodigoAsc();
        for (Parqueo parqueo : parqueos) {
            preparar(parqueo);
        }
        return parqueos;
    }

    @Transactional(readOnly = true)
    public Parqueo get(Integer idParqueo) {
        var parqueo = buscar(idParqueo);
        preparar(parqueo);
        return parqueo;
    }

    @Transactional(readOnly = true)
    public List<TipoParqueo> getTipos() {
        return tipoParqueoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TipoParqueo getTipo(Integer idTipoParqueo) {
        if (idTipoParqueo == null) {
            return null;
        }
        var resultado = tipoParqueoRepository.findById(idTipoParqueo);
        return resultado.orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ParqueoFilial> porFilial(Integer idFilial) {
        return parqueoFilialRepository.findByFilialIdFilialOrderByParqueoCodigoAsc(idFilial);
    }

    private TipoParqueo buscarTipo(Integer idTipoParqueo) {
        var tipo = getTipo(idTipoParqueo);
        if (tipo == null) {
            throw new IllegalArgumentException("error.campos");
        }
        return tipo;
    }

    private void validarCodigo(String codigo, Integer idParqueo) {
        var duplicado = parqueoRepository.findByCodigoIgnoreCase(codigo);
        if (duplicado.isPresent() && !Objects.equals(duplicado.get().getIdParqueo(), idParqueo)) {
            throw new IllegalArgumentException("error.parqueoDuplicado");
        }
    }

    private void validarCambio(Parqueo parqueo, Parqueo formulario, Integer idTipoParqueo) {
        if (parqueo.getIdParqueo() == null) {
            return;
        }
        preparar(parqueo);
        if (!parqueo.isBloqueado()) {
            return;
        }
        if (!parqueo.getTipoParqueo().getIdTipoParqueo().equals(idTipoParqueo)) {
            throw new IllegalStateException("error.parqueoUsado");
        }
        if (parqueo.isActivo() && !formulario.isActivo()) {
            throw new IllegalStateException("error.parqueoUsado");
        }
    }

    @Transactional
    public void save(Parqueo formulario, Integer idTipoParqueo) {
        String codigo = PersonaService.texto(formulario.getCodigo()).toUpperCase(Locale.ROOT);
        var tipo = buscarTipo(idTipoParqueo);
        validarCodigo(codigo, formulario.getIdParqueo());
        var parqueo = formulario.getIdParqueo() == null ? new Parqueo() : buscar(formulario.getIdParqueo());
        validarCambio(parqueo, formulario, idTipoParqueo);
        parqueo.setCodigo(codigo);
        parqueo.setTipoParqueo(tipo);
        parqueo.setActivo(formulario.isActivo());
        parqueoRepository.save(parqueo);
        parqueoRepository.flush();
    }

    private void validarAsignacion(Parqueo parqueo) {
        if (!parqueo.isActivo()) {
            throw new IllegalStateException("error.parqueoAsignar");
        }
        if (!"Residente".equals(parqueo.getTipoParqueo().getNombre())) {
            throw new IllegalStateException("error.parqueoAsignar");
        }
        if (parqueoRepository.contarVisitasAbiertas(parqueo.getIdParqueo()) > 0) {
            throw new IllegalStateException("error.parqueoAsignar");
        }
    }

    private Filial buscarFilial(Integer idFilial) {
        if (idFilial == null) {
            throw new IllegalArgumentException("error.campos");
        }
        var resultado = filialRepository.findById(idFilial);
        if (!resultado.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return resultado.get();
    }

    @Transactional
    public void asignar(Integer idParqueo, Integer idFilial) {
        var parqueo = buscar(idParqueo);
        validarAsignacion(parqueo);
        var filial = buscarFilial(idFilial);
        var resultado = parqueoFilialRepository.findByParqueoIdParqueo(idParqueo);
        var asignacion = resultado.isPresent() ? resultado.get() : new ParqueoFilial();
        asignacion.setParqueo(parqueo);
        asignacion.setFilial(filial);
        parqueoFilialRepository.save(asignacion);
        parqueoFilialRepository.flush();
    }

    @Transactional
    public void liberar(Integer idParqueo) {
        buscar(idParqueo);
        var resultado = parqueoFilialRepository.findByParqueoIdParqueo(idParqueo);
        if (!resultado.isPresent()) {
            throw new IllegalArgumentException("error.parqueoSinAsignacion");
        }
        if (parqueoRepository.contarVisitasAbiertas(idParqueo) > 0) {
            throw new IllegalStateException("error.parqueoUsado");
        }
        parqueoFilialRepository.deleteById(resultado.get().getIdParqueoFilial());
        parqueoFilialRepository.flush();
    }

    @Transactional
    public void delete(Integer idParqueo) {
        if (!parqueoRepository.existsById(idParqueo)) {
            throw new IllegalArgumentException("error.noExiste");
        }
        try {
            parqueoRepository.deleteById(idParqueo);
            parqueoRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("error.parqueoAsociado", exception);
        }
    }
}
