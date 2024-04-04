package rw.auca.radinfotracker.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import rw.auca.radinfotracker.exceptions.InternalServerErrorException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.File;

import java.io.IOException;
import java.util.UUID;

public interface FileService {

    File findById(UUID id) throws ResourceNotFoundException;

    File findByIdOrElseNull(UUID id);

    File findByName(String name) throws ResourceNotFoundException;

    File downloadById(UUID id) throws ResourceNotFoundException;


    void deleteById(UUID id) throws ResourceNotFoundException, InternalServerErrorException, IOException;

    String save(MultipartFile file, String filename) throws Exception;

    Resource load(String path) throws IOException, ResourceNotFoundException;

    File create(MultipartFile document) throws Exception;

}
