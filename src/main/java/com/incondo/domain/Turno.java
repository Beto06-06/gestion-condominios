package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "turno")
public class Turno implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Integer idTurno;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "hora_inicio")
    private java.time.LocalTime horaInicio;

    @Column(name = "hora_fin")
    private java.time.LocalTime horaFin;
}
