# Axon AI

A Fabric 1.21.11 mod that lets you talk to an LLM in-game using `/a` commands.

## Commands

- `/a <message>` — Send a message to the AI
- `/a-key <api key> [provider]` — Set your API key and provider (e.g. `/a-key sk-or-v1-... openrouter`)
- `/a-model <model-id>` — Switch models
- `/a-clear` — Clear conversation history
- `/a-gui` — Open the config screen

## Providers

Works with any OpenAI-compatible provider: Google, OpenAI, Grok, DeepSeek, Mistral, OpenRouter, Anthropic, Azure, Vertex. OpenRouter's `openrouter/free` model is free to use.

## Install

1. Install Fabric Loader 0.19.3 for Minecraft 1.21.11
2. Put `axon-ai-1.0.0.jar` and `fabric-api-0.141.6+1.21.11.jar` in your `mods/` folder
3. Launch the game
4. Get an API key (Google AI Studio, OpenRouter, etc.)
5. Run `/a-key <your_api_key> <provider>` in-game
6. Run `/a <message>` to talk to the AI

## License

GPL-3.0-or-later. See [LICENSE](LICENSE).
