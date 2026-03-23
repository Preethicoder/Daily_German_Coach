package com.dailygermancoach.service;

import com.dailygermancoach.model.SentenceRequest;
import com.dailygermancoach.model.SentenceResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.auth.oauth2.GoogleCredentials;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI service with configurable provider support. It supports a "providerType" flag that
 * can be set to 'gemini', 'openai', or 'generic'.
 *
 * For Gemini (Google Generative Models), a common pattern is to call the provider's
 * REST endpoint and pass an API key. Google API keys are often passed as a query
 * parameter (key=...), while OAuth Bearer tokens are provided in Authorization headers.
 *
 * This implementation is intentionally flexible: you can configure whether the
 * provider expects the API key as a query parameter (`ai.provider.useKeyQuery=true`) or
 * as an Authorization header (default false). You must set `AI_PROVIDER_KEY` in your
 * environment or `ai.provider.key` property with a valid API key or token.
 */
@Service
public class AiService {
    private static final Logger logger = LoggerFactory.getLogger(AiService.class);
    private final WebClient webClient;
    private final String configuredKey;
    private final String model;
    private final String providerType;
    private final boolean useKeyQuery;
    private final boolean useSdk;
    private final GenAiSdkService sdkService;

    public AiService(WebClient.Builder webClientBuilder,
                     @Value("${ai.provider.url:https://api.example.com/v1/generate}") String apiUrl,
                     @Value("${ai.provider.model:gemini-3-flash-preview}") String model,
                     @Value("${ai.provider.key:}") String configuredKey,
                     @Value("${ai.provider.type:gemini}") String providerType,
                     @Value("${ai.provider.useKeyQuery:true}") boolean useKeyQuery,
                     @Value("${ai.provider.useSdk:false}") boolean useSdk,
                     @org.springframework.beans.factory.annotation.Autowired(required = false) GenAiSdkService sdkService) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.configuredKey = configuredKey;
        this.model = model;
        this.providerType = providerType.toLowerCase();
        this.useKeyQuery = useKeyQuery;
        this.useSdk = useSdk;
        this.sdkService = sdkService;
        logger.info("AiService initialized: useSdk={}, providerType={}", useSdk, providerType);
    }

    /**
     * Translate a single German word to English and generate a simple German example sentence.
     * Returns a SentenceResponse where sentence=exampleSentence and translation=english.
     */
    public SentenceResponse translateAndExample(String germanWord) {
        // If SDK mode is enabled, delegate to SDK
        if (useSdk && sdkService != null) {
            try {
                String rawJson = sdkService.translateAndExample(germanWord);
                logger.info("SDK translateAndExample raw response: {}", rawJson);
                // Parse JSON from SDK response
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                JsonNode node = mapper.readTree(rawJson);
                String eng = node.has("english") ? node.get("english").asText() : null;
                String example = node.has("example") ? node.get("example").asText() : null;
                if (eng != null || example != null) {
                    SentenceResponse resp = new SentenceResponse();
                    resp.setTranslation(eng == null ? "" : eng);
                    resp.setSentence(example == null ? "" : example);
                    return resp;
                }
            } catch (Exception e) {
                logger.error("SDK translateAndExample failed, falling back to local dictionary", e);
            }
        }

        // If no provider is configured, return a local fallback
        SentenceResponse fallback = new SentenceResponse();
        // Quick local dictionary fallback for very common words so the API is usable without AI keys.
        try {
            java.util.Map<String, String[]> small = java.util.Map.of(
                    "haus", new String[]{"house", "Das Haus ist groß."},
                    "auto", new String[]{"car", "Das Auto ist neu."},
                    "hund", new String[]{"dog", "Der Hund läuft im Park."},
                    "katze", new String[]{"cat", "Die Katze schläft auf dem Sofa."}
            );
            String key = germanWord == null ? "" : germanWord.toLowerCase().trim();
            if (small.containsKey(key)) {
                String[] pair = small.get(key);
                SentenceResponse resp = new SentenceResponse();
                resp.setTranslation(pair[0]);
                resp.setSentence(pair[1]);
                return resp;
            }
        } catch (Exception ignored) {}
        if ((configuredKey == null || configuredKey.isBlank()) && !useKeyQuery) {
            // Attempt ADC token - we already try that in correctSentence but keep a safe fallback
        }

        String prompt = "Translate the single German word to English and return a simple German example sentence that uses the word. " +
                "Respond only in JSON with two fields: {\"english\": \"...\", \"example\": \"...\"}.\nWord: \"" + germanWord + "\"";

        // Build body similar to other calls
        Map<String, Object> body = new HashMap<>();
        if (providerType.equals("openai")) {
            Map<String, Object> message = Map.of("role", "user", "content", prompt);
            body.put("model", model);
            body.put("messages", new Object[] { message });
        } else {
            // Gemini format: contents array with parts
            Map<String, Object> part = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", new Object[] { part });
            body.put("contents", new Object[] { content });
        }

        try {
            // Decide authentication as in correctSentence
            String effectiveKeyLocal = configuredKey;
            if (effectiveKeyLocal == null || effectiveKeyLocal.isBlank()) effectiveKeyLocal = System.getenv("AI_PROVIDER_KEY");

            String bearerToken = null;
            if ((effectiveKeyLocal == null || effectiveKeyLocal.isBlank()) && !useKeyQuery) {
                try {
                    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                            .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
                    credentials.refreshIfExpired();
                    if (credentials.getAccessToken() != null) bearerToken = credentials.getAccessToken().getTokenValue();
                } catch (Exception ignored) {}
            }

            WebClient.RequestBodySpec req = webClient.post().contentType(MediaType.APPLICATION_JSON);
            if (useKeyQuery) {
                final String k = effectiveKeyLocal;
                req = webClient.post().uri(uriBuilder -> uriBuilder.queryParam("key", k).build()).contentType(MediaType.APPLICATION_JSON);
            } else {
                String tokenToUse = (bearerToken != null && !bearerToken.isBlank()) ? bearerToken : effectiveKeyLocal;
                req = req.header("Authorization", "Bearer " + tokenToUse);
            }

            JsonNode json = req.bodyValue(body).retrieve().bodyToMono(JsonNode.class).block(Duration.ofSeconds(20));
            if (json == null) {
                logger.info("AI provider returned no JSON response for translateAndExample prompt");
                return fallback;
            }
            logger.info("AI provider raw JSON (translateAndExample): {}", json.toString());

            // Extract text from Gemini format: candidates[0].content.parts[0].text
            String text = null;
            if (json.has("candidates")) {
                JsonNode candidates = json.get("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode first = candidates.get(0);
                    if (first.has("content") && first.get("content").has("parts")) {
                        JsonNode parts = first.get("content").get("parts");
                        if (parts.isArray() && parts.size() > 0) {
                            JsonNode firstPart = parts.get(0);
                            if (firstPart.has("text")) {
                                text = firstPart.get("text").asText();
                            }
                        }
                    } else if (first.has("output")) {
                        text = first.get("output").asText();
                    }
                }
            }
            if (text == null) {
                if (json.has("output")) text = json.get("output").asText();
                else if (json.has("response")) text = json.get("response").asText();
                else if (json.has("choices") && json.get("choices").isArray() && json.get("choices").size() > 0) {
                    JsonNode first = json.get("choices").get(0);
                    if (first.has("message") && first.get("message").has("content")) text = first.get("message").get("content").asText();
                    else if (first.has("text")) text = first.get("text").asText();
                } else if (json.has("text")) text = json.get("text").asText();
            }

            if (text == null) {
                logger.info("Could not extract text from provider JSON in translateAndExample; returning fallback. JSON: {}", json.toString());
                return fallback;
            }

            // Remove markdown code blocks if present (```json ... ```)
            text = text.trim();
            if (text.startsWith("```json")) {
                text = text.substring(7);
            }
            if (text.startsWith("```")) {
                text = text.substring(3);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3);
            }
            text = text.trim();

            // The model was instructed to return JSON. Try to parse it.
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                JsonNode node = mapper.readTree(text);
                String eng = null;
                String example = null;
                if (node.has("english")) eng = node.get("english").asText();
                if (node.has("example")) example = node.get("example").asText();
                if (eng != null || example != null) {
                    SentenceResponse resp = new SentenceResponse();
                    resp.setTranslation(eng == null ? "" : eng);
                    resp.setSentence(example == null ? "" : example);
                    return resp;
                }
            } catch (Exception e) {
                // not JSON, fall through
            }

            // If not JSON, try to split lines: first line English, second line example
            String[] lines = text.split("\n");
            String eng = lines.length > 0 ? lines[0].trim() : "";
            String example = lines.length > 1 ? lines[1].trim() : "";
            SentenceResponse resp = new SentenceResponse();
            resp.setTranslation(eng);
            resp.setSentence(example);
            return resp;
        } catch (Exception e) {
            logger.error("translateAndExample: provider call failed", e);
            return fallback;
        }
    }

    /**
     * Check if a user's German sentence is grammatically correct and uses the given word properly.
     * Returns AI feedback about the sentence.
     *
     * @param germanWord The German word that should be used in the sentence
     * @param userSentence The sentence created by the user
     * @return Feedback from AI about correctness and suggestions
     */
    public String checkUserSentence(String germanWord, String userSentence) {
        if (useSdk && sdkService != null) {
            try {
                return sdkService.checkSentence(germanWord, userSentence);
            } catch (Exception e) {
                logger.error("SDK checkSentence failed, falling back to REST", e);
            }
        }

        String prompt = String.format(
            "Check if this German sentence is grammatically correct and properly uses the word '%s': \"%s\"\n\n" +
            "Respond in JSON format with:\n" +
            "{\n" +
            "  \"correct\": true/false,\n" +
            "  \"feedback\": \"explanation of what's right or wrong\",\n" +
            "  \"suggestion\": \"corrected sentence if needed, or empty string if correct\"\n" +
            "}\n" +
            "Only return valid JSON, no markdown or extra text.",
            germanWord, userSentence
        );

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("contents", new Object[] {
                Map.of("parts", new Object[] {
                    Map.of("text", prompt)
                })
            });

            WebClient.RequestBodySpec request;
            
            if (useKeyQuery) {
                // Key as query parameter
                request = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", configuredKey).build())
                    .contentType(MediaType.APPLICATION_JSON);
            } else if (configuredKey != null && !configuredKey.isBlank()) {
                // Authorization header
                request = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + configuredKey);
            } else {
                request = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON);
            }

            String rawJson = request
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(20))
                .block();

            logger.info("AI provider raw JSON (checkUserSentence): {}", rawJson);

            // Parse response - extract text from Gemini response structure
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode json = mapper.readTree(rawJson);
            String textContent = null;

            // Try Gemini format: candidates[0].content.parts[0].text
            if (json.has("candidates")) {
                JsonNode candidates = json.get("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode first = candidates.get(0);
                    if (first.has("content") && first.get("content").has("parts")) {
                        JsonNode parts = first.get("content").get("parts");
                        if (parts.isArray() && parts.size() > 0) {
                            JsonNode firstPart = parts.get(0);
                            if (firstPart.has("text")) {
                                textContent = firstPart.get("text").asText();
                            }
                        }
                    }
                }
            }

            if (textContent == null) {
                return "{\"correct\": false, \"feedback\": \"Unable to check sentence at this time.\", \"suggestion\": \"\"}";
            }

            // Remove markdown code blocks if present
            textContent = textContent.trim();
            if (textContent.startsWith("```json")) {
                textContent = textContent.substring(7);
            }
            if (textContent.startsWith("```")) {
                textContent = textContent.substring(3);
            }
            if (textContent.endsWith("```")) {
                textContent = textContent.substring(0, textContent.length() - 3);
            }
            textContent = textContent.trim();

            // Return the AI's JSON response
            return textContent;

        } catch (Exception e) {
            logger.error("checkUserSentence: provider call failed", e);
            return "{\"correct\": false, \"feedback\": \"Error checking sentence: " + e.getMessage() + "\", \"suggestion\": \"\"}";
        }
    }
}
