package app.jer.axon.command;

import app.jer.axon.Axon;
import app.jer.axon.Utils;
import app.jer.axon.gui.AxonConfigScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GuiCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> commandContext) {
        Axon.LOGGER.info("Received /a-gui command, opening configuration screen");
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> client.setScreen(new AxonConfigScreen()));
        MutableText msg = Utils.prefixText("Axon AI")
                .append(Text.literal("Opened Axon AI configuration screen.").formatted(Formatting.GREEN));
        Axon.chatMessage(msg);
        return 1;
    }
}
