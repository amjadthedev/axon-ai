package app.jer.axon.gui;

import app.jer.axon.Axon;
import app.jer.axon.service.LLMService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AxonConfigScreen extends Screen {
    private TextFieldWidget apiKeyBox;
    private TextFieldWidget providerBox;
    private TextFieldWidget modelBox;

    public AxonConfigScreen() {
        super(Text.literal("Axon AI Configuration"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int fieldWidth = 200;
        int fieldX = centerX - fieldWidth / 2;
        int buttonY = 60;

        this.apiKeyBox = new TextFieldWidget(this.textRenderer, fieldX, buttonY, fieldWidth, 20, Text.literal("API Key"));
        this.apiKeyBox.setText("");
        addDrawableChild(this.apiKeyBox);

        this.providerBox = new TextFieldWidget(this.textRenderer, fieldX, buttonY + 30, fieldWidth, 20, Text.literal("Provider (google/openrouter/etc)"));
        this.providerBox.setText(LLMService.getProvider());
        addDrawableChild(this.providerBox);

        this.modelBox = new TextFieldWidget(this.textRenderer, fieldX, buttonY + 60, fieldWidth, 20, Text.literal("Model"));
        this.modelBox.setText(LLMService.getModel());
        addDrawableChild(this.modelBox);

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> save())
                .dimensions(fieldX, buttonY + 100, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> close())
                .dimensions(fieldX + 110, buttonY + 100, 100, 20).build());
    }

    private void save() {
        String key = this.apiKeyBox.getText().trim();
        String provider = this.providerBox.getText().trim();
        String model = this.modelBox.getText().trim();

        if (provider.isEmpty()) {
            provider = "google";
        }

        try {
            LLMService.setKey(key);
            LLMService.setProvider(provider);
            if (!model.isEmpty()) {
                LLMService.setModel(model);
            }

            Axon.chatMessage(Text.literal("Axon AI settings saved.").formatted(Formatting.GREEN));
            close();
        } catch (Exception e) {
            Axon.chatMessage(Text.literal("Failed to save: " + e.getMessage()).formatted(Formatting.RED));
        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
        super.close();
    }
}
