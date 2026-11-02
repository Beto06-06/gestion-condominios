package com.incondo.controller;

import com.incondo.domain.AreaComun;
import com.incondo.service.AreaComunService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/area")
public class AreaComunController {

    private final AreaComunService areaComunService;
    private final MessageSource messageSource;

    private String mensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, messageSource.getMessage("error.operacion", null, locale), locale);
    }

    private void cargar(Model model, boolean soloActivas) {
        model.addAttribute("areas", areaComunService.getAreas(soloActivas));
        model.addAttribute("soloActivas", soloActivas);
        model.addAttribute("rutaIdioma", "/area/listado");
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(required = false) Integer editar,
            @RequestParam(defaultValue = "false") boolean soloActivas, Model model, Locale locale) {
        cargar(model, soloActivas);
        AreaComun area = new AreaComun();
        if (editar != null) {
            try {
                area = areaComunService.get(editar);
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", mensaje(exception.getMessage(), locale));
            }
        }
        model.addAttribute("areaComun", area);
        model.addAttribute("editar", area.getIdAreaComun());
        return "area/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid AreaComun areaComun, BindingResult bindingResult,
            @RequestParam(defaultValue = "false") boolean soloActivas,
            Model model, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.guardado";
        if (!bindingResult.hasErrors()) {
            try {
                areaComunService.save(areaComun);
            } catch (IllegalArgumentException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (IllegalStateException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (DataIntegrityViolationException exception) {
                titulo = "error";
                clave = "error.areaGuardar";
            } catch (Exception exception) {
                titulo = "error";
                clave = "error.operacion";
            }
            if ("todoOk".equals(titulo)) {
                redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
                redirectAttributes.addAttribute("soloActivas", soloActivas);
                return "redirect:/area/listado";
            }
            model.addAttribute(titulo, mensaje(clave, locale));
        }
        cargar(model, soloActivas);
        model.addAttribute("editar", areaComun.getIdAreaComun());
        return "area/listado";
    }

    @PostMapping("/estado")
    public String estado(@RequestParam Integer idAreaComun,
            @RequestParam(defaultValue = "false") boolean soloActivas,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.areaEstado";
        try {
            areaComunService.cambiarEstado(idAreaComun);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (IllegalStateException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (Exception exception) {
            titulo = "error";
            clave = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
        redirectAttributes.addAttribute("soloActivas", soloActivas);
        return "redirect:/area/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idAreaComun,
            @RequestParam(defaultValue = "false") boolean soloActivas,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.registroEliminado";
        try {
            areaComunService.delete(idAreaComun);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (IllegalStateException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (Exception exception) {
            titulo = "error";
            clave = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
        redirectAttributes.addAttribute("soloActivas", soloActivas);
        return "redirect:/area/listado";
    }
}
