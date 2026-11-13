package com.example.standalone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.imageio.ImageIO;

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

public class StartEndLangGraph {

    public static void main(String[] args) throws GraphStateException {
        StateGraph<AgentState> graphBuilder = new StateGraph<>(AgentState::new);
        graphBuilder.addNode("hello", node_async(state -> Map.of("message", "Hello, world!")));
        graphBuilder.addEdge(START, "hello");
        graphBuilder.addEdge("hello", END);

        CompiledGraph<AgentState> graph = graphBuilder.compile();
        var result = graph.invoke(GraphInput.args(Map.of()), RunnableConfig.empty())
                .orElseThrow(() -> new IllegalStateException("Graph returned no state"));

        System.out.println(result.<String>value("message")
                .orElseThrow(() -> new IllegalStateException("Graph returned no message")));
        System.out.println(graph.getGraph(GraphRepresentation.Type.MERMAID).content());

        try {
            Path imagePath = Path.of("standalone", "start-end-graph.png").toAbsolutePath();
            writeGraphImage(imagePath);
            System.out.println("Graph image saved to: " + imagePath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save graph image", e);
        }
    }

    private static void writeGraphImage(Path imagePath) throws IOException {
        BufferedImage image = new BufferedImage(720, 240, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

            graphics.setStroke(new BasicStroke(2));
            drawArrow(graphics, 164, 120, 280, 120);
            drawArrow(graphics, 440, 120, 556, 120);

            graphics.setColor(new Color(225, 238, 250));
            graphics.fillOval(72, 76, 96, 88);
            graphics.fillOval(552, 76, 96, 88);
            graphics.setColor(new Color(50, 90, 130));
            graphics.drawOval(72, 76, 96, 88);
            graphics.drawOval(552, 76, 96, 88);

            graphics.setColor(new Color(232, 245, 233));
            graphics.fillRoundRect(280, 80, 160, 80, 16, 16);
            graphics.setColor(new Color(70, 120, 80));
            graphics.drawRoundRect(280, 80, 160, 80, 16, 16);

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            drawCenteredText(graphics, "START", 120, 125);
            drawCenteredText(graphics, "hello", 360, 125);
            drawCenteredText(graphics, "END", 600, 125);
        } finally {
            graphics.dispose();
        }

        Files.createDirectories(imagePath.toAbsolutePath().getParent());
        if (!ImageIO.write(image, "png", imagePath.toFile())) {
            throw new IOException("No PNG image writer is available");
        }
    }

    private static void drawArrow(Graphics2D graphics, int startX, int y, int endX, int endY) {
        graphics.setColor(new Color(70, 70, 70));
        graphics.drawLine(startX, y, endX, endY);
        graphics.fillPolygon(
                new int[]{endX, endX - 10, endX - 10},
                new int[]{endY, endY - 6, endY + 6},
                3);
    }

    private static void drawCenteredText(Graphics2D graphics, String text, int centerX, int baselineY) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.setColor(new Color(35, 45, 55));
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }
}
