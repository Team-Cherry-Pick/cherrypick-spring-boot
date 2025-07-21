package com.cherrypick.backend.domain.deal.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component @RequiredArgsConstructor
public class OpenAiAdapter {

    private final ChatClient chatClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public <T> Optional<T> requestClassify(String prompt, Class<T> type) {

        

        var option = OpenAiChatOptions.builder()
                .withTemperature(0.0F) // 낮을수록 보수적
                .withMaxTokens(500)
                .withModel("gpt-3.5-turbo")
                .build();


        return Optional.ofNullable(chatClient.prompt()
                .options(option)
                .user(prompt)
                .call()
                .entity(type));
    }

    public <T> Optional<T> requestContent(String prompt, Class<T> type) {

        var option = OpenAiChatOptions.builder()
                .withTemperature(1.5F) // 낮을수록 보수적
                .withMaxTokens(1000)
                .withModel("gpt-4o")
                .build();

        return Optional.ofNullable(chatClient.prompt()
                .options(option)
                .user(prompt)
                .call()
                .entity(type));
    }




}
