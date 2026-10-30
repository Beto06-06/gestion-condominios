package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final PersonaService personaService;
    private final EmpleadoPuestoRepository empleadoPuestoRepository;
    private final PuestoRepository puestoRepository;
    private final TurnoRepository turnoRepository;

    @Transactional(readOnly = true)
    public List<Empleado> listar(Integer idPuesto) {
        List<Empleado> empleados = new ArrayList<>();
        for (Empleado empleado : empleadoRepository.findAllByOrderByPersonaNombreCompletoAsc()) {
            boolean incluir = idPuesto == null;
            for (EmpleadoPuesto asignacion : empleado.getAsignaciones()) {
                if (asignacion.getPuesto().getIdPuesto().equals(idPuesto)) {
                    incluir = true;
                }
            }
            if (incluir) {
                empleados.add(empleado);
            }
        }
        return empleados;
    }

    @Transactional(readOnly = true)
    public EmpleadoPuesto getAsignacion(Integer idEmpleadoPuesto) {
        var busquedaAsignacion = empleadoPuestoRepository.findById(idEmpleadoPuesto);
        if (!busquedaAsignacion.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return busquedaAsignacion.get();
    }

    @Transactional
    public void save(Integer idAsignacion, Persona personaFormulario, Integer idPuesto, Integer idTurno) {
        personaService.normalizar(personaFormulario);
        if (personaFormulario.getTelefono().isBlank()) {
            throw new IllegalArgumentException("error.telefonoEmpleado");
        }
        if (idPuesto == null || idTurno == null) {
            throw new IllegalArgumentException("error.campos");
        }
        var asignacion = idAsignacion == null ? new EmpleadoPuesto() : getAsignacion(idAsignacion);
        Persona persona;
        if (idAsignacion != null) {
            persona = asignacion.getEmpleado().getPersona();
            if (!PersonaService.cedulaNormalizada(persona.getCedula()).equals(personaFormulario.getCedula())) {
                throw new IllegalArgumentException("error.identidadEmpleado");
            }
        } else {
            var busquedaPersona = personaRepository.findByCedula(personaFormulario.getCedula());
            if (busquedaPersona.isPresent()) {
                persona = busquedaPersona.get();
            } else {
                persona = new Persona();
                persona.setCedula(personaFormulario.getCedula());
                persona.setNombreCompleto(personaFormulario.getNombreCompleto());
                persona.setTelefono(personaFormulario.getTelefono());
                persona = personaRepository.save(persona);
                personaRepository.flush();
            }
        }
        // Los datos compartidos se cambian desde Personas.
        if (!PersonaService.texto(persona.getNombreCompleto()).equalsIgnoreCase(personaFormulario.getNombreCompleto())
                || !PersonaService.texto(persona.getTelefono()).equals(personaFormulario.getTelefono())) {
            throw new IllegalArgumentException("error.personaExistente");
        }
        Empleado empleado;
        if (idAsignacion != null) {
            empleado = asignacion.getEmpleado();
        } else {
            var busquedaEmpleado = empleadoRepository.findByPersonaIdPersona(persona.getIdPersona());
            if (busquedaEmpleado.isPresent()) {
                empleado = busquedaEmpleado.get();
            } else {
                empleado = new Empleado();
                empleado.setPersona(persona);
                empleado = empleadoRepository.save(empleado);
                empleadoRepository.flush();
            }
        }
        if (empleadoPuestoRepository.existsByEmpleadoIdEmpleadoAndPuestoIdPuestoAndTurnoIdTurnoAndIdEmpleadoPuestoNot(empleado.getIdEmpleado(), idPuesto, idTurno, idAsignacion == null ? 0 : idAsignacion)) {
            throw new IllegalArgumentException("error.asignacionDuplicada");
        }
        var busquedaPuesto = puestoRepository.findById(idPuesto);
        var busquedaTurno = turnoRepository.findById(idTurno);
        if (!busquedaPuesto.isPresent() || !busquedaTurno.isPresent()) {
            throw new IllegalArgumentException("error.campos");
        }
        asignacion.setEmpleado(empleado);
        asignacion.setPuesto(busquedaPuesto.get());
        asignacion.setTurno(busquedaTurno.get());
        empleadoPuestoRepository.save(asignacion);
        empleadoPuestoRepository.flush();
    }

    @Transactional
    public void cambiarEstado(Integer idEmpleado) {
        var busquedaEmpleado = empleadoRepository.findById(idEmpleado);
        if (!busquedaEmpleado.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        var empleado = busquedaEmpleado.get();
        empleado.setActivo(!empleado.isActivo());
        empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public List<Puesto> getPuestos() {
        return puestoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Puesto> getPuesto(Integer idPuesto) {
        return puestoRepository.findById(idPuesto);
    }

    @Transactional(readOnly = true)
    public List<Turno> getTurnos() {
        return turnoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Turno> getTurno(Integer idTurno) {
        return turnoRepository.findById(idTurno);
    }

    @Transactional
    public void delete(Integer idEmpleado) {
        if (!empleadoRepository.existsById(idEmpleado)) {
            throw new IllegalArgumentException("El empleado no existe.");
        }
        try {
            var asignaciones = empleadoPuestoRepository.findByEmpleadoIdEmpleado(idEmpleado);
            for (EmpleadoPuesto asignacion : asignaciones) {
                empleadoPuestoRepository.deleteById(asignacion.getIdEmpleadoPuesto());
            }
            empleadoPuestoRepository.flush();
            empleadoRepository.deleteById(idEmpleado);
            empleadoRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("El empleado tiene datos asociados.", exception);
        }
    }

}
