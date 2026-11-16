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
import java.util.Locale;

public class ModelCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> commandContext) {
        String requestedModel = StringArgumentType.getString(commandContext, "model").trim();
        if (requestedModel.isEmpty()) {
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Usage: /a-model <model-id> or /a-model auto").formatted(Formatting.RED));
            Axon.chatMessage(msg);
            return 0;
        }
        if ("auto".equalsIgnoreCase(requestedModel) || "default".equalsIgnoreCase(requestedModel) || "none".equalsIgnoreCase(requestedModel)) {
            String defaultModel = LLMService.resetModelToProviderDefault();
            if (defaultModel != null) {
                MutableText msg = Utils.prefixText("Axon AI")
                        .append(Text.literal("Model reset to provider default: ").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(defaultModel).formatted(Formatting.GREEN));
                Axon.chatMessage(msg);
            } else {
                MutableText msg = Utils.prefixText("Axon AI")
                        .append(Text.literal("No default model known for provider '" + LLMService.getProvider() + "'. Provide a model id explicitly.").formatted(Formatting.RED));
                Axon.chatMessage(msg);
            }
            return 1;
        }
        LLMService.setModel(requestedModel);
        MutableText msg = Utils.prefixText("Axon AI")
                .append(Text.literal("Model set to ").formatted(Formatting.DARK_GREEN))
                .append(Text.literal(LLMService.getModel()).formatted(Formatting.GREEN));
        Axon.chatMessage(msg);
        return 1;
    }

    public static class ShowCommand implements Command<ServerCommandSource> {
        @Override
        public int run(CommandContext<ServerCommandSource> commandContext) {
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Current model: ").formatted(Formatting.AQUA))
                    .append(Text.literal(LLMService.getModel()).formatted(Formatting.GREEN));
            Axon.chatMessage(msg);
            MutableText msg2 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Current provider: ").formatted(Formatting.AQUA))
                    .append(Text.literal(LLMService.getProviderDisplayName(LLMService.getProvider()) + " (" + LLMService.getProvider() + ")").formatted(Formatting.GREEN));
            Axon.chatMessage(msg2);
            if (!"google".equals(LLMService.getProvider()) && LLMService.getModel().toLowerCase(Locale.ROOT).startsWith("gemini")) {
                MutableText msg3 = Utils.prefixText("Axon AI")
                        .append(Text.literal("Warning: model '" + LLMService.getModel() + "' looks like a Google Gemini model, but the provider is not Google. Set a model ID your provider supports, e.g. /a-model <provider-model-id>").formatted(Formatting.RED));
                Axon.chatMessage(msg3);
            }
            String defaultModel = LLMService.getDefaultModelFor(LLMService.getProvider());
            if (defaultModel != null && !defaultModel.equals(LLMService.getModel())) {
                MutableText msg4 = Utils.prefixText("Axon AI")
                        .append(Text.literal("Free default for this provider: " + defaultModel).formatted(Formatting.DARK_AQUA));
                Axon.chatMessage(msg4);
            }
            MutableText msg5 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Suggested models for " + LLMService.getProvider() + ":").formatted(Formatting.GRAY));
            Axon.chatMessage(msg5);
            for (String suggestion : LLMService.getSuggestedModels(LLMService.getProvider())) {
                MutableText msg6 = Utils.prefixText("Axon AI")
                        .append(Text.literal(" - " + suggestion).formatted(Formatting.DARK_AQUA));
                Axon.chatMessage(msg6);
            }
            MutableText msg7 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Usage: /a-model <model-id> or /a-model auto").formatted(Formatting.GRAY));
            Axon.chatMessage(msg7);
            return 1;
        }
    }
}
