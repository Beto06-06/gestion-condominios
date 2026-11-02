package com.incondo.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "parqueo_filial")
public class ParqueoFilial implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parqueo_filial")
    private Integer idParqueoFilial;
    @ManyToOne
    @JoinColumn(name = "id_parqueo", nullable = false)
    private Parqueo parqueo;
    @ManyToOne
    @JoinColumn(name = "id_filial", nullable = false)
    private Filial filial;
}
