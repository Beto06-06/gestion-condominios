package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alquiler")
public class Alquiler implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alquiler")
    private Integer idAlquiler;
    @ManyToOne
    @JoinColumn(name = "id_filial", nullable = false)
    private Filial filial;

    @NotNull(message = "{validacion.requerido}")
    @Column(name = "fecha_inicio")
    private java.time.LocalDate fechaInicio;

    @NotNull(message = "{validacion.requerido}")
    @Column(name = "fecha_fin")
    private java.time.LocalDate fechaFin;

    @Column(name = "fecha_salida")
    private java.time.LocalDate fechaSalida;

    @NotNull(message = "{validacion.requerido}")
    @DecimalMin(value = "0.01", message = "{validacion.monto}")
    @Column(name = "monto_mensual")
    private java.math.BigDecimal montoMensual;

    @Transient
    private java.util.List<Persona> inquilinos = new java.util.ArrayList<>();
    @Transient
    private String estado;
    @Transient
    private boolean editable;
    @Transient
    private boolean eliminable;
    @Transient
    private boolean finalizable;

}
