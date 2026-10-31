package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final AlquilerInquilinoRepository alquilerInquilinoRepository;
    private final PersonaRepository personaRepository;
    private final PersonaFilialRepository personaFilialRepository;
    private final FilialRepository filialRepository;

    private Alquiler buscar(Integer idAlquiler) {
        var resultado = alquilerRepository.findById(idAlquiler);
        if (!resultado.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return resultado.get();
    }

    private void preparar(Alquiler alquiler) {
        List<Persona> inquilinos = new ArrayList<>();
        for (AlquilerInquilino relacion : alquilerInquilinoRepository.findByAlquilerIdAlquilerOrderByPersonaNombreCompletoAsc(alquiler.getIdAlquiler())) {
            inquilinos.add(relacion.getPersona());
        }
        alquiler.setInquilinos(inquilinos);
        LocalDate hoy = LocalDate.now();
        alquiler.setEditable(alquiler.getFechaSalida() == null);
        alquiler.setEliminable(alquiler.getFechaSalida() == null && alquiler.getFechaInicio().isAfter(hoy));
        alquiler.setFinalizable(alquiler.getFechaSalida() == null && !alquiler.getFechaInicio().isAfter(hoy));
        if (alquiler.getFechaSalida() != null) {
            alquiler.setEstado("alquiler.finalizado");
        } else if (alquiler.getFechaInicio().isAfter(hoy)) {
            alquiler.setEstado("alquiler.programado");
        } else if (alquiler.getFechaFin().isBefore(hoy)) {
            alquiler.setEstado("alquiler.vencido");
        } else {
            alquiler.setEstado("alquiler.vigente");
        }
    }

    @Transactional(readOnly = true)
    public List<Alquiler> listar(String busqueda) {
        var alquileres = alquilerRepository.findAllByOrderByFechaInicioDescIdAlquilerDesc();
        List<Alquiler> resultado = new ArrayList<>();
        String termino = PersonaService.texto(busqueda).toUpperCase(Locale.ROOT);
        for (Alquiler alquiler : alquileres) {
            preparar(alquiler);
            boolean incluir = alquiler.getFilial().getNumero().toUpperCase(Locale.ROOT).contains(termino);
            for (Persona persona : alquiler.getInquilinos()) {
                if (persona.getNombreCompleto().toUpperCase(Locale.ROOT).contains(termino) || persona.getCedula().contains(PersonaService.cedulaNormalizada(busqueda))) {
                    incluir = true;
                }
            }
            if (incluir) {
                resultado.add(alquiler);
            }
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<Alquiler> porFilial(Integer idFilial) {
        var alquileres = alquilerRepository.findByFilialIdFilialOrderByFechaInicioDescIdAlquilerDesc(idFilial);
        for (Alquiler alquiler : alquileres) {
            preparar(alquiler);
        }
        return alquileres;
    }

    @Transactional(readOnly = true)
    public Alquiler get(Integer idAlquiler) {
        var alquiler = buscar(idAlquiler);
        preparar(alquiler);
        return alquiler;
    }

    @Transactional(readOnly = true)
    public Alquiler getEditable(Integer idAlquiler) {
        var alquiler = get(idAlquiler);
        if (!alquiler.isEditable()) {
            throw new IllegalStateException("error.alquilerCerrado");
        }
        return alquiler;
    }

    @Transactional(readOnly = true)
    public Alquiler getFinalizable(Integer idAlquiler) {
        var alquiler = get(idAlquiler);
        if (alquiler.getFechaSalida() != null) {
            throw new IllegalStateException("error.alquilerCerrado");
        }
        if (!alquiler.isFinalizable()) {
            throw new IllegalStateException("error.alquilerNoFinalizar");
        }
        return alquiler;
    }

    private void validarFormulario(Alquiler formulario) {
        if (!formulario.getFechaFin().isAfter(formulario.getFechaInicio())) {
            throw new IllegalArgumentException("error.alquilerFechas");
        }
        BigDecimal monto = formulario.getMontoMensual();
        if (monto.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException("error.alquilerMonto");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("error.alquilerMonto");
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

    private void validarPropietario(Integer idPersona, List<PersonaFilial> relaciones) {
        for (PersonaFilial relacion : relaciones) {
            boolean mismaPersona = relacion.getPersona().getIdPersona().equals(idPersona);
            boolean propietario = "Propietario".equals(relacion.getTipoRelacion().getNombre());
            if (mismaPersona && propietario) {
                throw new IllegalArgumentException("error.alquilerPropietario");
            }
        }
    }

    private List<Persona> validarInquilinos(Integer idFilial, List<Integer> idsInquilinos) {
        if (idsInquilinos == null || idsInquilinos.isEmpty()) {
            throw new IllegalArgumentException("error.alquilerInquilinos");
        }
        List<Persona> personas = new ArrayList<>();
        List<Integer> idsVerificados = new ArrayList<>();
        var relaciones = personaFilialRepository.findByFilialIdFilialOrderByPersonaNombreCompletoAsc(idFilial);
        for (Integer idPersona : idsInquilinos) {
            if (idPersona == null || idsVerificados.contains(idPersona)) {
                throw new IllegalArgumentException("error.alquilerInquilinos");
            }
            var resultado = personaRepository.findById(idPersona);
            if (!resultado.isPresent()) {
                throw new IllegalArgumentException("error.noExiste");
            }
            validarPropietario(idPersona, relaciones);
            idsVerificados.add(idPersona);
            personas.add(resultado.get());
        }
        return personas;
    }

    private boolean contienePersona(List<Persona> personas, Integer idPersona) {
        for (Persona persona : personas) {
            if (persona.getIdPersona().equals(idPersona)) {
                return true;
            }
        }
        return false;
    }

    private boolean tieneInquilino(List<AlquilerInquilino> relaciones, Integer idPersona) {
        for (AlquilerInquilino relacion : relaciones) {
            if (relacion.getPersona().getIdPersona().equals(idPersona)) {
                return true;
            }
        }
        return false;
    }

    private void guardarInquilinos(Alquiler alquiler, List<Persona> personas) {
        var actuales = alquilerInquilinoRepository.findByAlquilerIdAlquilerOrderByPersonaNombreCompletoAsc(alquiler.getIdAlquiler());
        for (AlquilerInquilino relacion : actuales) {
            if (!contienePersona(personas, relacion.getPersona().getIdPersona())) {
                alquilerInquilinoRepository.deleteById(relacion.getIdAlquilerInquilino());
            }
        }
        alquilerInquilinoRepository.flush();
        for (Persona persona : personas) {
            if (!tieneInquilino(actuales, persona.getIdPersona())) {
                var relacion = new AlquilerInquilino();
                relacion.setAlquiler(alquiler);
                relacion.setPersona(persona);
                alquilerInquilinoRepository.save(relacion);
            }
        }
        alquilerInquilinoRepository.flush();
    }

    @Transactional
    public void save(Alquiler formulario, Integer idFilial, List<Integer> idsInquilinos) {
        var filial = buscarFilial(idFilial);
        validarFormulario(formulario);
        Alquiler alquiler = formulario.getIdAlquiler() == null ? new Alquiler() : getEditable(formulario.getIdAlquiler());
        if (alquiler.getIdAlquiler() != null && !alquiler.getFilial().getIdFilial().equals(idFilial)) {
            throw new IllegalArgumentException("error.alquilerFilial");
        }
        var personas = validarInquilinos(idFilial, idsInquilinos);
        Integer idAlquiler = alquiler.getIdAlquiler() == null ? 0 : alquiler.getIdAlquiler();
        if (alquilerRepository.contarTraslapes(idFilial, idAlquiler, formulario.getFechaInicio(), formulario.getFechaFin()) > 0) {
            throw new IllegalArgumentException("error.alquilerTraslape");
        }
        alquiler.setFilial(filial);
        alquiler.setFechaInicio(formulario.getFechaInicio());
        alquiler.setFechaFin(formulario.getFechaFin());
        alquiler.setMontoMensual(formulario.getMontoMensual());
        alquiler = alquilerRepository.save(alquiler);
        alquilerRepository.flush();
        guardarInquilinos(alquiler, personas);
    }

    @Transactional
    public void finalizar(Integer idAlquiler, LocalDate fechaSalida) {
        var alquiler = getFinalizable(idAlquiler);
        if (fechaSalida == null || fechaSalida.isBefore(alquiler.getFechaInicio()) || fechaSalida.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("error.alquilerSalida");
        }
        if (alquilerRepository.contarTraslapes(alquiler.getFilial().getIdFilial(), idAlquiler, alquiler.getFechaInicio(), fechaSalida) > 0) {
            throw new IllegalArgumentException("error.alquilerTraslape");
        }
        alquiler.setFechaSalida(fechaSalida);
        alquilerRepository.save(alquiler);
        alquilerRepository.flush();
    }

    @Transactional
    public void delete(Integer idAlquiler) {
        if (!alquilerRepository.existsById(idAlquiler)) {
            throw new IllegalArgumentException("error.noExiste");
        }
        var alquiler = buscar(idAlquiler);
        if (alquiler.getFechaSalida() != null || !alquiler.getFechaInicio().isAfter(LocalDate.now())) {
            throw new IllegalStateException("error.alquilerEliminar");
        }
        try {
            for (AlquilerInquilino relacion : alquilerInquilinoRepository.findByAlquilerIdAlquilerOrderByPersonaNombreCompletoAsc(idAlquiler)) {
                alquilerInquilinoRepository.deleteById(relacion.getIdAlquilerInquilino());
            }
            alquilerInquilinoRepository.flush();
            alquilerRepository.deleteById(idAlquiler);
            alquilerRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("error.alquilerAsociado", exception);
        }
    }
}
