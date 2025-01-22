package com.edu.fileupload.service;

import com.edu.fileupload.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс сервиса для загрузки файлов.
 */
public interface FileUploadService {
    /**
     * Загружает файлы с параллельной обработкой и проверкой антивирусом.
     *
     * @param files Список файлов для загрузки.
     * @return CompletableFuture с результатом загрузки.
     */
    CompletableFuture<FileUploadResponse> uploadFiles(List<MultipartFile> files);
}