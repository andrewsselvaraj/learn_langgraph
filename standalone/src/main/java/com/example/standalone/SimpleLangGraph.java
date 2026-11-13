package com.example.standalone;

import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import org.bsc.langgraph4j.state.AgentState;

public class SimpleLangGraph {

    public static void main(String[] args) throws GraphStateException {
        // State is the data passed between graph nodes.
        StateGraph<AgentState> graphBuilder = new StateGraph<>(AgentState::new);

        // A node reads the state and returns values to add to it.
        graphBuilder.addNode("greet", node_async(SimpleLangGraph::greet));

        // Connect the graph's start to our node, then our node to its end.
        graphBuilder.addEdge(START, "greet");
        graphBuilder.addEdge("greet", END);

        // Compile the graph before running it.
        CompiledGraph<AgentState> graph = graphBuilder.compile();

        // Run the graph with an initial state containing a name.
        var result = graph.invoke(
                        GraphInput.args(Map.of("name", "LangGraph")),
                        RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Graph returned no state"));

        System.out.println(result.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Graph returned no message")));
    }

    private static Map<String, Object> greet(AgentState state) {
        String name = state.<String>value("name").orElse("world");
        return Map.of("message", "Hello, " + name + "!");
    }
}
