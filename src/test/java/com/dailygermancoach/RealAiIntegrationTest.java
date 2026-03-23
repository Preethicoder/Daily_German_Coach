package com.dailygermancoach;

import com.dailygermancoach.model.SentenceRequest;
import com.dailygermancoach.model.SentenceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Conditional integration test that hits the real AI provider.
 *
 * To run: export RUN_REAL_AI_INTEGRATION=true and ensure ADC or API key is available in your environment.
 */
@EnabledIfEnvironmentVariable(named = "RUN_REAL_AI_INTEGRATION", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RealAiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void previewEnrichWithRealAiReturnsTranslationAndSentence() {
        String url = "http://localhost:" + port + "/api/german/enrich/preview";
        // Use a real word that's not in the local fallback map to exercise the provider
        SentenceRequest req = new SentenceRequest(null, "Zimmer");

        var resp = restTemplate.postForEntity(url, req, SentenceResponse.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        SentenceResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTranslation()).withFailMessage("translation was null or empty; check provider/credentials").isNotNull().isNotEmpty();
        assertThat(body.getSentence()).withFailMessage("sentence was null or empty; check provider/credentials").isNotNull().isNotEmpty();
    }
}
