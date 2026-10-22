package com.example.langgraphhelloworld;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Service
public class HelloWorldGraph {

    private final CompiledGraph<AgentState> graph;

    public HelloWorldGraph(ChatClient chatClient) throws GraphStateException {
        graph = new StateGraph<>(AgentState::new)
                .addNode("chat", node_async(state -> {
                    String message = state.<String>value("message")
                            .orElseThrow(() -> new IllegalArgumentException("Message is required"));
                    String response = chatClient.prompt()
                            .user(message)
                            .call()
                            .content();
                    return Map.of("response", response);
                }))
                .addEdge(START, "chat")
                .addEdge("chat", END)
                .compile();
    }

    public String chat(String message) {
        var result = graph.invoke(
                        GraphInput.args(Map.of("message", message)),
                        RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Chat graph returned no state"));
        return result.<String>value("response")
                .orElseThrow(() -> new IllegalStateException("Chat graph returned no response"));
    }
}
