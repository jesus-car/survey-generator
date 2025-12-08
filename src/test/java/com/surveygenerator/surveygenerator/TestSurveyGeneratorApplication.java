package com.surveygenerator.surveygenerator;

import org.springframework.boot.SpringApplication;

public class TestSurveyGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.from(SurveyGeneratorApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
