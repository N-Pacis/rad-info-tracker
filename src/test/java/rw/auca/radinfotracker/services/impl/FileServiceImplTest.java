package rw.auca.radinfotracker.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.enums.EFileSizeType;
import rw.auca.radinfotracker.model.enums.EFileStatus;
import rw.auca.radinfotracker.repository.FileRepository;
import rw.auca.radinfotracker.utils.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageService fileStorageService;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        String root = "/path/to/upload/directory";
        String baseUrl = "http://localhost:8080";

        fileService = new FileServiceImpl(fileRepository, fileStorageService);

        ReflectionTestUtils.setField(fileStorageService, "root", root);
        ReflectionTestUtils.setField(fileService, "baseUrl", baseUrl);
    }

    @Test
    void findById_shouldReturnFileWhenIdExists() throws ResourceNotFoundException {
        UUID id = UUID.randomUUID();
        File file = new File();
        when(fileRepository.findById(id)).thenReturn(Optional.of(file));

        File result = fileService.findById(id);

        assertThat(result).isEqualTo(file);
    }

    @Test
    void findById_shouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(fileRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileService.findById(id));
    }

    @Test
    void create_shouldReturnFileWhenFileIsSaved() throws Exception {
        // Arrange
        String originalFilename = "file.jpg";
        String contentType = "image/jpeg";
        byte[] fileContent = "test data".getBytes();
        long fileSize = fileContent.length;

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getContentType()).thenReturn(contentType);
        when(multipartFile.getSize()).thenReturn(fileSize);

        String documentSizeType = FileUtil.getFileSizeTypeFromFileSize(multipartFile.getSize());
        int documentSize = FileUtil.getFormattedFileSizeFromFileSize(multipartFile.getSize(), EFileSizeType.valueOf(documentSizeType));

        when(fileStorageService.save(any(MultipartFile.class), any(String.class))).thenReturn("path/to/file");

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);

        fileService.create(multipartFile);

        verify(fileRepository).save(fileCaptor.capture());
        File capturedFile = fileCaptor.getValue();

        assertThat(capturedFile.getType()).isEqualTo(contentType);
        assertThat(capturedFile.getSize()).isEqualTo(documentSize);
        assertThat(capturedFile.getSizeType()).isEqualTo(EFileSizeType.valueOf(documentSizeType));
    }

    @Test
    void create_shouldThrowBadRequestExceptionWhenFileExtensionIsNotSupported() {
        MultipartFile multipartFile = new MockMultipartFile("file.txt", "file.txt", "text/plain", "test data".getBytes());

        assertThrows(Exception.class, () -> fileService.create(multipartFile));
    }

    @Test
    void deleteById_shouldDeleteFileWhenIdExists() throws ResourceNotFoundException, IOException {
        UUID id = UUID.randomUUID();
        File file = new File();
        file.setPath("path/to/file");
        when(fileRepository.findById(id)).thenReturn(Optional.of(file));
        Path filePath = Paths.get(file.getPath());
        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);

        fileService.deleteById(id);

        verify(fileRepository, times(1)).deleteById(id);
        assertThat(Files.exists(filePath)).isFalse();
    }
}