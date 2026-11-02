package com.incondo.service;

import com.incondo.domain.Persona;
import com.incondo.repository.PersonaRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    public static String cedulaNormalizada(String cedula) {
        return cedula == null ? "" : cedula.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
    }

    @Transactional(readOnly = true)
    public List<Persona> listar(String busqueda) {
        return personaRepository.findByNombreCompletoContainingIgnoreCaseOrCedulaContainingIgnoreCaseOrderByNombreCompletoAsc(texto(busqueda), cedulaNormalizada(busqueda));
    }

    @Transactional(readOnly = true)
    public Persona get(Integer idPersona) {
        var busquedaPersona = personaRepository.findById(idPersona);
        if (!busquedaPersona.isPresent()) {
            throw new IllegalArgumentException("error.noExiste");
        }
        return busquedaPersona.get();
    }

    public void normalizar(Persona persona) {
        persona.setCedula(cedulaNormalizada(persona.getCedula()));
        persona.setNombreCompleto(texto(persona.getNombreCompleto()));
        persona.setTelefono(texto(persona.getTelefono()));
        persona.setCorreo(texto(persona.getCorreo()).toLowerCase(Locale.ROOT));
        if (persona.getCedula().isBlank()) {
            throw new IllegalArgumentException("error.campos");
        }
    }

    @Transactional
    public Persona save(Persona persona) {
        normalizar(persona);
        if (personaRepository.existsByCedulaAndIdPersonaNot(persona.getCedula(), persona.getIdPersona() == null ? 0 : persona.getIdPersona())) {
            throw new IllegalArgumentException("error.cedulaDuplicada");
        }
        if (persona.getTelefono().isBlank() || persona.getCorreo().isBlank()) {
            throw new IllegalArgumentException("error.contacto");
        }
        Persona personaGuardada = persona.getIdPersona() == null ? new Persona() : get(persona.getIdPersona());
        personaGuardada.setCedula(persona.getCedula());
        personaGuardada.setNombreCompleto(persona.getNombreCompleto());
        personaGuardada.setTelefono(persona.getTelefono());
        personaGuardada.setCorreo(persona.getCorreo());
        personaGuardada = personaRepository.save(personaGuardada);
        personaRepository.flush();
        return personaGuardada;
    }

    @Transactional
    public void delete(Integer idPersona) {
        if (!personaRepository.existsById(idPersona)) {
            throw new IllegalArgumentException("La persona no existe.");
        }
        try {
            personaRepository.deleteById(idPersona);
            personaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException("La persona tiene datos asociados.", exception);
        }
    }

}
