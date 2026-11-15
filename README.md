# Axon AI

A standalone AI chat mod for Minecraft 1.21.11 / Fabric. Talk to an LLM through `/a` commands.

## Features

- **`/a <message>`** — Send a message to the AI agent
- **`/a-key <api key> [provider]`** — Set your API key (provider is auto-detected from the key prefix)
- **`/a-model <model-id>`** — Switch the model the agent uses
- **`/a-clear`** — Clear conversation memory
- **`/a-gui`** — Open the configuration screen

## Supported Providers

| Provider | Key prefix | Default free model |
|---|---|---|
| Google AI Studio | `AIza...` | `gemini-flash-lite-latest` |
| OpenAI | `sk-...` / `sk-proj-...` | `gpt-4o-mini` |
| Grok (xAI) | `gsk-...` | `grok-3-mini` |
| DeepSeek | `sk-...` | `deepseek-chat` |
| Mistral | `mistral-...` | `mistral-small-latest` |
| Anyscale | `sk-...` | `meta-llama-3.1-8b-instruct` |
| Azure OpenAI | `sk-...` | `gpt-4o-mini` |
| Anthropic | `sk-ant-...` | `claude-3-haiku-20240307` |
| OpenRouter | `sk-or-v1-...` | `openrouter/free` |

## Requirements

- Minecraft **1.21.11**
- [Fabric Loader](https://fabricmc.net/use/installer/) **0.19.3**
- [Fabric API](https://modrinth.com/mod/fabric-api) **0.141.6+1.21.11**
- Java **21**

## Installation

1. Install Fabric Loader 0.19.3 for Minecraft 1.21.11
2. Put `axon-ai-1.0.0.jar` and `fabric-api-*.jar` in your `mods/` folder
3. Launch Minecraft
4. Get an API key from your provider (e.g. [Google AI Studio](https://aistudio.google.com/apikey) or [OpenRouter](https://openrouter.ai))
5. In-game, run `/a-key <your_api_key>`
6. Use `/a <your message>` to talk to the AI

## License

GPL-3.0-or-later. See [LICENSE](LICENSE).
