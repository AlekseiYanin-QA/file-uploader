package com.edu.fileupload.service.impl;

import com.edu.fileupload.dto.FileUploadResponse;
import com.edu.fileupload.exception.FileUploadException;
import com.edu.fileupload.model.FileMetadata;
import com.edu.fileupload.service.AntivirusService;
import com.edu.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final AntivirusService antivirusService;

    @Value("${file.upload-dir:uploads}") // Директория для загрузки файлов (по умолчанию "uploads")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation); // Создаем директорию, если она не существует
                log.info("Created upload directory: {}", rootLocation);
            }
        } catch (IOException e) {
            log.error("Could not initialize storage directory: {}", rootLocation, e);
            throw new FileUploadException("Could not initialize storage directory", e);
        }
    }

    @Override
    @Async
    public CompletableFuture<FileUploadResponse> uploadFiles(List<MultipartFile> files) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (files == null || files.isEmpty()) {
                    throw new FileUploadException("No files provided");
                }

                // Используем Fork/Join для параллельной обработки файлов
                ForkJoinPool forkJoinPool = new ForkJoinPool();
                List<String> uploadedFiles = forkJoinPool.invoke(new FileProcessingTask(files));

                log.info("Successfully uploaded {} files", uploadedFiles.size());
                return new FileUploadResponse(true, "Files uploaded successfully", uploadedFiles);
            } catch (Exception e) {
                log.error("Failed to upload files", e);
                throw new FileUploadException("Failed to upload files: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Внутренний класс для обработки файлов с использованием Fork/Join.
     */
    private class FileProcessingTask extends RecursiveTask<List<String>> {
        private final List<MultipartFile> files;

        public FileProcessingTask(List<MultipartFile> files) {
            this.files = files;
        }

        @Override
        protected List<String> compute() {
            return files.parallelStream()
                    .map(file -> {
                        try {
                            FileMetadata metadata = processFile(file); // Обрабатываем каждый файл
                            return metadata.getFilename();
                        } catch (IOException e) {
                            log.error("Failed to process file: {}", file.getOriginalFilename(), e);
                            throw new FileUploadException("Failed to process file: " + file.getOriginalFilename(), e);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * Обрабатывает файл: сохраняет его и проверяет антивирусом.
     *
     * @param file Файл для обработки.
     * @return Метаданные обработанного файла.
     */
    private FileMetadata processFile(MultipartFile file) throws IOException {
        String originalFilename = FilenameUtils.getName(file.getOriginalFilename());
        String uniqueFilename = generateUniqueFilename(originalFilename); // Генерация уникального имени
        Path destinationFile = rootLocation.resolve(uniqueFilename);

        try {
            // Сохраняем файл с перезаписью, если он уже существует
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("File saved: {}", destinationFile);

            // Создаем метаданные файла
            FileMetadata metadata = new FileMetadata();
            metadata.setFilename(uniqueFilename);
            metadata.setSize(file.getSize());
            metadata.setContentType(file.getContentType());
            metadata.setVirusFree(antivirusService.scanFile(metadata)); // Проверяем антивирусом

            log.debug("File processed: {}", metadata);
            return metadata;
        } catch (IOException e) {
            log.error("Failed to save file: {}", uniqueFilename, e);
            throw new IOException("Failed to save file: " + uniqueFilename, e);
        }
    }

    /**
     * Генерирует уникальное имя файла.
     *
     * @param originalFilename Оригинальное имя файла.
     * @return Уникальное имя файла.
     */
    private String generateUniqueFilename(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString();
        return timestamp + "_" + uuid + "_" + originalFilename;
    }
}