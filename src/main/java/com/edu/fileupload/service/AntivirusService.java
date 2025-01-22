package com.edu.fileupload.service;

import com.edu.fileupload.model.FileMetadata;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class AntivirusService {

    /**
     * Имитирует проверку файла антивирусом.
     *
     * @param fileMetadata Метаданные файла для проверки.
     * @return true, если файл безопасен, false — если обнаружен вирус.
     */
    public boolean scanFile(FileMetadata fileMetadata) {
        // Имитация проверки антивирусом
        try {
            Thread.sleep(1000); // Имитация задержки
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return ThreadLocalRandom.current().nextBoolean(); // Случайный результат
    }
}