package rw.auca.radinfotracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rw.auca.radinfotracker.exceptions.InternalServerErrorException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.File;
import rw.auca.radinfotracker.model.dtos.NewFileDTO;
import rw.auca.radinfotracker.services.impl.FileServiceImpl;
import rw.auca.radinfotracker.utils.ApiResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files")
@Validated
public class FileController extends BaseController{
    private final MessageSource messageSource;
    private final FileServiceImpl fileServiceImpl;
    private final Locale locale = LocaleContextHolder.getLocale();

    @Autowired
    public FileController(FileServiceImpl fileServiceImpl, MessageSource messageSource) {
        this.fileServiceImpl = fileServiceImpl;
        this.messageSource = messageSource;
    }

    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<File>> upload(@ModelAttribute NewFileDTO newFileDTO) throws Exception {
        File savedFile = fileServiceImpl.create(newFileDTO.getFile());
        return new ApiResponse<>(savedFile, messageSource.getMessage("success", null, locale), HttpStatus.CREATED).toResponseEntity();
    }

    @GetMapping("/raw/{name}")
    @ResponseBody
    public ResponseEntity<Resource> getFileResource(@PathVariable String name) throws IOException, ResourceNotFoundException {
        File file = fileServiceImpl.findByName(name);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline;filename=" + file.getName())
                .contentType(MediaType.valueOf(file.getType()))
                .body(fileServiceImpl.load(file.getPath()));
    }

    @GetMapping("/download/resource/{name}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFileResource(@PathVariable String name) throws IOException, ResourceNotFoundException {
        File file = fileServiceImpl.findByName(name);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename=" + file.getName())
                .contentType(MediaType.valueOf(file.getType()))
                .body(fileServiceImpl.load(file.getPath()));
    }

    @GetMapping("/download/byId/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFileId(@PathVariable UUID id) throws IOException, ResourceNotFoundException {
        File file = fileServiceImpl.downloadById(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename=" + file.getName())
                .contentType(MediaType.valueOf(file.getType()))
                .body(fileServiceImpl.load(file.getPath()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFileById(@PathVariable UUID id) throws ResourceNotFoundException, InternalServerErrorException, IOException {
        fileServiceImpl.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(localize("success"), HttpStatus.OK));
    }

    @GetMapping("/byId/{fileId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<File>> getById(@PathVariable @NotNull UUID fileId) throws ResourceNotFoundException {
        File file = fileServiceImpl.findById(fileId);
        return new ApiResponse<>(file, messageSource.getMessage("success", null, LocaleContextHolder.getLocale()), HttpStatus.OK).toResponseEntity();
    }

    @Override
    protected String getEntityName() {
        return "File";
    }
}
