package app.jer.axon.mixin;

import app.jer.axon.Axon;
import app.jer.axon.Utils;
import app.jer.axon.agent.AxonAgent;
import app.jer.axon.gui.AxonConfigScreen;
import app.jer.axon.service.LLMService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void onSendChatCommand(String command, CallbackInfo ci) {
        if (command == null || !command.startsWith("/a")) {
            return;
        }

        String cmd = command.substring(1).trim();

        if (cmd.equals("a")) {
            Axon.chatMessage("Hello World!");
            ci.cancel();
            return;
        }

        if (cmd.startsWith("a ")) {
            String message = cmd.substring(2).trim();
            if (!message.isEmpty()) {
                AxonAgent.userMessage(message);
            }
            ci.cancel();
            return;
        }

        if (cmd.startsWith("a-key ")) {
            String rest = cmd.substring(6).trim();
            String[] parts = rest.split(" ", 2);
            String key = parts[0];
            String provider = parts.length > 1 ? parts[1] : null;

            LLMService.setKey(key);
            if (provider != null && !provider.isEmpty()) {
                try {
                    LLMService.setProvider(provider);
                } catch (IllegalArgumentException e) {
                    MutableText msg = Utils.prefixText("Axon AI")
                            .append(Text.literal("Unknown provider '" + provider + "'").formatted(Formatting.RED));
                    Axon.chatMessage(msg);
                    ci.cancel();
                    return;
                }
            }

            String activeProvider = LLMService.getProvider();
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Key set successfully").formatted(Formatting.DARK_GREEN))
                    .append(Text.literal(" (provider: " + LLMService.getProviderDisplayName(activeProvider) + ")").formatted(Formatting.GRAY));
            Axon.chatMessage(msg);
            ci.cancel();
            return;
        }

        if (cmd.equals("a-model")) {
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Current model: ").formatted(Formatting.AQUA))
                    .append(Text.literal(LLMService.getModel()).formatted(Formatting.GREEN));
            Axon.chatMessage(msg);
            MutableText msg2 = Utils.prefixText("Axon AI")
                    .append(Text.literal("Current provider: ").formatted(Formatting.AQUA))
                    .append(Text.literal(LLMService.getProviderDisplayName(LLMService.getProvider()) + " (" + LLMService.getProvider() + ")").formatted(Formatting.GREEN));
            Axon.chatMessage(msg2);
            ci.cancel();
            return;
        }

        if (cmd.startsWith("a-model ")) {
            String model = cmd.substring(8).trim();
            if (!model.isEmpty()) {
                LLMService.setModel(model);
                MutableText msg = Utils.prefixText("Axon AI")
                        .append(Text.literal("Model set to ").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(LLMService.getModel()).formatted(Formatting.GREEN));
                Axon.chatMessage(msg);
            }
            ci.cancel();
            return;
        }

        if (cmd.equals("a-clear")) {
            AxonAgent.clearChat();
            ci.cancel();
            return;
        }

        if (cmd.equals("a-gui")) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(new AxonConfigScreen()));
            MutableText msg = Utils.prefixText("Axon AI")
                    .append(Text.literal("Opened Axon AI configuration screen.").formatted(Formatting.GREEN));
            Axon.chatMessage(msg);
            ci.cancel();
            return;
        }
    }
}
