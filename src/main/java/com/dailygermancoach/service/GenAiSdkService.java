package com.dailygermancoach.service;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Wrapper service for the official Google Generative AI Java SDK (Vertex AI).
 * Enabled only when ai.provider.useSdk=true.
 * Uses Application Default Credentials (ADC) automatically.
 */
@Service
@ConditionalOnProperty(name = "ai.provider.useSdk", havingValue = "true", matchIfMissing = false)
public class GenAiSdkService {
    private static final Logger logger = LoggerFactory.getLogger(GenAiSdkService.class);

    private final String projectId;
    private final String location;
    private final String modelName;

    public GenAiSdkService(
            @Value("${gcp.project.id:}") String projectId,
            @Value("${gcp.location:us-central1}") String location,
            @Value("${ai.provider.model:gemini-2.0-flash}") String modelName) {
        this.projectId = projectId;
        this.location = location;
        this.modelName = modelName;
        logger.info("GenAiSdkService initialized with project={}, location={}, model={}", projectId, location, modelName);
    }

    /**
     * Generate text from a simple prompt using the configured Gemini model.
     * Uses ADC for authentication.
     */
    public String generateText(String prompt) {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("gcp.project.id must be set when using SDK mode");
        }

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            String text = ResponseHandler.getText(response);
            logger.debug("SDK generateText response: {}", text);
            return text;
        } catch (IOException e) {
            logger.error("SDK generateText failed for prompt: {}", prompt, e);
            throw new RuntimeException("Failed to generate content via SDK", e);
        }
    }

    /**
     * Translate a German word to English and return a simple example sentence.
     * Returns a simple string; caller should parse if needed.
     */
    public String translateAndExample(String germanWord) {
        String prompt = "Translate the single German word \"" + germanWord + "\" to English. " +
                "Then provide a simple German example sentence using that word. " +
                "Respond in JSON format with two fields: {\"english\": \"...\", \"example\": \"...\"}";
        return generateText(prompt);
    }

    /**
     * Check if a user's German sentence is grammatically correct and uses the given word properly.
     */
    public String checkSentence(String germanWord, String userSentence) {
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
        return generateText(prompt);
    }
}
