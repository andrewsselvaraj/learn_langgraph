package com.example.standalone;

import java.util.Map;

import org.bsc.langgraph4j.CompiledGraph;
import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import org.bsc.langgraph4j.state.AgentState;

public class StartToEndLangGraph {

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);
        graph.addNode("greet", node_async(StartToEndLangGraph::greet));
        graph.addNode("addClosing", node_async(StartToEndLangGraph::addClosing));
        graph.addEdge(START, "greet");
        graph.addEdge("greet", "addClosing");
        graph.addEdge("addClosing", END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        var result = compiledGraph.invoke(
                        GraphInput.args(Map.of("name", "LangGraph")),
                        RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Graph returned no state"));

        System.out.println(result.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Graph returned no message")));
        System.out.println("\n\n"+compiledGraph.getGraph(GraphRepresentation.Type.MERMAID).content());
    }

    private static Map<String, Object> greet(AgentState state) {
        String name = state.<String>value("name").orElse("world");
        return Map.of("message", "Hello, " + name + "!");
    }

    private static Map<String, Object> addClosing(AgentState state) {
        String message = state.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Greeting is missing"));
        return Map.of("message", message + " Have a nice day!");
    }
}
