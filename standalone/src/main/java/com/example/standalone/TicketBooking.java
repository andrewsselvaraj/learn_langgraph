package com.example.standalone;

import java.util.Map;
import java.util.Scanner;

import org.bsc.langgraph4j.CompiledGraph;
import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import org.bsc.langgraph4j.state.AgentState;

public class TicketBooking {

    public static void main(String[] args) throws GraphStateException {
        System.out.print("Enter your name: ");
        String userName;
        int ticketCount;
        try (Scanner scanner = new Scanner(System.in)) {
            userName = scanner.nextLine().trim();
            if (userName.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }

            System.out.print("How many tickets would you like to book? ");
            try {
                ticketCount = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Ticket count must be a whole number", exception);
            }
        }
        if (ticketCount <= 0) {
            throw new IllegalArgumentException("Ticket count must be greater than zero");
        }

        StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);
        AsyncNodeAction<AgentState> welcomeAction = node_async(state -> {
            String name = state.<String>value("name")
                    .orElseThrow(() -> new IllegalStateException("Graph state has no name"));
            return Map.of("message", "Welcome, " + name + ", to Ticket Booking!");
        });
        AsyncNodeAction<AgentState> bookTicketsAction = node_async(state -> {
            String name = state.<String>value("name")
                    .orElseThrow(() -> new IllegalStateException("Graph state has no name"));
            int count = state.<Integer>value("ticketCount")
                    .orElseThrow(() -> new IllegalStateException("Graph state has no ticket count"));
            return Map.of("bookingResult", "Successfully booked " + count + " ticket(s) for " + name + ".");
        });
        AsyncNodeAction<AgentState> rejectBookingAction = node_async(state -> {
            int count = state.<Integer>value("ticketCount")
                    .orElseThrow(() -> new IllegalStateException("Graph state has no ticket count"));
            return Map.of("bookingResult", "Cannot book " + count + " tickets. The limit is 6 tickets per booking.");
        });
        graph.addNode("welcome", welcomeAction);
        graph.addNode("bookTickets", bookTicketsAction);
        graph.addNode("rejectBooking", rejectBookingAction);
        graph.addEdge(START, "welcome");
        graph.addConditionalEdges(
                "welcome",
                edge_async(state -> state.<Integer>value("ticketCount")
                        .orElseThrow(() -> new IllegalStateException("Graph state has no ticket count")) <= 6
                        ? "bookTickets"
                        : "rejectBooking"),
                Map.of("bookTickets", "bookTickets", "rejectBooking", "rejectBooking"));
        graph.addEdge("bookTickets", END);
        graph.addEdge("rejectBooking", END);

        CompiledGraph<AgentState> compiledGraph = graph.compile();
        var result = compiledGraph.invoke(
                        GraphInput.args(Map.of("name", userName, "ticketCount", ticketCount)),
                        RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Graph returned no state"));

        System.out.println(result.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Graph returned no message")));
        System.out.println(result.<String>value("bookingResult")
                .orElseThrow(() -> new IllegalStateException("Graph returned no booking result")));
    }
}
