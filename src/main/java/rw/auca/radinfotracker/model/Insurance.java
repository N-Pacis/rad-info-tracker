package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.dtos.NewInsuranceDTO;
import rw.auca.radinfotracker.model.enums.EInsuranceStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Insurance extends TimestampAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "rate", nullable = false)
    private Double rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private EInsuranceStatus status = EInsuranceStatus.ACTIVE;

    public Insurance(NewInsuranceDTO dto){
        this.name = dto.getName();
        this.rate = dto.getRate();
    }

    public Insurance(String name, Double rate, EInsuranceStatus status){
        this.name = name;
        this.rate = rate;
        this.status = status;
    }
}
