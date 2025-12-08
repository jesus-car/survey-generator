package com.surveygenerator.surveygenerator.document.application.service;

import com.surveygenerator.surveygenerator.utils.ValidatorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileContentProcessor {

    public String extractContent(MultipartFile file) {
        log.debug("Extracting content from file: {}", file.getOriginalFilename());

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            log.debug("Content extracted successfully, length: {}", content.length());

            return content;

        } catch (IOException error) {
            log.error("Error extracting file content", error);
            throw new RuntimeException("Error extracting file content", error);
        }
    }

    public ValidatorResult<String> validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("File content is empty");
            return ValidatorResult.error("Uploaded file is empty");
        }

        log.debug("Content validation successful");
        return ValidatorResult.success(content);
    }
}
