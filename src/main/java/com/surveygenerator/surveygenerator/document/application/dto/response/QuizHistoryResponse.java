package com.surveygenerator.surveygenerator.document.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record QuizHistoryResponse(
        @JsonProperty("id") String id,
        @JsonProperty("statement") String statement,
        @JsonProperty("createdAt") LocalDateTime createdAt
) {}
