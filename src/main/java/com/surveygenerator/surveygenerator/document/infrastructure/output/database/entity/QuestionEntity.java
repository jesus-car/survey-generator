package com.surveygenerator.surveygenerator.document.infrastructure.output.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionEntity {
    private String question;
    private List<String> options;
    private String answer;
}
