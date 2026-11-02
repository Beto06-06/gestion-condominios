package com.incondo.service;

import com.incondo.domain.*;
import com.incondo.repository.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitaService {

    private final PersonaRepository personaRepository;
    private final VisitanteRepository visitanteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final VisitaRepository visitaRepository;
    private final VisitaParqueoRepository visitaParqueoRepository;
    private final PersonaService personaService;
    private final FilialService filialService;
    private final EmpleadoService empleadoService;
    private final ParqueoService parqueoService;

    @Transactional(readOnly = true)
    public Optional<Persona> buscarPersona(String cedula) {
        return personaRepository.findByCedula(PersonaService.cedulaNormalizada(cedula));
    }

    @Transactional(readOnly = true)
    public boolean estaDentro(String cedula) {
        var resultado = visitanteRepository.findByPersonaCedula(PersonaService.cedulaNormalizada(cedula));
        return resultado.isPresent() && visitaRepository.existsByVisitanteIdVisitanteAndFechaSalidaIsNull(resultado.get().getIdVisitante());
    }

    private Persona obtenerPersona(Persona formulario) {
        var resultado = personaRepository.findByCedula(formulario.getCedula());
        if (resultado.isPresent()) {
            var persona = resultado.get();
            if (!PersonaService.texto(persona.getNombreCompleto()).equalsIgnoreCase(formulario.getNombreCompleto())) {
                throw new IllegalArgumentException("error.visitaIdentidad");
            }
            return persona;
        }
        var persona = new Persona();
        persona.setCedula(formulario.getCedula());
        persona.setNombreCompleto(formulario.getNombreCompleto());
        persona = personaRepository.save(persona);
        personaRepository.flush();
        return persona;
    }

    private Visitante obtenerVisitante(Persona persona) {
        var resultado = visitanteRepository.findByPersonaCedula(persona.getCedula());
        if (resultado.isPresent()) {
            return resultado.get();
        }
        var visitante = new Visitante();
        visitante.setPersona(persona);
        visitante = visitanteRepository.save(visitante);
        visitanteRepository.flush();
        return visitante;
    }

    private Parqueo validarVehiculo(boolean llegaEnVehiculo, String placa, Integer idParqueo) {
        if (!llegaEnVehiculo) {
            if (!PersonaService.texto(placa).isEmpty() || idParqueo != null) {
                throw new IllegalArgumentException("error.visitaSinVehiculo");
            }
            return null;
        }
        if (PersonaService.texto(placa).isEmpty()) {
            throw new IllegalArgumentException("error.visitaPlaca");
        }
        if (PersonaService.texto(placa).length() > 20) {
            throw new IllegalArgumentException("error.visitaPlaca");
        }
        return parqueoService.getDisponibleVisita(idParqueo);
    }

    private Vehiculo obtenerVehiculo(String placa) {
        String placaNormalizada = PersonaService.texto(placa).toUpperCase(Locale.ROOT);
        var resultado = vehiculoRepository.findByPlacaIgnoreCase(placaNormalizada);
        if (resultado.isPresent()) {
            return resultado.get();
        }
        var vehiculo = new Vehiculo();
        vehiculo.setPlaca(placaNormalizada);
        vehiculo = vehiculoRepository.save(vehiculo);
        vehiculoRepository.flush();
        return vehiculo;
    }

    private void guardarParqueo(Visita visita, Parqueo parqueo) {
        if (parqueo == null) {
            return;
        }
        var asignacion = new VisitaParqueo();
        asignacion.setVisita(visita);
        asignacion.setParqueo(parqueo);
        visitaParqueoRepository.save(asignacion);
        visitaParqueoRepository.flush();
    }

    @Transactional
    public Visita save(Persona formulario, Integer idFilial, Integer idEmpleadoPuesto,
            boolean llegaEnVehiculo, String placa, Integer idParqueo) {
        personaService.normalizar(formulario);
        if (idFilial == null) {
            throw new IllegalArgumentException("error.campos");
        }
        var filial = filialService.get(idFilial);
        var guarda = empleadoService.getGuarda(idEmpleadoPuesto);
        var parqueo = validarVehiculo(llegaEnVehiculo, placa, idParqueo);
        var persona = obtenerPersona(formulario);
        var visitante = obtenerVisitante(persona);
        if (visitaRepository.existsByVisitanteIdVisitanteAndFechaSalidaIsNull(visitante.getIdVisitante())) {
            throw new IllegalStateException("error.visitanteDentro");
        }
        var visita = new Visita();
        visita.setVisitante(visitante);
        visita.setFilial(filial);
        visita.setEmpleadoPuesto(guarda);
        visita.setVehiculo(llegaEnVehiculo ? obtenerVehiculo(placa) : null);
        visita.setFechaIngreso(LocalDateTime.now().withNano(0));
        visita = visitaRepository.save(visita);
        visitaRepository.flush();
        guardarParqueo(visita, parqueo);
        return visita;
    }
}
