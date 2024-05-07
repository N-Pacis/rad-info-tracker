package rw.auca.radinfotracker.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    @Getter
    @Value("${upload.directory}")
    private String root;

    public String save(MultipartFile file, String filename) throws Exception {
        try {
            Path of = Path.of(root);
            Files.copy(file.getInputStream(), of.resolve(Objects.requireNonNull(filename)));
            return of + "/" + filename;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
