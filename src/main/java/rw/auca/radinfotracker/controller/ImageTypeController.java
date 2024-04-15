package rw.auca.radinfotracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rw.auca.radinfotracker.exceptions.BadRequestException;
import rw.auca.radinfotracker.exceptions.ResourceNotFoundException;
import rw.auca.radinfotracker.model.ImageType;
import rw.auca.radinfotracker.model.dtos.NewImageTypeDTO;
import rw.auca.radinfotracker.model.enums.EImageTypeStatus;
import rw.auca.radinfotracker.services.IImageTypeService;
import rw.auca.radinfotracker.utils.ApiResponse;
import rw.auca.radinfotracker.utils.Constants;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/imageTypes")
public class ImageTypeController extends BaseController{
    private final IImageTypeService imageTypeService;

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ImageType>>> searchAll(
            @RequestParam(value = "page", defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "status", required = false) EImageTypeStatus status,
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "limit", defaultValue = Constants.DEFAULT_PAGE_SIZE) int limit) throws BadRequestException, ResourceNotFoundException {

        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<ImageType> imageTypes = this.imageTypeService.getAll(query, status, pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(imageTypes, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ImageType>>> getAllActiveImageTypes(
            @RequestParam(value = "query", required = false, defaultValue = "") String query) {

        List<ImageType> imageTypes = this.imageTypeService.getAll(query);
        return ResponseEntity.ok(
                new ApiResponse<>(imageTypes, localize("responses.getListSuccess"), HttpStatus.OK)
        );
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ImageType>> register(@Valid @RequestBody NewImageTypeDTO dto) throws BadRequestException {
        ImageType imageType = this.imageTypeService.register(dto);
        return ResponseEntity.ok(new ApiResponse<>(imageType,localize("responses.saveEntitySuccess"), HttpStatus.CREATED));
    }

    @Override
    protected String getEntityName() {
        return "Image Type";
    }
}
