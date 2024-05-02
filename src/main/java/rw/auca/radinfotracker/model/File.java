package rw.auca.radinfotracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rw.auca.radinfotracker.audits.TimestampAudit;
import rw.auca.radinfotracker.model.enums.EFileSizeType;
import rw.auca.radinfotracker.model.enums.EFileStatus;

import java.util.UUID;

@Entity
@Table(name = "files")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class File extends TimestampAudit {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "url")
    private String url;

    @Column(name = "size")
    private int size;

    @Column(name = "size_type")
    @Enumerated(EnumType.STRING)
    private EFileSizeType sizeType;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EFileStatus status;

    public File(String name, String path, String url, int size, EFileSizeType sizeType, String type, EFileStatus status){
        this.name = name;
        this.path = path;
        this.url = url;
        this.size = size;
        this.sizeType = sizeType;
        this.type = type;
        this.status = status;
    }

}
