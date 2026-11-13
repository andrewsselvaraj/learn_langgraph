package com.example.langgraphhelloworld;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class HelloController {

    private final HelloWorldGraph helloWorldGraph;

    public HelloController(HelloWorldGraph helloWorldGraph) {
        this.helloWorldGraph = helloWorldGraph;
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        return new ChatResponse(helloWorldGraph.chat(request.message()));
    }

    // Opening /chat in a browser sends a GET; send it to the chat page instead of a 405.
    @GetMapping("/chat")
    public ResponseEntity<Void> chatPage() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String response) {
    }
}
