package com.incondo.controller;

import com.incondo.domain.Persona;
import com.incondo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/persona")
public class PersonaController {

    private final PersonaService personaService;
    private final RelacionService relacionService;
    private final MessageSource messageSource;

    @GetMapping("/listado")
    public String listado(@RequestParam(name = "q", defaultValue = "") String busqueda, @RequestParam(name = "ver", required = false) Integer idPersonaSeleccionada, Model model) {
        model.addAttribute("personas", personaService.listar(busqueda));
        model.addAttribute("q", busqueda);
        model.addAttribute("ver", idPersonaSeleccionada);
        if (idPersonaSeleccionada != null) {
            model.addAttribute("seleccionada", personaService.get(idPersonaSeleccionada));
            model.addAttribute("relaciones", relacionService.porPersona(idPersonaSeleccionada));
            model.addAttribute("alquileres", relacionService.alquileresVigentes(idPersonaSeleccionada));
        }
        return "persona/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("persona", new Persona());
        return "persona/formulario";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer idPersona, Model model) {
        model.addAttribute("persona", personaService.get(idPersona));
        return "persona/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Persona persona, BindingResult bindingResult, Model model, Locale locale, RedirectAttributes redirectAttributes) {
        model.addAttribute("rutaIdioma", persona.getIdPersona() == null ? "/persona/nuevo" : "/persona/modificar/" + persona.getIdPersona());
        if (bindingResult.hasErrors()) {
            return "persona/formulario";
        }
        try {
            var personaGuardada = personaService.save(persona);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.guardado", null, locale));
            return "redirect:/persona/listado?ver=" + personaGuardada.getIdPersona();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", messageSource.getMessage("error.cedulaDuplicada", null, locale));
        }
        return "persona/formulario";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idPersona,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String mensaje = "mensaje.registroEliminado";
        try {
            personaService.delete(idPersona);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            mensaje = "error.noExiste";
        } catch (IllegalStateException exception) {
            titulo = "error";
            mensaje = "error.personaAsociado";
        } catch (Exception exception) {
            titulo = "error";
            mensaje = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/persona/listado";
    }

}
