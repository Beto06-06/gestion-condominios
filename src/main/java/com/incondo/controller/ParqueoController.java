package com.incondo.controller;

import com.incondo.domain.Parqueo;
import com.incondo.service.*;
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
@RequestMapping("/parqueo")
public class ParqueoController {

    private final ParqueoService parqueoService;
    private final FilialService filialService;
    private final MessageSource messageSource;

    private String mensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, messageSource.getMessage("error.operacion", null, locale), locale);
    }

    private void cargar(Model model) {
        model.addAttribute("parqueos", parqueoService.listar());
        model.addAttribute("tiposParqueo", parqueoService.getTipos());
        model.addAttribute("filiales", filialService.listar());
        model.addAttribute("rutaIdioma", "/parqueo/listado");
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(required = false) Integer editar, Model model, Locale locale) {
        cargar(model);
        Parqueo parqueo = new Parqueo();
        if (editar != null) {
            try {
                parqueo = parqueoService.get(editar);
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", mensaje(exception.getMessage(), locale));
            }
        }
        model.addAttribute("parqueo", parqueo);
        model.addAttribute("parqueoActual", parqueo.getIdParqueo() == null ? null : parqueo);
        model.addAttribute("editar", parqueo.getIdParqueo());
        return "parqueo/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Parqueo parqueo, BindingResult bindingResult,
            @RequestParam(required = false) Integer idTipoParqueo,
            Model model, Locale locale, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                parqueoService.save(parqueo, idTipoParqueo);
                redirectAttributes.addFlashAttribute("todoOk", mensaje("mensaje.guardado", locale));
                return "redirect:/parqueo/listado";
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", mensaje(exception.getMessage(), locale));
            } catch (IllegalStateException exception) {
                model.addAttribute("error", mensaje(exception.getMessage(), locale));
            } catch (DataIntegrityViolationException exception) {
                model.addAttribute("error", mensaje("error.parqueoDuplicado", locale));
            } catch (Exception exception) {
                model.addAttribute("error", mensaje("error.operacion", locale));
            }
        }
        cargar(model);
        parqueo.setTipoParqueo(parqueoService.getTipo(idTipoParqueo));
        if (parqueo.getIdParqueo() != null) {
            try {
                var actual = parqueoService.get(parqueo.getIdParqueo());
                model.addAttribute("parqueoActual", actual);
                parqueo.setBloqueado(actual.isBloqueado());
                parqueo.setFilialAsignada(actual.getFilialAsignada());
                parqueo.setEstado(actual.getEstado());
                if (actual.isBloqueado()) {
                    parqueo.setTipoParqueo(actual.getTipoParqueo());
                    parqueo.setActivo(actual.isActivo());
                }
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", mensaje(exception.getMessage(), locale));
            }
        }
        model.addAttribute("editar", parqueo.getIdParqueo());
        return "parqueo/listado";
    }

    @PostMapping("/asignar")
    public String asignar(@RequestParam Integer idParqueo, @RequestParam(required = false) Integer idFilial,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.parqueoAsignado";
        try {
            parqueoService.asignar(idParqueo, idFilial);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (IllegalStateException exception) {
            titulo = "error";
            clave = exception.getMessage();
        } catch (DataIntegrityViolationException exception) {
            titulo = "error";
            clave = "error.parqueoAsignar";
        } catch (Exception exception) {
            titulo = "error";
            clave = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
        if ("error".equals(titulo)) {
            redirectAttributes.addFlashAttribute("idParqueoAsignacion", idParqueo);
            redirectAttributes.addFlashAttribute("idFilialAsignacion", idFilial);
        }
        return "redirect:/parqueo/listado";
    }

    @PostMapping("/liberar")
    public String liberar(@RequestParam Integer idParqueo, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.parqueoLiberado";
        try {
            parqueoService.liberar(idParqueo);
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
        return "redirect:/parqueo/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idParqueo, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.registroEliminado";
        try {
            parqueoService.delete(idParqueo);
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
        return "redirect:/parqueo/listado";
    }
}
