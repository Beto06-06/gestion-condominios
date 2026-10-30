package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "empleado_puesto")
public class EmpleadoPuesto implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado_puesto")
    private Integer idEmpleadoPuesto;
    @ManyToOne
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;
    @ManyToOne
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;
    @ManyToOne
    @JoinColumn(name = "id_turno", nullable = false)
    private Turno turno;
}
