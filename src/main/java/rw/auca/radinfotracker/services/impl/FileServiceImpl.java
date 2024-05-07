package rw.auca.radinfotracker.services.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.InternalServerErrorException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.enums.EFileSizeType;
import rw.auca.radinfotracker.model.enums.EFileStatus;
import rw.auca.radinfotracker.repository.FileRepository;
import rw.auca.radinfotracker.services.FileService;
import rw.auca.radinfotracker.utils.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileRepository fileRepository;

    private final FileStorageService fileStorageService;

    @Getter
    @Value("${openapi.service.url}")
    private String baseUrl;

    @Override
    public File findById(UUID id) throws ResourceNotFoundException {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty())
            throw new ResourceNotFoundException("exceptions.notFound.file");

        return fileOptional.get();
    }

    @Override
    public File findByIdOrElseNull(UUID id) {
        if (id == null) return null;
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public File findByName(String name) throws ResourceNotFoundException {
        Optional<File> fileOptional = fileRepository.findByName(name);
        if (fileOptional.isEmpty())
            throw new ResourceNotFoundException("exceptions.notFound.file");
        return fileOptional.get();
    }

    @Override
    public File downloadById(UUID id) throws ResourceNotFoundException {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty())
            throw new ResourceNotFoundException("exceptions.notFound.file");
        return fileOptional.get();
    }

    @Override
    public void deleteById(UUID id) throws ResourceNotFoundException, InternalServerErrorException, IOException {
        File file = this.findById(id);
        delete(file.getPath());
        fileRepository.deleteById(id);
    }

    @Override
    public Resource load(String filePath) throws IOException {
        Path path = Path.of(filePath);
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    public void delete(String filePath) throws ResourceNotFoundException, IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path))
            throw new ResourceNotFoundException("exceptions.notFound.file");
        else Files.delete(path);
    }

    @Override
    public File create(MultipartFile document) throws Exception {

        String fileExtension = StringUtils.getFilenameExtension(Objects.requireNonNull(document.getOriginalFilename()));

        if (fileExtension == null || !Arrays.asList("png", "jpeg", "jpg", "pdf").contains(fileExtension.toLowerCase())) {
            throw new BadRequestException("exceptions.file.notSupported");
        }

        File file = new File();

        String fileName = FileUtil.generateUUID(Objects.requireNonNull(document.getOriginalFilename()));
        String documentSizeType = FileUtil.getFileSizeTypeFromFileSize(document.getSize());
        int documentSize = FileUtil.getFormattedFileSizeFromFileSize(document.getSize(), EFileSizeType.valueOf(documentSizeType));

        if(documentSize > 3 * 1024 * 1024){
            throw new BadRequestException("exceptions.file.tooLarge");
        }else {
            file.setName(fileName);
            file.setPath(fileStorageService.save(document, fileName));
            file.setUrl(baseUrl+"/api/v1/files/");
            file.setStatus(EFileStatus.SAVED);
            file.setType(document.getContentType());
            file.setSize(documentSize);
            file.setSizeType(EFileSizeType.valueOf(documentSizeType));
        }
        return this.fileRepository.save(file);
    }
}
