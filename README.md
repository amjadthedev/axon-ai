# Axon AI

A dumb little AI chat mod for Minecraft 1.21.11 / Fabric. No Baritone, no Meteor Client, no nonsense — just you and an LLM in your game.

## What it does

Type `/a` followed by whatever you want to say, and an LLM replies. That's it. It's a client-side mod, so it works on any server you join (as long as the server doesn't block client commands).

## Commands

- `/a <message>` — Talk to the AI
- `/a-key <api key> [provider]` — Set your API key (it guesses the provider from the key prefix, e.g. `sk-or-v1-...` = OpenRouter, `AIza...` = Google)
- `/a-model <model-id>` — Switch models
- `/a-clear` — Clear the conversation
- `/a-gui` — Open the config screen

## Providers

It works with any OpenAI-compatible provider. Google, OpenAI, Grok, DeepSeek, Mistral, OpenRouter, Anthropic, Azure, Vertex — whatever you've got a key for. OpenRouter's `openrouter/free` is a good default if you don't want to pick.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.3
- Fabric API 0.141.6+

## Install

1. Install Fabric Loader 0.19.3 for 1.21.11
2. Drop `axon-ai-1.0.0.jar` and `fabric-api-*.jar` into your `mods/` folder
3. Launch the game
4. Get an API key (Google AI Studio, OpenRouter, wherever)
5. In-game: `/a-key <your_key>`
6. `/a hello`

## License

GPL-3.0-or-later. See [LICENSE](LICENSE).
