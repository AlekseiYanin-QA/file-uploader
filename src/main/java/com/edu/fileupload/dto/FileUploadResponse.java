package com.edu.fileupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private boolean success;       // Успешность операции
    private String message;        // Сообщение о результате
    private List<String> uploadedFiles; // Список загруженных файлов
}