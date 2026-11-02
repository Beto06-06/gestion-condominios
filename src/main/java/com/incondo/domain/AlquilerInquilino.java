package com.incondo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alquiler_inquilino")
public class AlquilerInquilino implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alquiler_inquilino")
    private Integer idAlquilerInquilino;
    @ManyToOne
    @JoinColumn(name = "id_alquiler", nullable = false)
    private Alquiler alquiler;
    @ManyToOne
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;
}
