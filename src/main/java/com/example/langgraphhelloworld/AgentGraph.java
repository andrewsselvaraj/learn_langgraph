package com.example.langgraphhelloworld;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * A tool-calling agent:
 *
 * <pre>
 *   START -> agent -(tool calls?)-> tools -> agent ... -(no tool calls)-> END
 * </pre>
 *
 * The {@code agent} node asks the model what to do next. When the model asks
 * for tools, the {@code tools} node runs them and loops back to {@code agent}
 * so the model can read the results.
 */
@Service
public class AgentGraph {

    private static final String MESSAGES = "messages";

    private final ChatModel chatModel;
    private final List<ToolCallback> toolCallbacks;
    private final Map<String, ToolCallback> toolsByName;
    private final CompiledGraph<AgentState> graph;

    public AgentGraph(ChatModel chatModel, AgentTools agentTools) throws GraphStateException {
        this.chatModel = chatModel;
        this.toolCallbacks = Arrays.asList(ToolCallbacks.from(agentTools));
        this.toolsByName = toolCallbacks.stream()
                .collect(Collectors.toMap(tool -> tool.getToolDefinition().name(), Function.identity()));

        graph = new StateGraph<>(AgentState::new)
                .addNode("agent", node_async(this::callModel))
                .addNode("tools", node_async(this::runTools))
                .addEdge(START, "agent")
                .addConditionalEdges("agent",
                        edge_async(state -> lastMessage(state) instanceof AssistantMessage reply
                                && reply.hasToolCalls() ? "tools" : "end"),
                        Map.of("tools", "tools", "end", END))
                .addEdge("tools", "agent")
                .compile();
    }

    public String ask(String question) {
        List<Message> messages = List.of(
                new SystemMessage("You are a helpful assistant. Use the tools when they help."),
                new UserMessage(question));
        // Spring AI messages are not java.io.Serializable, so skip LangGraph4j's
        // per-step state copy. The nodes never mutate a list; they return a new one.
        var config = RunnableConfig.builder().disableCloneState().build();
        var result = graph.invoke(GraphInput.args(Map.of(MESSAGES, messages)), config)
                .orElseThrow(() -> new IllegalStateException("Agent graph returned no state"));
        return lastMessage(result).getText();
    }

    /** The graph as a Mermaid diagram, ready to paste into any Mermaid viewer. */
    public String mermaid() {
        return graph.getGraph(GraphRepresentation.Type.MERMAID, "Tool-calling agent", false).content();
    }

    // Node "agent": send the conversation to the model. Tool execution is
    // turned off inside Spring AI so the graph decides what happens next.
    private Map<String, Object> callModel(AgentState state) {
        var options = ToolCallingChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .internalToolExecutionEnabled(false)
                .build();
        AssistantMessage reply = chatModel.call(new Prompt(messages(state), options))
                .getResult()
                .getOutput();
        return Map.of(MESSAGES, append(messages(state), reply));
    }

    // Node "tools": run every tool the model asked for and add the results.
    private Map<String, Object> runTools(AgentState state) {
        var reply = (AssistantMessage) lastMessage(state);
        var responses = reply.getToolCalls().stream()
                .map(call -> new ToolResponseMessage.ToolResponse(
                        call.id(), call.name(), toolsByName.get(call.name()).call(call.arguments())))
                .toList();
        return Map.of(MESSAGES, append(messages(state), new ToolResponseMessage(responses)));
    }

    private static List<Message> messages(AgentState state) {
        return state.<List<Message>>value(MESSAGES).orElseThrow();
    }

    private static Message lastMessage(AgentState state) {
        List<Message> messages = messages(state);
        return messages.get(messages.size() - 1);
    }

    private static List<Message> append(List<Message> messages, Message message) {
        var updated = new ArrayList<>(messages);
        updated.add(message);
        return updated;
    }
}
