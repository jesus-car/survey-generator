package com.surveygenerator.surveygenerator.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class FileValidator {

    private static final String[] ALLOWED_EXTENSIONS = {".md"};

    public ValidatorResult<List<MultipartFile>> validateFiles(List<MultipartFile> files) {
        log.debug("Validating multipart file upload");

        if (files == null || files.isEmpty()) {
            log.warn("No files found in request");
            return ValidatorResult.error("No files found in request");
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("Empty file found in request");
                return ValidatorResult.error("Empty file found in request");
            }

            ValidatorResult<Void> extensionValidator = validateFileExtension(file.getOriginalFilename());
            if (!extensionValidator.isValid()) {
                return ValidatorResult.error(extensionValidator.getErrorMessage());
            }
        }

        log.debug("File validation successful for {} file(s)", files.size());
        return ValidatorResult.success(files);
    }

    public ValidatorResult<MultipartFile> validateSingleFile(MultipartFile file) {
        log.debug("Validating single multipart file upload");

        if (file == null || file.isEmpty()) {
            log.warn("No file found in request or file is empty");
            return ValidatorResult.error("No file found in request or file is empty");
        }

        ValidatorResult<Void> extensionValidator = validateFileExtension(file.getOriginalFilename());
        if (!extensionValidator.isValid()) {
            return ValidatorResult.error(extensionValidator.getErrorMessage());
        }

        log.debug("File validation successful for: {}", file.getOriginalFilename());
        return ValidatorResult.success(file);
    }

    private ValidatorResult<Void> validateFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            log.warn("Filename is null or empty");
            return ValidatorResult.error("Filename cannot be empty");
        }

        String lowercaseFilename = filename.toLowerCase();
        boolean isValidExtension = Arrays.stream(ALLOWED_EXTENSIONS)
                .anyMatch(lowercaseFilename::endsWith);

        if (!isValidExtension) {
            log.warn("Invalid file extension for file: {}", filename);
            String allowedExtensionsStr = String.join(", ", ALLOWED_EXTENSIONS);
            return ValidatorResult.error("Only " + allowedExtensionsStr + " files are allowed");
        }

        return ValidatorResult.success(null);
    }
}