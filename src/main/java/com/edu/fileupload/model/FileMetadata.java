package com.edu.fileupload.model;

import lombok.Data;

@Data
public class FileMetadata {
    private String filename;       // Имя файла
    private long size;             // Размер файла в байтах
    private String contentType;    // Тип содержимого файла
    private boolean isVirusFree;   // Результат проверки антивирусом
}