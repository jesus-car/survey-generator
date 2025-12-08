package com.surveygenerator.surveygenerator.document.application.factory;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SurveyGeneratorFactory {

    private final Map<String, PromptStrategy> strategies;

    public SurveyGeneratorFactory(List<PromptStrategy> strategyInstances) {
        this.strategies = strategyInstances.stream()
                .collect(Collectors.toMap(PromptStrategy::getStep, Function.identity()));
    }

    public PromptStrategy getStrategy(String step) {
        return Optional.ofNullable(strategies.get(step.toUpperCase()))
                .orElseThrow(() -> new RuntimeException("Not found strategy for step: " + step));
    }
}


