# Daily German Coach - SDK Usage Guide

## SDK Mode (Recommended for Production)

The project now supports the official Google Generative AI Java SDK (`google-cloud-vertexai`) as an alternative to raw REST calls.

### Benefits of SDK Mode
- **Type Safety**: Native Java objects instead of manual JSON parsing
- **Automatic Retries**: Built-in retry logic for 429/500 errors
- **ADC Support**: Automatic Application Default Credentials authentication
- **Streaming**: Simplified streaming response handling
- **Maintained by Google**: Schema changes handled by library updates

### Configuration

Add these properties to your `application.properties` or set as environment variables:

```properties
# Enable SDK mode (default: false)
ai.provider.useSdk=true

# GCP project ID (required for SDK mode)
gcp.project.id=your-gcp-project-id

# GCP location (default: us-central1)
gcp.location=us-central1

# Model name (default: gemini-2.0-flash)
ai.provider.model=gemini-2.0-flash
```

### Environment Setup

Ensure Application Default Credentials are configured:

```bash
# Option 1: Service account (recommended for production)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.gcp/your-service-account.json"

# Option 2: User credentials (dev only)
gcloud auth application-default login
```

### Running with SDK

```bash
# Set required properties
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.gcp/daily-german-coach-sa.json"

# Run with SDK enabled
mvn spring-boot:run -Dai.provider.useSdk=true -Dgcp.project.id=your-project-id
```

### Fallback Behavior

When SDK mode is enabled:
1. `AiService.translateAndExample()` delegates to `GenAiSdkService`
2. If SDK call fails, falls back to local dictionary for common words
3. If SDK is disabled (`useSdk=false`), uses WebClient REST calls (current default)

### Testing

The integration tests work with both modes. To test SDK mode:

```bash
export RUN_REAL_AI_INTEGRATION=true
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.gcp/daily-german-coach-sa.json"
mvn -Dtest=RealAiIntegrationTest test -Dai.provider.useSdk=true -Dgcp.project.id=your-project-id
```

### Migration Path

Current setup maintains backward compatibility:
- Default mode: WebClient with REST calls (current behavior)
- Opt-in mode: SDK via `ai.provider.useSdk=true`
- Both modes support the same endpoints and response shapes

### Rate Limiting & Quotas

SDK automatically respects:
- Retry-After headers from 429 responses
- Exponential backoff with jitter
- Circuit breaking for persistent failures

If you hit quota limits (429), the SDK will:
1. Log the quota violation
2. Wait for the retry delay specified by the service
3. Retry with exponential backoff
4. Fall back to local dictionary after max retries

### Troubleshooting

**"gcp.project.id must be set when using SDK mode"**
- Set `gcp.project.id` property or `GCP_PROJECT_ID` environment variable

**ADC not found errors**
- Verify `GOOGLE_APPLICATION_CREDENTIALS` points to valid JSON
- Run `gcloud auth application-default print-access-token` to test

**VertexAI 403 Forbidden**
- Enable Vertex AI API in Cloud Console
- Grant service account `roles/aiplatform.user` or `roles/aiplatform.admin`

**Model not found**
- Check `ai.provider.model` matches an available model
- List models: `gcloud ai models list --region=us-central1`

## Security Notes

- **Never commit** API keys or service account JSON to source control
- Use environment variables or secret manager for credentials
- Rotate keys immediately if exposed
- Use ADC with service accounts in production (not API keys)
