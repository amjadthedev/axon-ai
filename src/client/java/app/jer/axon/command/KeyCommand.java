package app.jer.axon.command;

import app.jer.axon.Axon;
import app.jer.axon.Utils;
import app.jer.axon.service.LLMService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class KeyCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> commandContext) {
        String key = StringArgumentType.getString(commandContext, "key").trim();
        Axon.LOGGER.info("Received /a-key command (key length {}).", key.length());

        String provider = null;
        try {
            provider = StringArgumentType.getString(commandContext, "provider").trim();
        } catch (IllegalArgumentException ignored) {
        }

        if (provider != null && !LLMService.isSupportedProvider(provider)) {
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Unknown provider '" + provider + "'.").formatted(Formatting.RED));
            Axon.chatMessage(msg);
            MutableText msg2 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Providers available: " + LLMService.getProvidersDescription()).formatted(Formatting.GRAY));
            Axon.chatMessage(msg2);
            MutableText msg3 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Usage: /a-key <api key> [provider]").formatted(Formatting.GRAY));
            Axon.chatMessage(msg3);
            return 0;
        }

        try {
            LLMService.setKey(key);
            if (provider != null) {
                LLMService.setProvider(provider);
                MutableText msg = Utils.prefixText("Axon AI")
                        .append(Text.literal("Provider set to " + LLMService.getProviderDisplayName(provider) + ".").formatted(Formatting.GRAY));
                Axon.chatMessage(msg);
            } else {
                String detected = LLMService.detectProvider(key);
                if (detected != null) {
                    MutableText msg = Utils.prefixText("Axon AI")
                            .append(Text.literal("Auto-detected provider from key: " + LLMService.getProviderDisplayName(detected)).formatted(Formatting.GRAY));
                    Axon.chatMessage(msg);
                } else {
                    MutableText msg = Utils.prefixText("Axon AI")
                            .append(Text.literal("Could not detect provider from key; using " + LLMService.getProviderDisplayName(LLMService.getProvider()) + ". Specify it with /a-key <key> <provider_name>.").formatted(Formatting.YELLOW));
                    Axon.chatMessage(msg);
                }
            }
        } catch (Exception e) {
            Axon.LOGGER.error("Failed to set LLM key/provider", e);
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Failed to set key: " + e.getMessage()).formatted(Formatting.RED));
            Axon.chatMessage(msg);
            return 0;
        }

        String activeProvider = LLMService.getProvider();
        MutableText msg = Utils.prefixText("Axon AI")
                .append(Text.literal("Key set successfully").formatted(Formatting.DARK_GREEN))
                .append(Text.literal(" (provider: " + LLMService.getProviderDisplayName(activeProvider) + ")").formatted(Formatting.GRAY));
        Axon.chatMessage(msg);
        String autoModel = LLMService.getDefaultModelFor(activeProvider);
        if (autoModel != null) {
            MutableText msg2 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Auto-selected model: " + autoModel).formatted(Formatting.AQUA))
                    .append(Text.literal(" (change with /a-model <model name>)").formatted(Formatting.GRAY));
            Axon.chatMessage(msg2);
            MutableText msg3 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Suggested models for " + activeProvider + ":").formatted(Formatting.GRAY));
            Axon.chatMessage(msg3);
            for (String suggestion : LLMService.getSuggestedModels(activeProvider)) {
                MutableText msg4 = Utils.prefixText("Axon AI")
                        .append(Text.literal(" - " + suggestion).formatted(Formatting.DARK_AQUA));
                Axon.chatMessage(msg4);
            }
        }
        MutableText msg5 = Utils.prefixText("Axon AI")
                .append(Text.literal("Now set the model using /a-model <model name>").formatted(Formatting.AQUA));
        Axon.chatMessage(msg5);
        MutableText msg6 = Utils.prefixText("Axon AI")
                .append(Text.literal("Providers available: " + LLMService.getProvidersDescription()).formatted(Formatting.GRAY));
        Axon.chatMessage(msg6);
        return 1;
    }
}
