package com.incondo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "visita")
public class Visita implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita")
    private Integer idVisita;

    @ManyToOne
    @JoinColumn(name = "id_visitante", nullable = false)
    private Visitante visitante;

    @ManyToOne
    @JoinColumn(name = "id_filial", nullable = false)
    private Filial filial;

    @ManyToOne
    @JoinColumn(name = "id_empleado_puesto", nullable = false)
    private EmpleadoPuesto empleadoPuesto;

    @ManyToOne
    @JoinColumn(name = "id_vehiculo")
    private Vehiculo vehiculo;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Transient
    private Parqueo parqueoAsignado;
}
