package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "area_comun")
public class AreaComun implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_area_comun")
    private Integer idAreaComun;

    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 100, message = "{validacion.longitud}")
    @Column(name = "nombre")
    private String nombre;

    @NotBlank(message = "{validacion.requerido}")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @NotNull(message = "{validacion.requerido}")
    @Min(value = 1, message = "{validacion.capacidad}")
    @Column(name = "capacidad")
    private Integer capacidad;

    @NotNull(message = "{validacion.requerido}")
    @Column(name = "hora_apertura")
    private LocalTime horaApertura;

    @NotNull(message = "{validacion.requerido}")
    @Column(name = "hora_cierre")
    private LocalTime horaCierre;

    @Column(name = "activo")
    private boolean activo = true;
}
