package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "persona")
public class Persona implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Integer idPersona;
    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 30, message = "{validacion.longitud}")
    @Column(name = "cedula")
    private String cedula;
    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 150, message = "{validacion.longitud}")
    @Column(name = "nombre_completo")
    private String nombreCompleto;
    @Size(max = 30, message = "{validacion.longitud}")
    @Column(name = "telefono")
    private String telefono;
    @Email(message = "{validacion.correo}")
    @Size(max = 150, message = "{validacion.longitud}")
    @Column(name = "correo")
    private String correo;
}
