package com.surveygenerator.surveygenerator.document.infrastructure.input.controller;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.service.DocumentService;
import com.surveygenerator.surveygenerator.user.infrastructure.config.CustomUserDetails;
import com.surveygenerator.surveygenerator.utils.FileValidator;
import com.surveygenerator.surveygenerator.utils.ValidatorResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final FileValidator fileValidator;
    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<List<QuizResponse>> uploadDocument(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ValidatorResult<List<MultipartFile>> validationResult = fileValidator.validateFiles(files);

        if (!validationResult.isValid()) {
            return ResponseEntity.badRequest().build();
        }

        // Obtener userId del usuario autenticado
        String userId = userDetails.getUserId();

        return ResponseEntity.ok(
                documentService.generateQuestions(validationResult.getData(), userId));
    }
}
