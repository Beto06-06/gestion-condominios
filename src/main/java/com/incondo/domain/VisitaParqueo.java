package com.incondo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "visita_parqueo")
public class VisitaParqueo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita_parqueo")
    private Integer idVisitaParqueo;

    @ManyToOne
    @JoinColumn(name = "id_visita", nullable = false)
    private Visita visita;

    @ManyToOne
    @JoinColumn(name = "id_parqueo", nullable = false)
    private Parqueo parqueo;
}
