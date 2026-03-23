# URGENT SECURITY ACTION REQUIRED

## Exposed API Key - Immediate Steps

Your API key `AIzaSyDU7dbjleDQ3xcn0EBZnYUuOg3oO1t-LQQ` was exposed in chat and must be rotated **immediately**.

### Step 1: Delete the Exposed Key

```bash
# Open Google Cloud Console
open "https://console.cloud.google.com/apis/credentials?project=daily-german-coach-sa"

# Or via CLI (if you know the key ID):
# gcloud alpha services api-keys delete KEY_ID --project=daily-german-coach-sa
```

In the Console:
1. Find the API key ending in `...LQQ`
2. Click the three dots (⋮) → **Delete**
3. Confirm deletion

### Step 2: Create a New API Key

```bash
# Via CLI (recommended - doesn't expose key in terminal history):
gcloud alpha services api-keys create \
  --display-name="Daily German Coach API Key" \
  --project=daily-german-coach-sa \
  --api-target=service=generativelanguage.googleapis.com

# This will output: Created key [projects/.../keys/...].
# Copy the key string (starts with AIza...)
```

Or in Console:
1. Click **+ CREATE CREDENTIALS** → **API key**
2. Copy the new key immediately
3. Click **RESTRICT KEY**
4. Under "API restrictions", select "Restrict key"
5. Search for and select: **Generative Language API**
6. Save

### Step 3: Update Your Local .env

```bash
# Edit .env and replace the placeholder
nano .env
# or
code .env

# Paste your new API key:
AI_PROVIDER_KEY="AIzaSy..."  # <- paste new key here
```

### Step 4: Verify .gitignore

```bash
# Ensure .env is ignored
grep -q "^\.env$" .gitignore || echo ".env" >> .gitignore

# Check git status - .env should NOT appear
git status
```

### Step 5: Check for Accidental Commits

```bash
# Search git history for the old key
git log -p -S 'AIzaSyDU7dbjleDQ3xcn0EBZnYUuOg3oO1t-LQQ'

# If found in history, you need to scrub it:
# (WARNING: rewrites history - coordinate with team)
# git filter-branch --force --index-filter \
#   "git rm --cached --ignore-unmatch .env" \
#   --prune-empty --tag-name-filter cat -- --all
```

### Step 6: Test with New Key

```bash
# Source the updated .env
source .env

# Quick test
curl -s "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$AI_PROVIDER_KEY" \
  -H "Content-Type: application/json" \
  -d '{"contents":[{"parts":[{"text":"Hello"}]}]}' | jq .
```

## Why This Matters

- **Quota Theft**: Anyone with your key can consume your quota
- **Cost**: If you have billing enabled, unauthorized usage = charges
- **Rate Limits**: Malicious actors can exhaust your rate limits
- **Security Audit**: Exposed keys trigger security alerts in some orgs

## Prevention Going Forward

1. **Never commit** `.env` files
2. **Use environment variables** or secret managers in production
3. **Rotate keys regularly** (e.g., quarterly)
4. **Use ADC** (Application Default Credentials) instead of API keys when possible
5. **Set API restrictions** (limit to specific APIs and IPs if possible)

## ADC Alternative (Recommended)

Instead of API keys, use service account credentials:

```bash
# Already configured in your .env:
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.gcp/daily-german-coach-sa.json"
export AI_PROVIDER_USE_KEY_QUERY=false  # Use bearer token instead

# Enable SDK mode (uses ADC automatically):
export AI_PROVIDER_USE_SDK=true
export GCP_PROJECT_ID="daily-german-coach-sa"

# Run your app - no API key needed!
mvn spring-boot:run
```

## Questions?

After rotating the key, test with:
```bash
source .env
mvn spring-boot:run
# In another terminal:
curl http://localhost:8080/api/german/enrich/preview -H "Content-Type: application/json" -d '{"german":"Haus"}'
```
