package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parqueo")
public class Parqueo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parqueo")
    private Integer idParqueo;
    @NotBlank(message = "{validacion.requerido}")
    @Size(max = 30, message = "{validacion.longitud}")
    private String codigo;
    @ManyToOne
    @JoinColumn(name = "id_tipo_parqueo", nullable = false)
    private TipoParqueo tipoParqueo;
    private boolean activo = true;
    @Transient
    private String estado;
    @Transient
    private boolean bloqueado;
    @Transient
    private Filial filialAsignada;
}
