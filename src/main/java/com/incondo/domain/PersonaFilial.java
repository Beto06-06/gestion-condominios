package com.incondo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "persona_filial")
public class PersonaFilial implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona_filial")
    private Integer idPersonaFilial;
    @ManyToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;
    @ManyToOne
    @JoinColumn(name = "id_filial", nullable = false)
    private Filial filial;
    @ManyToOne
    @JoinColumn(name = "id_tipo_relacion", nullable = false)
    private TipoRelacion tipoRelacion;

    @Column(name = "vive_en_filial")
    private boolean viveEnFilial;
}
