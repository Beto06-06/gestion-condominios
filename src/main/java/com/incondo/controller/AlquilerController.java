package com.incondo.controller;

import com.incondo.domain.Alquiler;
import com.incondo.domain.Persona;
import com.incondo.service.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
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
@RequestMapping("/alquiler")
public class AlquilerController {

    private final AlquilerService alquilerService;
    private final FilialService filialService;
    private final PersonaService personaService;
    private final MessageSource messageSource;

    private String mensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, messageSource.getMessage("error.operacion", null, locale), locale);
    }

    private void cargar(Model model, String busqueda) {
        model.addAttribute("alquileres", alquilerService.listar(busqueda));
        model.addAttribute("filiales", filialService.listar());
        model.addAttribute("personas", personaService.listar(""));
        model.addAttribute("q", busqueda);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("rutaIdioma", "/alquiler/listado");
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(defaultValue = "", name = "q") String busqueda,
            @RequestParam(required = false) Integer editar,
            @RequestParam(required = false) Integer finalizar, Model model, Locale locale) {
        cargar(model, busqueda);
        model.addAttribute("alquiler", new Alquiler());
        model.addAttribute("idsInquilinos", new ArrayList<Integer>());
        model.addAttribute("editar", null);
        String titulo = "error";
        String clave = null;
        try {
            if (editar != null) {
                var alquiler = alquilerService.getEditable(editar);
                List<Integer> seleccionados = new ArrayList<>();
                for (Persona persona : alquiler.getInquilinos()) {
                    seleccionados.add(persona.getIdPersona());
                }
                model.addAttribute("alquiler", alquiler);
                model.addAttribute("idFilialSeleccionada", alquiler.getFilial().getIdFilial());
                model.addAttribute("idsInquilinos", seleccionados);
                model.addAttribute("editar", alquiler.getIdAlquiler());
            }
            if (finalizar != null) {
                model.addAttribute("alquilerFinalizar", alquilerService.getFinalizable(finalizar));
                model.addAttribute("finalizar", finalizar);
                model.addAttribute("mostrarFinalizacion", true);
            }
        } catch (IllegalArgumentException exception) {
            clave = exception.getMessage();
        } catch (IllegalStateException exception) {
            clave = exception.getMessage();
        } catch (Exception exception) {
            clave = "error.operacion";
        }
        if (clave != null) {
            model.addAttribute(titulo, mensaje(clave, locale));
        }
        return "alquiler/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Alquiler alquiler, BindingResult bindingResult,
            @RequestParam(required = false) Integer idFilial,
            @RequestParam(required = false) List<Integer> idsInquilinos,
            @RequestParam(defaultValue = "", name = "q") String busqueda,
            Model model, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.guardado";
        if (!bindingResult.hasErrors()) {
            try {
                alquilerService.save(alquiler, idFilial, idsInquilinos);
            } catch (IllegalArgumentException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (IllegalStateException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (DataIntegrityViolationException exception) {
                titulo = "error";
                clave = "error.alquilerDuplicado";
            } catch (Exception exception) {
                titulo = "error";
                clave = "error.operacion";
            }
            if ("todoOk".equals(titulo)) {
                redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
                redirectAttributes.addAttribute("q", busqueda);
                return "redirect:/alquiler/listado";
            }
            model.addAttribute(titulo, mensaje(clave, locale));
        }
        cargar(model, busqueda);
        model.addAttribute("editar", alquiler.getIdAlquiler());
        model.addAttribute("idFilialSeleccionada", idFilial);
        model.addAttribute("idsInquilinos", idsInquilinos == null ? new ArrayList<Integer>() : idsInquilinos);
        return "alquiler/listado";
    }

    @PostMapping("/finalizar")
    public String finalizar(@RequestParam Integer idAlquiler,
            @RequestParam(defaultValue = "") String fechaSalida,
            @RequestParam(defaultValue = "") String retorno,
            @RequestParam(defaultValue = "", name = "q") String busqueda,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.alquilerFinalizado";
        Integer idFilial = null;
        try {
            idFilial = alquilerService.get(idAlquiler).getFilial().getIdFilial();
            alquilerService.finalizar(idAlquiler, LocalDate.parse(fechaSalida));
        } catch (DateTimeParseException exception) {
            titulo = "error";
            clave = "error.alquilerSalida";
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
        if ("error".equals(titulo)) {
            redirectAttributes.addFlashAttribute("fechaSalida", fechaSalida);
            redirectAttributes.addFlashAttribute("retorno", retorno);
            redirectAttributes.addAttribute("finalizar", idAlquiler);
        } else if ("filial".equals(retorno)) {
            return "redirect:/filial/detalle/" + idFilial;
        }
        redirectAttributes.addAttribute("q", busqueda);
        return "redirect:/alquiler/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idAlquiler, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.registroEliminado";
        try {
            alquilerService.delete(idAlquiler);
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
        return "redirect:/alquiler/listado";
    }
}
