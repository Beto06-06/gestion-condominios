package com.incondo.controller;

import com.incondo.domain.*;
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
@RequestMapping("/filial")
public class FilialController {

    private final FilialService filialService;
    private final PersonaService personaService;
    private final RelacionService relacionService;
    private final MessageSource messageSource;
    private final AlquilerService alquilerService;
    private final ParqueoService parqueoService;

    private void cargarDatosListado(Model model) {
        model.addAttribute("filiales", filialService.listar());
        model.addAttribute("torres", filialService.getTorres());
        model.addAttribute("tipos", filialService.getTiposFilial());
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(required = false) Integer editar, Model model) {
        cargarDatosListado(model);
        model.addAttribute("editar", editar);
        model.addAttribute("filial", editar == null ? new Filial() : filialService.get(editar));
        return "filial/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Filial filial, BindingResult bindingResult, @RequestParam Integer idTorre, @RequestParam Integer idTipoFilial, Model model, Locale locale, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) try {
            filialService.save(filial, idTorre, idTipoFilial);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.guardado", null, locale));
            return "redirect:/filial/listado";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", messageSource.getMessage("error.filialDuplicada", null, locale));
        }
        cargarDatosListado(model);
        model.addAttribute("editar", filial.getIdFilial());
        model.addAttribute("rutaIdioma", "/filial/listado");
        filial.setTorre(filialService.getTorre(idTorre).orElse(null));
        filial.setTipoFilial(filialService.getTipoFilial(idTipoFilial).orElse(null));
        return "filial/listado";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Integer idFilial, @RequestParam(required = false) Integer editar, Model model) {
        model.addAttribute("editar", editar);
        model.addAttribute("filial", filialService.get(idFilial));
        model.addAttribute("relaciones", relacionService.porFilial(idFilial));
        model.addAttribute("personas", personaService.listar(""));
        model.addAttribute("tiposRelacion", relacionService.getTiposRelacion());
        model.addAttribute("historialAlquileres", alquilerService.porFilial(idFilial));
        model.addAttribute("parqueosFilial", parqueoService.porFilial(idFilial));
        model.addAttribute("hoy", java.time.LocalDate.now());
        if (editar != null) {
            model.addAttribute("relacionForm", relacionService.getRelacion(editar, idFilial));
        } else {
            model.addAttribute("relacionForm", new PersonaFilial());
        }
        return "filial/detalle";
    }

    @PostMapping("/asociar")
    public String asociar(@RequestParam(required = false) Integer idPersonaFilial, @RequestParam Integer idPersona, @RequestParam Integer idFilial, @RequestParam Integer idTipoRelacion, @RequestParam(defaultValue = "false") boolean viveEnFilial, Locale locale, Model model, RedirectAttributes redirectAttributes) {
        try {
            relacionService.save(idPersonaFilial, idPersona, idFilial, idTipoRelacion, viveEnFilial);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.guardado", null, locale));
            return "redirect:/filial/detalle/" + idFilial;
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        } catch (DataIntegrityViolationException exception) {
            model.addAttribute("error", messageSource.getMessage("error.relacionDuplicada", null, locale));
        }
        model.addAttribute("filial", filialService.get(idFilial));
        model.addAttribute("relaciones", relacionService.porFilial(idFilial));
        model.addAttribute("personas", personaService.listar(""));
        model.addAttribute("tiposRelacion", relacionService.getTiposRelacion());
        model.addAttribute("historialAlquileres", alquilerService.porFilial(idFilial));
        model.addAttribute("parqueosFilial", parqueoService.porFilial(idFilial));
        model.addAttribute("hoy", java.time.LocalDate.now());
        model.addAttribute("editar", idPersonaFilial);
        model.addAttribute("rutaIdioma", "/filial/detalle/" + idFilial);
        var relacion = new PersonaFilial();
        relacion.setIdPersonaFilial(idPersonaFilial);
        var personaSeleccionada = new Persona();
        personaSeleccionada.setIdPersona(idPersona);
        relacion.setPersona(personaSeleccionada);
        relacion.setTipoRelacion(relacionService.getTipoRelacion(idTipoRelacion).orElse(null));
        relacion.setViveEnFilial(viveEnFilial);
        model.addAttribute("relacionForm", relacion);
        return "filial/detalle";
    }

    @PostMapping("/desasociar")
    public String desasociar(@RequestParam Integer idPersonaFilial, @RequestParam Integer idFilial, Locale locale, RedirectAttributes redirectAttributes) {
        try {
            relacionService.eliminar(idPersonaFilial, idFilial);
            redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.eliminado", null, locale));
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("error.relacionUsada", null, locale));
        }
        return "redirect:/filial/detalle/" + idFilial;
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idFilial,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String mensaje = "mensaje.registroEliminado";
        try {
            filialService.delete(idFilial);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            mensaje = "error.noExiste";
        } catch (IllegalStateException exception) {
            titulo = "error";
            mensaje = "error.filialAsociado";
        } catch (Exception exception) {
            titulo = "error";
            mensaje = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return "redirect:/filial/listado";
    }

}
