package com.edu.fileupload.controller;

import com.edu.fileupload.dto.FileUploadResponse;
import com.edu.fileupload.exception.FileUploadException;
import com.edu.fileupload.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Tag(name = "File Uploader", description = "API for uploading and managing files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "Upload files", description = "Upload multiple files with parallel processing and antivirus scanning.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Files uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FileUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public CompletableFuture<ResponseEntity<FileUploadResponse>> uploadFiles(
            @Parameter(description = "Files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            log.warn("No files provided in the request");
            return CompletableFuture.completedFuture(
                    ResponseEntity.badRequest().body(new FileUploadResponse(false, "No files provided", null))
            );
        }

        // Проверяем расширение файлов
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.endsWith(".txt")) {
                log.warn("Invalid file format: {}", filename);
                return CompletableFuture.completedFuture(
                        ResponseEntity.badRequest().body(new FileUploadResponse(false, "Only .txt files are allowed", null))
                );
            }
        }

        return fileUploadService.uploadFiles(files)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("Failed to upload files", ex);
                    return ResponseEntity.badRequest().body(
                            new FileUploadResponse(false, ex.getMessage(), null)
                    );
                });
    }
}