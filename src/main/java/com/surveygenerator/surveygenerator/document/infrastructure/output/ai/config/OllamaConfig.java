package com.surveygenerator.surveygenerator.document.infrastructure.output.ai.config;

//import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class OllamaConfig {

//    @Value("classpath:/static/prompt/ExpertSystemPrompt")
//    private Resource proSystemPrompt;
//
//    @Bean
//    ChatClient chatClient(ChatClient.Builder builder){
//        return builder.defaultSystem(proSystemPrompt)
//                .defaultOptions(ChatOptions.builder()
//                        .temperature(0.4)
//                        .topP(0.9)
//                        .build())
//                .build();
//    }
}
