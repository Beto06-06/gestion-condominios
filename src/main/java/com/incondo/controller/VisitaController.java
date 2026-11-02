package com.incondo.controller;

import com.incondo.domain.Persona;
import com.incondo.service.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
@RequestMapping("/visita")
public class VisitaController {

    private final VisitaService visitaService;
    private final FilialService filialService;
    private final EmpleadoService empleadoService;
    private final ParqueoService parqueoService;
    private final MessageSource messageSource;

    private String mensaje(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, messageSource.getMessage("error.operacion", null, locale), locale);
    }

    private void cargar(Model model, String cedula, boolean llegaEnVehiculo, String placa,
            Integer idFilial, Integer idEmpleadoPuesto, Integer idParqueo) {
        model.addAttribute("filiales", filialService.listar());
        model.addAttribute("guardas", empleadoService.getGuardas());
        model.addAttribute("parqueosDisponibles", parqueoService.listarDisponiblesVisita());
        model.addAttribute("personaExistente", visitaService.buscarPersona(cedula).isPresent());
        model.addAttribute("visitanteDentro", visitaService.estaDentro(cedula));
        model.addAttribute("llegaEnVehiculo", llegaEnVehiculo);
        model.addAttribute("placa", placa);
        model.addAttribute("idFilialSeleccionada", idFilial);
        model.addAttribute("idGuardaSeleccionado", idEmpleadoPuesto);
        model.addAttribute("idParqueoSeleccionado", idParqueo);
        model.addAttribute("ahora", LocalDateTime.now());
        model.addAttribute("rutaIdioma", "/visita/registro");
        model.addAttribute("cedulaBusqueda", cedula);
    }

    @GetMapping("/registro")
    public String registro(@RequestParam(defaultValue = "") String cedula, Model model) {
        var persona = visitaService.buscarPersona(cedula).orElse(new Persona());
        if (persona.getIdPersona() == null) {
            persona.setCedula(PersonaService.cedulaNormalizada(cedula));
        }
        model.addAttribute("persona", persona);
        cargar(model, cedula, false, "", null, null, null);
        return "visita/registro";
    }

    @GetMapping("/dentro")
    public String dentro(Model model) {
        var visitas = visitaService.listarDentro();
        model.addAttribute("visitas", visitas);
        model.addAttribute("totalDentro", visitas.size());
        model.addAttribute("parqueosLibres", parqueoService.listarDisponiblesVisita().size());
        model.addAttribute("ingresosHoy", visitaService.contarIngresosHoy());
        model.addAttribute("rutaIdioma", "/visita/dentro");
        return "visita/dentro";
    }

    @PostMapping("/salida")
    public String salida(@RequestParam Integer idVisita, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.visitaSalida";
        try {
            visitaService.registrarSalida(idVisita);
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
        return "redirect:/visita/dentro";
    }

    @GetMapping("/historial")
    public String historial(@RequestParam(defaultValue = "") String desde,
            @RequestParam(defaultValue = "") String hasta,
            @RequestParam(required = false) Integer idFilial,
            @RequestParam(defaultValue = "") String cedula, Model model, Locale locale) {
        model.addAttribute("visitas", new ArrayList<>());
        model.addAttribute("filiales", filialService.listar());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("idFilialFiltro", idFilial);
        model.addAttribute("cedulaBusqueda", cedula);
        model.addAttribute("rutaIdioma", "/visita/historial");
        String clave = null;
        try {
            var fechaDesde = desde.isBlank() ? null : LocalDate.parse(desde);
            var fechaHasta = hasta.isBlank() ? null : LocalDate.parse(hasta);
            model.addAttribute("visitas", visitaService.historial(fechaDesde, fechaHasta, idFilial, cedula));
        } catch (DateTimeParseException exception) {
            clave = "error.historialFechaInvalida";
        } catch (IllegalArgumentException exception) {
            clave = exception.getMessage();
        } catch (IllegalStateException exception) {
            clave = exception.getMessage();
        } catch (Exception exception) {
            clave = "error.operacion";
        }
        if (clave != null) {
            model.addAttribute("error", mensaje(clave, locale));
        }
        return "visita/historial";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Persona persona, BindingResult bindingResult,
            @RequestParam(required = false) Integer idFilial,
            @RequestParam(required = false) Integer idEmpleadoPuesto,
            @RequestParam(defaultValue = "false") boolean llegaEnVehiculo,
            @RequestParam(defaultValue = "") String placa,
            @RequestParam(required = false) Integer idParqueo,
            Model model, Locale locale, RedirectAttributes redirectAttributes) {
        String titulo = "todoOk";
        String clave = "mensaje.visitaRegistrada";
        if (!bindingResult.hasErrors()) {
            try {
                var visita = visitaService.save(persona, idFilial, idEmpleadoPuesto, llegaEnVehiculo, placa, idParqueo);
                redirectAttributes.addFlashAttribute("visitaRegistrada", visita);
            } catch (IllegalArgumentException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (IllegalStateException exception) {
                titulo = "error";
                clave = exception.getMessage();
            } catch (DataIntegrityViolationException exception) {
                titulo = "error";
                clave = "error.visitaDuplicada";
            } catch (Exception exception) {
                titulo = "error";
                clave = "error.operacion";
            }
            if ("todoOk".equals(titulo)) {
                redirectAttributes.addFlashAttribute(titulo, mensaje(clave, locale));
                return "redirect:/visita/registro";
            }
            model.addAttribute(titulo, mensaje(clave, locale));
        }
        cargar(model, persona.getCedula(), llegaEnVehiculo, placa, idFilial, idEmpleadoPuesto, idParqueo);
        return "visita/registro";
    }
}
