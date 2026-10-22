package com.example.langgraphhelloworld;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.ai.openai.api-key=test-api-key")
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatClient chatClient;

    @TestConfiguration(proxyBeanMethods = false)
    static class MockChatClientConfiguration {

        @Bean
        @Primary
        ChatClient mockChatClient() {
            return mock(ChatClient.class, Answers.RETURNS_DEEP_STUBS);
        }
    }

    @Test
    void chatEndpointRunsGraphAndReturnsModelResponse() throws Exception {
        when(chatClient.prompt().user(anyString()).call().content())
                .thenReturn("Hello from OpenAI!");

        mockMvc.perform(post("/chat")
                        .contentType("application/json")
                        .content("{\"message\":\"Say hello\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"response\":\"Hello from OpenAI!\"}"));
    }
}
