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

    @Column(name = "fecha_inicio")
    private java.time.LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private java.time.LocalDate fechaFin;

    @Column(name = "fecha_salida")
    private java.time.LocalDate fechaSalida;

    @Column(name = "monto_mensual")
    private java.math.BigDecimal montoMensual;
}
