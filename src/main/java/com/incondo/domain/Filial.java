package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "filial")
public class Filial implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_filial")
    private Integer idFilial;
    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 30, message = "{validacion.longitud}")
    @Column(name = "numero")
    private String numero;
    @ManyToOne
    @JoinColumn(name = "id_torre", nullable = false)
    private Torre torre;
    @ManyToOne
    @JoinColumn(name = "id_tipo_filial", nullable = false)
    private TipoFilial tipoFilial;

    @Column(name = "activo")
    private boolean activo = true;
    @Transient
    private String ocupacion;
}
