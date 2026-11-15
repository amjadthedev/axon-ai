package app.jer.axon.command;

import app.jer.axon.Axon;
import app.jer.axon.agent.AxonAgent;
import app.jer.axon.service.LLMService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandRegistrar {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                environment
        ) -> {
            dispatcher.register(CommandManager.literal("a")
                    .then(CommandManager.argument("message", StringArgumentType.greedyString())
                            .executes(new PromptCommand())
                    )
                    .executes(ctx -> {
                        Axon.chatMessage("Hello World!");
                        return 1;
                    })
            );
            dispatcher.register(CommandManager.literal("a-clear")
                    .executes(new ClearCommand())
            );
            dispatcher.register(CommandManager.literal("a-key")
                    .then(CommandManager.argument("key", StringArgumentType.string())
                            .executes(new KeyCommand())
                            .then(CommandManager.argument("provider", StringArgumentType.string())
                                    .executes(new KeyCommand())
                            )
                    )
            );
            dispatcher.register(CommandManager.literal("a-model")
                    .executes(new ModelCommand.ShowCommand())
                    .then(CommandManager.argument("model", StringArgumentType.greedyString())
                            .executes(new ModelCommand())
                    )
            );
            dispatcher.register(CommandManager.literal("a-gui")
                    .executes(new GuiCommand())
            );
        });
        Axon.LOGGER.info("Axon AI commands registered");
    }

    public static class ClearCommand implements Command<ServerCommandSource> {
        @Override
        public int run(CommandContext<ServerCommandSource> commandContext) {
            try {
                AxonAgent.clearChat();
            } catch (Exception e) {
                Axon.LOGGER.error("Error sending clear command to agent", e);
                commandContext.getSource().sendError(Text.literal("Error: " + e.getMessage()));
                return 0;
            }
            return 1;
        }
    }
}
