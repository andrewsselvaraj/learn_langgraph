package com.example.standalone;

import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import org.bsc.langgraph4j.state.AgentState;

public class MyFile {

    public static void main(String[] args) throws GraphStateException {
        System.out.println("Hello, world!");

        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);
        // Define a node that returns a greeting message
        AsyncNodeAction<AgentState> greetAction = node_async(state -> {
            System.out.println("Running the greet node");
            return Map.of("message", "Hello from LangGraph!");
        });
        graph.addNode("greet", greetAction);
        graph.addEdge(START, "greet");
        graph.addEdge("greet", END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        var result = compiledGraph.invoke(
                        GraphInput.args(Map.of()),
                        RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Graph returned no state"));

        System.out.println(result.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Graph returned no message")));
    }
}