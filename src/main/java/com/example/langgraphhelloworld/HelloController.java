package com.example.langgraphhelloworld;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class HelloController {

    private final HelloWorldGraph helloWorldGraph;
    private final AgentGraph agentGraph;

    public HelloController(HelloWorldGraph helloWorldGraph, AgentGraph agentGraph) {
        this.helloWorldGraph = helloWorldGraph;
        this.agentGraph = agentGraph;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        return new ChatResponse(helloWorldGraph.chat(request.message()));
    }

    @PostMapping(value = "/agent", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse agent(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        return new ChatResponse(agentGraph.ask(request.message()));
    }

    @GetMapping(value = "/agent/graph", produces = MediaType.TEXT_PLAIN_VALUE)
    public String agentGraph() {
        return agentGraph.mermaid();
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String response) {
    }
}
