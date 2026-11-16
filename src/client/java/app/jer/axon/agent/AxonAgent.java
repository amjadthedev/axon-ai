package app.jer.axon.agent;

import app.jer.axon.Axon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AxonAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger("axon-agent");

    private static volatile boolean initialized = false;

    public static void initialize() {
        initialized = true;
        LOGGER.info("Axon AI agent loop started");
    }

    public static void userMessage(String text) {
        if (!initialized) {
            return;
        }
        LOGGER.info("Axon AI user message queued ({} chars)", text.length());

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            Axon.chatMessage(Text.literal("Axon AI: world not loaded yet."));
            return;
        }

        Axon.chatMessage(Text.literal("Axon AI: processing your message..."));
    }

    public static void clearChat() {
        LOGGER.info("Axon AI chat cleared");
        Axon.chatMessage(Text.literal("Axon AI: conversation memory cleared."));
    }
}
