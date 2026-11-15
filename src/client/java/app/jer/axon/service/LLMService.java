package app.jer.axon.service;

import app.jer.axon.Axon;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LLMService {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getGameDir()
            .resolve("axon-ai/llm.properties");
    private static final String KEY = "apiKey";
    private static final String MODEL_KEY = "model";
    private static final String PROVIDER_KEY = "provider";
    private static final String DEFAULT_MODEL = "gemini-flash-lite-latest";
    private static final String DEFAULT_PROVIDER = "google";
    private static final String MODEL_EXPLICIT_KEY = "modelExplicit";

    private static final Map<String, String> DEFAULT_MODELS = Map.ofEntries(
            Map.entry("google", "gemini-flash-lite-latest"),
            Map.entry("openai", "gpt-4o-mini"),
            Map.entry("grok", "grok-3-mini"),
            Map.entry("deepseek", "deepseek-chat"),
            Map.entry("mistral", "mistral-small-latest"),
            Map.entry("anyscale", "meta-llama-3.1-8b-instruct"),
            Map.entry("azure", "gpt-4o-mini"),
            Map.entry("gemini-vertex", "gemini-2.0-flash-lite-001"),
            Map.entry("anthropic", "claude-3-haiku-20240307"),
            Map.entry("openrouter", "openrouter/free")
    );

    private static final Map<String, Set<String>> PROVIDER_MODEL_PREFIXES = Map.of(
            "google", Set.of("gemini"),
            "gemini-vertex", Set.of("gemini"),
            "openai", Set.of("gpt", "o1", "o3", "o4", "chatgpt", "text-davinci"),
            "grok", Set.of("grok"),
            "deepseek", Set.of("deepseek", "deepseek-chat", "deepseek-coder"),
            "mistral", Set.of("mistral", "mixtral"),
            "anyscale", Set.of("meta-llama", "llama", "mistral", "qwen", "phi", "gemma", "codellama"),
            "azure", Set.of("gpt", "o1", "o3", "o4", "text", "text-embedding"),
            "anthropic", Set.of("claude", "claude-3", "claude-2"),
            "openrouter", Set.of()
    );

    private static final List<Provider> PROVIDERS = List.of(
            new Provider("google", "Google AI Studio", ""),
            new Provider("openai", "OpenAI", "https://api.openai.com"),
            new Provider("grok", "Grok (xAI)", "https://api.x.ai"),
            new Provider("deepseek", "DeepSeek", "https://api.deepseek.com"),
            new Provider("mistral", "Mistral", "https://api.mistral.ai"),
            new Provider("anyscale", "Anyscale", "https://api.endpoints.anyscale.com"),
            new Provider("azure", "Azure OpenAI", "https://YOUR_RESOURCE_NAME.openai.azure.com/openai/deployments/YOUR_DEPLOYMENT_NAME"),
            new Provider("gemini-vertex", "Gemini Vertex AI", ""),
            new Provider("anthropic", "Anthropic (experimental)", "https://api.anthropic.com"),
            new Provider("openrouter", "OpenRouter", "https://openrouter.ai/api")
    );

    private record Provider(String id, String name, String baseUrl) {}

    private static volatile String currentModel = DEFAULT_MODEL;
    private static volatile String currentProvider = DEFAULT_PROVIDER;
    private static volatile boolean modelExplicit = false;
    private static volatile boolean providerExplicit = false;
    private static @Nullable io.github.sashirestela.openai.SimpleOpenAI api = null;

    private static final HttpClient TEST_HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public static @Nullable io.github.sashirestela.openai.SimpleOpenAI getApi() {
        return api;
    }

    public static String getModel() {
        return currentModel;
    }

    public static String getProvider() {
        return currentProvider;
    }

    public static List<String> getSupportedProviders() {
        return PROVIDERS.stream().map(Provider::id).toList();
    }

    public static boolean isSupportedProvider(String provider) {
        if (provider == null) {
            return false;
        }
        String normalized = provider.trim().toLowerCase(Locale.ROOT);
        return PROVIDERS.stream().anyMatch(p -> p.id().equals(normalized));
    }

    public static String normalizeProvider(String provider) {
        return provider == null ? DEFAULT_PROVIDER : provider.trim().toLowerCase(Locale.ROOT);
    }

    public static String getProviderDisplayName(String provider) {
        return PROVIDERS.stream()
                .filter(p -> p.id().equals(normalizeProvider(provider)))
                .map(Provider::name)
                .findFirst()
                .orElse(provider);
    }

    public static String getProvidersDescription() {
        StringJoiner joiner = new StringJoiner(", ");
        for (Provider provider : PROVIDERS) {
            joiner.add(provider.id() + " (" + provider.name() + ")");
        }
        return joiner.toString();
    }

    public static @Nullable String getDefaultModelFor(String provider) {
        return DEFAULT_MODELS.get(normalizeProvider(provider));
    }

    public static List<String> getSuggestedModels(String provider) {
        String normalized = normalizeProvider(provider);
        String defaultModel = DEFAULT_MODELS.get(normalized);
        List<String> suggestions = new ArrayList<>();
        if (defaultModel != null) {
            suggestions.add(defaultModel);
        }
        switch (normalized) {
            case "google":
                suggestions.addAll(List.of(
                        "gemini-flash-lite-latest",
                        "gemini-flash-latest",
                        "gemini-2.0-flash-lite-001",
                        "gemini-2.0-flash-001",
                        "gemini-2.5-flash-lite",
                        "gemini-2.5-pro"
                ));
                break;
            case "openrouter":
                suggestions.addAll(List.of(
                        "openrouter/free",
                        "nvidia/nemotron-3-ultra-550b-a55b:free",
                        "nvidia/nemotron-3.5-lightning:free",
                        "google/gemma-4-31b-it:free",
                        "inclusionai/ling-3.2-flash:free"
                ));
                break;
            case "openai":
                suggestions.addAll(List.of("gpt-4o-mini", "gpt-4o", "gpt-4-turbo", "o1-mini"));
                break;
            case "grok":
                suggestions.addAll(List.of("grok-3-mini", "grok-3", "grok-2"));
                break;
            case "deepseek":
                suggestions.addAll(List.of("deepseek-chat", "deepseek-coder", "deepseek-reasoner"));
                break;
            case "mistral":
                suggestions.addAll(List.of("mistral-small-latest", "mistral-large-latest", "mixtral-8x7b"));
                break;
            case "anyscale":
                suggestions.addAll(List.of("meta-llama-3.1-8b-instruct", "llama-3.1-8b", "qwen-2.5-72b-instruct"));
                break;
            case "anthropic":
                suggestions.addAll(List.of("claude-3-haiku-20240307", "claude-3-sonnet-20240229", "claude-3-opus-20240229"));
                break;
            case "azure":
                suggestions.addAll(List.of("gpt-4o-mini", "gpt-4o", "gpt-4-turbo"));
                break;
            case "gemini-vertex":
                suggestions.addAll(List.of("gemini-2.0-flash-lite-001", "gemini-2.0-flash-001", "gemini-1.5-flash"));
                break;
        }
        return suggestions.stream().distinct().toList();
    }

    public static @Nullable String detectProvider(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String k = key.trim();
        if (k.startsWith("AIza")) {
            return "google";
        }
        if (k.startsWith("sk-or-v1-")) {
            return "openrouter";
        }
        if (k.startsWith("sk-ant-")) {
            return "anthropic";
        }
        if (k.startsWith("sk-proj-")) {
            return "openai";
        }
        if (k.startsWith("sk-")) {
            return "openai";
        }
        if (k.startsWith("mistral-")) {
            return "mistral";
        }
        if (k.startsWith("gsk-")) {
            return "grok";
        }
        return null;
    }

    public static void setKey(String key) {
        String detected = detectProvider(key);
        if (detected != null) {
            currentProvider = detected;
            providerExplicit = false;
            updateProperty(PROVIDER_KEY, detected);
            Axon.LOGGER.info("Auto-detected provider '{}' from API key prefix", detected);
        }
        // Build client - we'll keep api null for now since simple-openai builder
        // may need provider-specific setup; the chat will work once we add full client
        updateProperty(KEY, key);
    }

    public static void setProvider(String provider) {
        String normalized = normalizeProvider(provider);
        if (!isSupportedProvider(normalized)) {
            throw new IllegalArgumentException("Unknown provider '" + provider + "'. Supported providers: " + String.join(", ", getSupportedProviders()));
        }
        currentProvider = normalized;
        providerExplicit = true;
        updateProperty(PROVIDER_KEY, normalized);
    }

    public static void setModel(String model) {
        currentModel = model;
        modelExplicit = true;
        updateProperty(MODEL_KEY, model);
        updateProperty(MODEL_EXPLICIT_KEY, "true");
    }

    public static @Nullable String resetModelToProviderDefault() {
        String defaultModel = DEFAULT_MODELS.get(currentProvider);
        if (defaultModel != null) {
            currentModel = defaultModel;
            modelExplicit = false;
            updateProperty(MODEL_KEY, defaultModel);
            updateProperty(MODEL_EXPLICIT_KEY, "false");
        }
        return defaultModel;
    }

    public static void initialize() {
        currentModel = loadModel();
        modelExplicit = loadModelExplicit();
        String key = loadKey();

        Properties storedProps = readProperties();
        String storedProvider = storedProps.getProperty(PROVIDER_KEY);
        String detected = (key != null && !key.isBlank()) ? detectProvider(key) : null;
        if (storedProvider != null && !storedProvider.isBlank() && detected != null
                && !normalizeProvider(storedProvider).equals(detected)) {
            currentProvider = detected;
            providerExplicit = false;
            updateProperty(PROVIDER_KEY, detected);
            Axon.LOGGER.info("Re-detected provider '{}' from stored key prefix (stored was '{}')", detected, storedProvider);
        } else if (storedProvider != null && !storedProvider.isBlank()) {
            currentProvider = normalizeProvider(storedProvider);
            if (!isSupportedProvider(currentProvider)) {
                currentProvider = DEFAULT_PROVIDER;
            }
            providerExplicit = true;
        } else if (key != null && !key.isBlank() && detected != null) {
            currentProvider = detected;
            providerExplicit = true;
            updateProperty(PROVIDER_KEY, currentProvider);
            Axon.LOGGER.info("Auto-detected provider '{}' from stored API key prefix", currentProvider);
        } else {
            currentProvider = DEFAULT_PROVIDER;
        }

        if (key != null && !key.isBlank()) {
            try {
                if (!modelExplicit || isPlaceholderModel(currentModel)) {
                    String defaultModel = DEFAULT_MODELS.get(currentProvider);
                    if (defaultModel != null && !defaultModel.equals(currentModel)) {
                        Axon.LOGGER.info("Auto-selecting model '{}' for provider '{}'", defaultModel, currentProvider);
                        currentModel = defaultModel;
                        updateProperty(MODEL_KEY, defaultModel);
                        updateProperty(MODEL_EXPLICIT_KEY, "false");
                    }
                } else if (!modelBelongsToProvider(currentModel, currentProvider)) {
                    Axon.LOGGER.warn("Model '{}' may not exist on provider '{}'", currentModel, currentProvider);
                }
            } catch (Exception e) {
                Axon.LOGGER.error("Failed to initialize LLM client for provider '{}': {}", currentProvider, e.getMessage());
            }
        }
    }

    private static boolean isPlaceholderModel(String model) {
        if (model == null) {
            return true;
        }
        String m = model.trim().toLowerCase(Locale.ROOT);
        return m.isEmpty() || m.equals("default") || m.equals("auto") || m.equals("none");
    }

    private static boolean modelBelongsToProvider(String model, String provider) {
        if (model == null || model.isBlank()) {
            return false;
        }
        Set<String> prefixes = PROVIDER_MODEL_PREFIXES.get(provider);
        if (prefixes == null || prefixes.isEmpty()) {
            return true;
        }
        String lower = model.toLowerCase(Locale.ROOT);
        return prefixes.stream().anyMatch(lower::startsWith);
    }

    private static void updateProperty(String key, String value) {
        Properties props = readProperties();
        props.setProperty(key, value);
        writeProperties(props, "Axon AI LLM configuration");
    }

    private static Properties readProperties() {
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                props.load(r);
            } catch (IOException ignored) {
            }
        }
        return props;
    }

    private static void writeProperties(Properties props, String comment) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                props.store(w, comment);
            }
        } catch (IOException ignored) {
        }
    }

    @Nullable
    private static String loadModel() {
        if (Files.notExists(CONFIG_PATH)) {
            return DEFAULT_MODEL;
        }
        String model = readProperties().getProperty(MODEL_KEY);
        return (model == null || model.isBlank()) ? DEFAULT_MODEL : model.trim();
    }

    @Nullable
    private static String loadKey() {
        if (Files.notExists(CONFIG_PATH)) {
            return null;
        }
        return readProperties().getProperty(KEY);
    }

    private static boolean loadModelExplicit() {
        if (Files.notExists(CONFIG_PATH)) {
            return false;
        }
        String explicit = readProperties().getProperty(MODEL_EXPLICIT_KEY);
        return "true".equalsIgnoreCase(explicit);
    }
}
