package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(nullable = false)
    private Double totalCost;

    public ImageType(NewImageTypeDTO dto){
        this.name = dto.getName();
        this.totalCost = dto.getTotalCost();
    }

    public ImageType(String name, EImageTypeStatus status, Double totalCost){
        this.name = name;
        this.status = status;
        this.totalCost = totalCost;
    }

}
