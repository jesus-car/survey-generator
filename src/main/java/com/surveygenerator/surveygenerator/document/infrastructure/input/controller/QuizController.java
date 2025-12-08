package com.surveygenerator.surveygenerator.document.infrastructure.input.controller;

import com.surveygenerator.surveygenerator.document.application.dto.response.QuizHistoryResponse;
import com.surveygenerator.surveygenerator.document.application.dto.response.QuizResponse;
import com.surveygenerator.surveygenerator.document.application.service.QuizService;
import com.surveygenerator.surveygenerator.user.infrastructure.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // Obtener historial de quizzes del usuario (solo id, statement, fecha)
    @GetMapping("/history")
    public ResponseEntity<List<QuizHistoryResponse>> getUserQuizHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUserId();
        return ResponseEntity.ok(quizService.getUserQuizHistory(userId));
    }

    // Obtener quiz completo por ID
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(
            @PathVariable String quizId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userId = userDetails.getUserId();
        return ResponseEntity.ok(quizService.getQuizById(quizId, userId));
    }
}
