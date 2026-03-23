package com.dailygermancoach;

import com.dailygermancoach.model.SentenceRequest;
import com.dailygermancoach.model.SentenceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AiPreviewIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void previewEnrichReturnsFallbackForHaus() {
        String url = "http://localhost:" + port + "/api/german/enrich/preview";
        SentenceRequest req = new SentenceRequest(null, "Haus");

        var resp = restTemplate.postForEntity(url, req, SentenceResponse.class);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        SentenceResponse body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTranslation()).isNotNull().isNotEmpty();
        assertThat(body.getSentence()).isNotNull().isNotEmpty();
    }
}
