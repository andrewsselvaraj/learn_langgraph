package com.example.langgraphhelloworld;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentGraphTest {

    private final ChatModel chatModel = mock(ChatModel.class);

    @Test
    void agentCallsToolThenAnswers() throws Exception {
        var toolCall = new AssistantMessage.ToolCall("call-1", "function", "getWeather", "{\"city\":\"Chennai\"}");
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(reply(new AssistantMessage("", Map.of(), List.of(toolCall))))
                .thenReturn(reply(new AssistantMessage("It is 33°C and humid in Chennai.")));

        String answer = new AgentGraph(chatModel, new AgentTools()).ask("What's the weather in Chennai?");

        assertThat(answer).isEqualTo("It is 33°C and humid in Chennai.");

        // The second model call must see the tool result added by the "tools" node.
        var prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(2)).call(prompts.capture());
        List<Message> secondCall = prompts.getAllValues().get(1).getInstructions();
        var toolResult = (ToolResponseMessage) secondCall.get(secondCall.size() - 1);
        assertThat(toolResult.getResponses().get(0).responseData()).contains("33°C");
    }

    @Test
    void agentAnswersDirectlyWithoutTools() throws Exception {
        when(chatModel.call(any(Prompt.class))).thenReturn(reply(new AssistantMessage("Hello!")));

        assertThat(new AgentGraph(chatModel, new AgentTools()).ask("Hi")).isEqualTo("Hello!");
        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    void mermaidDiagramShowsTheLoop() throws Exception {
        String mermaid = new AgentGraph(chatModel, new AgentTools()).mermaid();
        System.out.println(mermaid);

        assertThat(mermaid).contains("agent").contains("tools");
    }

    private static ChatResponse reply(AssistantMessage message) {
        return new ChatResponse(List.of(new Generation(message)));
    }
}
