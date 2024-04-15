package rw.auca.radinfotracker.model;

import jakarta.persistence.*;
import lombok.*;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;

import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table
@Builder
public class ImageType extends TimestampAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EImageTypeStatus status = EImageTypeStatus.ACTIVE;

    public ImageType(String name){
        this.name = name;
    }

}
