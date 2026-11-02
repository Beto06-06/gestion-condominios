package com.incondo.controller;

import com.incondo.domain.*;
import com.incondo.service.*;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/empleado")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final MessageSource messageSource;

    private void cargarDatosListado(Model model, Integer puesto) {
        model.addAttribute("empleados", empleadoService.listar(puesto));
        model.addAttribute("puestos", empleadoService.getPuestos());
        model.addAttribute("turnos", empleadoService.getTurnos());
        model.addAttribute("filtroPuesto", puesto);
        model.addAttribute("rutaIdioma", "/empleado/listado");
    }

    private String redirigirAlListado(Integer puesto) {
        if (puesto == null) {
            return "redirect:/empleado/listado";
        }
        return "redirect:/empleado/listado?puesto=" + puesto;
    }

    @GetMapping("/listado")
    public String listado(@RequestParam(required = false) Integer puesto, @RequestParam(required = false) Integer editar, Model model) {
        cargarDatosListado(model, puesto);
        model.addAttribute("editar", editar);
        var asignacion = editar == null ? new EmpleadoPuesto() : empleadoService.getAsignacion(editar);
        model.addAttribute("asignacionForm", asignacion);
        model.addAttribute("persona", editar == null ? new Persona() : asignacion.getEmpleado().getPersona());
        return "empleado/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Persona persona, BindingResult bindingResult,
            @RequestParam(required = false) Integer idEmpleadoPuesto,
            @RequestParam(required = false) Integer idPuesto,
            @RequestParam(required = false) Integer idTurno,
            @RequestParam(required = false) Integer filtroPuesto,
            Locale locale, Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                empleadoService.save(idEmpleadoPuesto, persona, idPuesto, idTurno);
                redirectAttributes.addFlashAttribute("todoOk", messageSource.getMessage("mensaje.guardado", null, locale));
                return redirigirAlListado(filtroPuesto);
            } catch (IllegalArgumentException exception) {
                model.addAttribute("error", messageSource.getMessage(exception.getMessage(), null, messageSource.getMessage("error.operacion", null, locale), locale));
            } catch (DataIntegrityViolationException exception) {
                model.addAttribute("error", messageSource.getMessage("error.duplicado", null, locale));
            } catch (Exception exception) {
                log.error("No se pudo guardar el empleado", exception);
                model.addAttribute("error", messageSource.getMessage("error.operacion", null, locale));
            }
        }
        cargarDatosListado(model, filtroPuesto);
        model.addAttribute("editar", idEmpleadoPuesto);
        var asignacion = new EmpleadoPuesto();
        asignacion.setIdEmpleadoPuesto(idEmpleadoPuesto);
        if (idPuesto != null) {
            asignacion.setPuesto(empleadoService.getPuesto(idPuesto).orElse(null));
        }
        if (idTurno != null) {
            asignacion.setTurno(empleadoService.getTurno(idTurno).orElse(null));
        }
        model.addAttribute("asignacionForm", asignacion);
        return "empleado/listado";
    }

    @PostMapping("/estado")
    public String estado(@RequestParam Integer idEmpleado,
            @RequestParam(required = false) Integer filtroPuesto,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String mensaje = "mensaje.guardado";
        try {
            empleadoService.cambiarEstado(idEmpleado);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            mensaje = "error.noExiste";
        } catch (IllegalStateException exception) {
            titulo = "error";
            mensaje = "error.operacion";
        } catch (Exception exception) {
            titulo = "error";
            mensaje = "error.operacion";
            log.error("No se pudo cambiar el estado del empleado", exception);
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return redirigirAlListado(filtroPuesto);
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idEmpleado,
            @RequestParam(required = false) Integer filtroPuesto,
            Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String mensaje = "mensaje.registroEliminado";
        try {
            empleadoService.delete(idEmpleado);
        } catch (IllegalArgumentException exception) {
            titulo = "error";
            mensaje = "error.noExiste";
        } catch (IllegalStateException exception) {
            titulo = "error";
            mensaje = "error.empleadoAsociado";
        } catch (Exception exception) {
            titulo = "error";
            mensaje = "error.operacion";
        }
        redirectAttributes.addFlashAttribute(titulo, messageSource.getMessage(mensaje, null, locale));
        return redirigirAlListado(filtroPuesto);
    }

}
